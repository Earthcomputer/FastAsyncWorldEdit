package com.boydti.fawe.util;

import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReflectionUtils {

    public static <T> T as(Class<T> t, Object o) {
        return t.isInstance(o) ? t.cast(o) : null;
    }

    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String enumName) {
        return ReflectionUtils9.addEnum(enumType, enumName);
    }

    public static void setAccessibleNonFinal(Field field)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        // let's make the field accessible
        field.setAccessible(true);

        // next we change the modifier in the Field instance to
        // not be final anymore, thus tricking reflection into
        // letting us modify the static final field
        if (Modifier.isFinal(field.getModifiers())) {
            try {
                Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
                lookupField.setAccessible(true);

                // blank out the final bit in the modifiers int
                ((MethodHandles.Lookup) lookupField.get(null))
                    .findSetter(Field.class, "modifiers", int.class)
                    .invokeExact(field, field.getModifiers() & ~Modifier.FINAL);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static void setFailsafeFieldValue(Field field, Object target, Object value)
        throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        setAccessibleNonFinal(field);
        field.set(target, value);
    }

    private static void blankField(Class<?> enumClass, String fieldName)
        throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        for (Field field : Class.class.getDeclaredFields()) {
            if (field.getName().contains(fieldName)) {
                AccessibleObject.setAccessible(new Field[]{field}, true);
                setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }

    static void cleanEnumCache(Class<?> enumClass)
        throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        blankField(enumClass, "enumConstantDirectory"); // Sun (Oracle?!?) JDK 1.5/6
        blankField(enumClass, "enumConstants"); // IBM JDK
    }

    public static <T> List<T> getList(List<T> list) {
        try {
            Class<? extends List<T>> clazz = (Class<? extends List<T>>) Class
                .forName("java.util.Collections$UnmodifiableList");
            if (!clazz.isInstance(list)) {
                return list;
            }
            Field m = clazz.getDeclaredField("list");
            m.setAccessible(true);
            return (List<T>) m.get(list);
        } catch (Throwable e) {
            e.printStackTrace();
            return list;
        }
    }

    public static Object getHandle(Object wrapper) {
        final Method getHandle = makeMethod(wrapper.getClass(), "getHandle");
        return callMethod(getHandle, wrapper);
    }

    //Utils
    public static Method makeMethod(Class<?> clazz, String methodName, Class<?>... parameters) {
        try {
            return clazz.getDeclaredMethod(methodName, parameters);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T callMethod(Method method, Object instance, Object... parameters) {
        if (method == null) {
            throw new RuntimeException("No such method");
        }
        method.setAccessible(true);
        try {
            return (T) method.invoke(instance, parameters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> makeConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return (Constructor<T>) clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T> T callConstructor(Constructor<T> constructor, Object... parameters) {
        if (constructor == null) {
            throw new RuntimeException("No such constructor");
        }
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(parameters);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getCause());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field makeField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ex) {
            return null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Field findField(Class<?> clazz, Class<?> type, int hasMods, int noMods) {
        for (Field field : clazz.getDeclaredFields()) {
            if (type == null || type.isAssignableFrom(field.getType())) {
                int mods = field.getModifiers();
                if ((mods & hasMods) == hasMods && (mods & noMods) == 0) {
                    return setAccessible(field);
                }
            }
        }
        return null;
    }

    public static Field findField(Class<?> clazz, Class<?> type) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() == type) {
                return setAccessible(field);
            }
        }
        return null;
    }

    public static Method findMethod(Class<?> clazz, Class<?> returnType, Class... params) {
        return findMethod(clazz, 0, returnType, params);
    }

    public static Method findMethod(Class<?> clazz, int index, int hasMods, int noMods,
        Class<?> returnType, Class... params) {
        outer:
        for (Method method : sortMethods(clazz.getDeclaredMethods())) {
            if (returnType == null || method.getReturnType() == returnType) {
                Class<?>[] mp = method.getParameterTypes();
                int mods = method.getModifiers();
                if ((mods & hasMods) != hasMods || (mods & noMods) != 0) {
                    continue;
                }
                if (params == null) {
                    if (index-- == 0) {
                        return setAccessible(method);
                    } else {
                        continue;
                    }
                }
                if (mp.length == params.length) {
                    for (int i = 0; i < mp.length; i++) {
                        if (mp[i] != params[i]) {
                            continue outer;
                        }
                    }
                    if (index-- == 0) {
                        return setAccessible(method);
                    } else {
                        continue;
                    }
                }
            }
        }
        return null;
    }

    public static Method[] sortMethods(Method[] methods) {
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        return methods;
    }

    public static Field[] sortFields(Field[] fields) {
        Arrays.sort(fields, Comparator.comparing(Field::getName));
        return fields;
    }

    public static Method findMethod(Class<?> clazz, int index, Class<?> returnType,
        Class<?>... params) {
        return findMethod(clazz, index, 0, 0, returnType, params);
    }

    public static <T extends AccessibleObject> T setAccessible(T ao) {
        ao.setAccessible(true);
        return ao;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getField(@NotNull Field field, Object instance) {
        field.setAccessible(true);
        try {
            return (T) field.get(instance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void setField(@NotNull Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static <T> Class<? extends T> getClass(String name, Class<T> superClass) {
        try {
            return Class.forName(name).asSubclass(superClass);
        } catch (ClassCastException | ClassNotFoundException ex) {
            return null;
        }
    }


    /**
     * Get RefClass object by real class.
     *
     * @param clazz class
     * @return RefClass based on passed class
     */
    public static RefClass getRefClass(Class<?> clazz) {
        return new RefClass(clazz);
    }

    /**
     * RefClass - utility to simplify work with reflections.
     */
    public static class RefClass {

        private final Class<?> clazz;

        private RefClass(Class<?> clazz) {
            this.clazz = clazz;
        }

        public Class<?> getRealClass() {
            return this.clazz;
        }

        public boolean isInstance(Object object) {
            return this.clazz.isInstance(object);
        }

        /**
         * Get existing method by name and types.
         *
         * @param name name
         * @param types method parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException if method not found
         */
        public RefMethod getMethod(String name, Object... types) throws NoSuchMethodException {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    } else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    } else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefMethod(this.clazz.getMethod(name, classes));
                } catch (NoSuchMethodException ignored) {
                    return new RefMethod(this.clazz.getDeclaredMethod(name, classes));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Get existing constructor by types.
         *
         * @param types parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException if constructor not found
         */
        public RefConstructor getConstructor(Object... types) {
            try {
                final Class[] classes = new Class[types.length];
                int i = 0;
                for (Object e : types) {
                    if (e instanceof Class) {
                        classes[i++] = (Class) e;
                    } else if (e instanceof RefClass) {
                        classes[i++] = ((RefClass) e).getRealClass();
                    } else {
                        classes[i++] = e.getClass();
                    }
                }
                try {
                    return new RefConstructor(this.clazz.getConstructor(classes));
                } catch (NoSuchMethodException ignored) {
                    return new RefConstructor(this.clazz.getDeclaredConstructor(classes));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * find method by type parameters.
         *
         * @param types parameters. can be Class or RefClass
         * @return RefMethod object
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethod(Object... types) {
            final Class[] classes = new Class[types.length];
            int t = 0;
            for (Object e : types) {
                if (e instanceof Class) {
                    classes[t++] = (Class) e;
                } else if (e instanceof RefClass) {
                    classes[t++] = ((RefClass) e).getRealClass();
                } else {
                    classes[t++] = e.getClass();
                }
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            findMethod:
            for (Method m : methods) {
                final Class<?>[] methodTypes = m.getParameterTypes();
                if (methodTypes.length != classes.length) {
                    continue;
                }
                for (Class aClass : classes) {
                    if (!Arrays.equals(classes, methodTypes)) {
                        continue findMethod;
                    }
                    return new RefMethod(m);
                }
            }
            throw new RuntimeException("no such method");
        }

        /**
         * find method by name.
         *
         * @param names possible names of method
         * @return RefMethod object
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByName(String... names) {
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            for (Method m : methods) {
                for (String name : names) {
                    if (m.getName().equals(name)) {
                        return new RefMethod(m);
                    }
                }
            }
            throw new RuntimeException("no such method");
        }

        /**
         * find method by return value.
         *
         * @param type type of returned value
         * @return RefMethod
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByReturnType(RefClass type) {
            return this.findMethodByReturnType(type.clazz);
        }

        /**
         * find method by return value.
         *
         * @param type type of returned value
         * @return RefMethod
         * @throws RuntimeException if method not found
         */
        public RefMethod findMethodByReturnType(Class<?> type) {
            if (type == null) {
                type = void.class.getComponentType();
            }
            final List<Method> methods = new ArrayList<>();
            Collections.addAll(methods, this.clazz.getMethods());
            Collections.addAll(methods, this.clazz.getDeclaredMethods());
            for (Method m : methods) {
                if (type.equals(m.getReturnType())) {
                    return new RefMethod(m);
                }
            }
            throw new RuntimeException("no such method");
        }

        /**
         * find constructor by number of arguments.
         *
         * @param number number of arguments
         * @return RefConstructor
         * @throws RuntimeException if constructor not found
         */
        public RefConstructor findConstructor(int number) {
            final List<Constructor<?>> constructors = new ArrayList<>();
            Collections.addAll(constructors, this.clazz.getConstructors());
            Collections.addAll(constructors, this.clazz.getDeclaredConstructors());
            for (Constructor<?> m : constructors) {
                if (m.getParameterTypes().length == number) {
                    return new RefConstructor(m);
                }
            }
            throw new RuntimeException("no such constructor");
        }

        /**
         * get field by name.
         *
         * @param name field name
         * @return RefField
         * @throws RuntimeException if field not found
         */
        public RefField getField(String name) {
            try {
                try {
                    return new RefField(this.clazz.getField(name));
                } catch (NoSuchFieldException ignored) {
                    return new RefField(this.clazz.getDeclaredField(name));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * find field by type.
         *
         * @param type field type
         * @return RefField
         * @throws RuntimeException if field not found
         */
        public RefField findField(RefClass type) {
            return this.findField(type.clazz);
        }

        /**
         * find field by type.
         *
         * @param type field type
         * @return RefField
         * @throws RuntimeException if field not found
         */
        public RefField findField(Class<?> type) {
            if (type == null) {
                type = void.class;
            }
            final List<Field> fields = new ArrayList<>();
            Collections.addAll(fields, this.clazz.getFields());
            Collections.addAll(fields, this.clazz.getDeclaredFields());
            for (Field f : fields) {
                if (type.equals(f.getType())) {
                    return new RefField(f);
                }
            }
            throw new RuntimeException("no such field");
        }
    }

    /**
     * Method wrapper.
     */
    public static class RefMethod {

        private final Method method;

        private RefMethod(Method method) {
            this.method = method;
            method.setAccessible(true);
        }

        public Method getRealMethod() {
            return this.method;
        }

        public RefClass getRefClass() {
            return new RefClass(this.method.getDeclaringClass());
        }

        public RefClass getReturnRefClass() {
            return new RefClass(this.method.getReturnType());
        }

        /**
         * apply method to object.
         *
         * @param e object to which the method is applied
         * @return RefExecutor with method call(...)
         */
        public RefExecutor of(Object e) {
            return new RefExecutor(e);
        }

        /**
         * call static method.
         *
         * @param params sent parameters
         * @return return value
         */
        public Object call(Object... params) {
            try {
                return this.method.invoke(null, params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public class RefExecutor {

            final Object executor;

            public RefExecutor(Object executor) {
                this.executor = executor;
            }

            /**
             * apply method for selected object.
             *
             * @param params sent parameters
             * @return return value
             * @throws RuntimeException if something went wrong
             */
            public Object call(Object... params) {
                try {
                    return RefMethod.this.method.invoke(this.executor, params);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Constructor wrapper.
     */
    public static class RefConstructor {

        private final Constructor<?> constructor;

        private RefConstructor(Constructor<?> constructor) {
            this.constructor = constructor;
            constructor.setAccessible(true);
        }

        public Constructor<?> getRealConstructor() {
            return this.constructor;
        }

        public RefClass getRefClass() {
            return new RefClass(this.constructor.getDeclaringClass());
        }

        /**
         * Create a new instance with constructor.
         *
         * @param params parameters for constructor
         * @return new object
         * @throws RuntimeException if something went wrong
         */
        public Object create(Object... params) {
            try {
                return this.constructor.newInstance(params);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class RefField {

        private final Field field;

        private RefField(Field field) {
            this.field = field;
            field.setAccessible(true);
        }

        public Field getRealField() {
            return this.field;
        }

        public RefClass getRefClass() {
            return new RefClass(this.field.getDeclaringClass());
        }

        public RefClass getFieldRefClass() {
            return new RefClass(this.field.getType());
        }

        /**
         * Apply a field for object.
         *
         * @param e applied object
         * @return RefExecutor with getter and setter
         */
        public RefExecutor of(Object e) {
            return new RefExecutor(e);
        }

        public class RefExecutor {

            final Object executor;

            public RefExecutor(Object executor) {
                this.executor = executor;
            }

            /**
             * Set field value for applied object.
             *
             * @param param value
             */
            public void set(Object param) {
                try {
                    RefField.this.field.set(this.executor, param);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            /**
             * Get field value for the applied object.
             *
             * @return value of field
             */
            public Object get() {
                try {
                    return RefField.this.field.get(this.executor);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
