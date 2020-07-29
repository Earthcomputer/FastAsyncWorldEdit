import org.ajoberstar.grgit.Grgit
import java.time.format.DateTimeFormatter

plugins {
    `maven-publish`
}

logger.lifecycle("""
*******************************************
 You are building FastAsyncWorldEdit!

 If you encounter trouble:
 1) Read COMPILING.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/KxkjDVg

 Output files will be in [subproject]/build/libs
*******************************************
""")
//TODO FIX THIS WHEN I FEEL LIKE IT
var rootVersion = "1.16"
var revision: String = ""
var buildNumber = ""
var date: String = ""
ext {
    val git: Grgit = Grgit.open {
        dir = File("$rootDir/.git");
    }
    ext["date"] = git.head().dateTime.format(DateTimeFormatter.ofPattern("yy.MM.dd"));
    ext["revision"] = "-${git.head().abbreviatedId}";
    var parents: MutableList<String>? = git.head().parentIds;
    if (project.hasProperty("buildnumber")) {
        buildNumber = project.properties["buildnumber"] as String;
    } else {
        var index = -2109;  // Offset to match CI
        while (parents != null && parents.isNotEmpty()) {
            parents = git.resolve.toCommit(parents[0]).parentIds
            index++;
        }
        buildNumber = index.toString();
    }
}

allprojects {
    gradle.projectsEvaluated {
        tasks.withType(JavaCompile::class) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "1000"))
        }
    }
}

version = String.format("%s-%s", rootVersion, buildNumber)

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}

val props = java.util.Properties()
props.load(java.io.FileReader("credentials.properties"))

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            subprojects {
                afterEvaluate {
                    val proj = this
                    try {
                        val jar = tasks.getByName<Jar>("jar")
                        val sourcesJar = tasks.getByName<Jar>("sourcesJar")
                        artifact(jar) {
                            classifier = proj.name
                            builtBy(jar)
                        }
                        artifact(sourcesJar) {
                            builtBy(sourcesJar)
                            classifier = proj.name + "-sources"
                        }
                    } catch (e: UnknownTaskException) {
                        // ignore
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://api.bintray.com/maven/earthcomputer/mods/fawe/")
            credentials {
                username = "earthcomputer"
                password = props.getProperty("bintrayPass")
            }
        }
    }
}
