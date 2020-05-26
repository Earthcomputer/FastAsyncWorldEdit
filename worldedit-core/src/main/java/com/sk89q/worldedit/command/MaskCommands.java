// TODO: Ping @MattBDev to reimplement (or remove because this class is stupid) 2020-02-04
//package com.sk89q.worldedit.command;
//
//import com.boydti.fawe.object.mask.AdjacentAnyMask;
//import com.boydti.fawe.object.mask.AdjacentMask;
//import com.boydti.fawe.object.mask.AngleMask;
//import com.boydti.fawe.object.mask.BiomeMask;
//import com.boydti.fawe.object.mask.BlockLightMask;
//import com.boydti.fawe.object.mask.BrightnessMask;
//import com.boydti.fawe.object.mask.DataMask;
//import com.boydti.fawe.object.mask.ExtremaMask;
//import com.boydti.fawe.object.mask.IdDataMask;
//import com.boydti.fawe.object.mask.IdMask;
//import com.boydti.fawe.object.mask.LightMask;
//import com.boydti.fawe.object.mask.OpacityMask;
//import com.boydti.fawe.object.mask.ROCAngleMask;
//import com.boydti.fawe.object.mask.RadiusMask;
//import com.boydti.fawe.object.mask.RandomMask;
//import com.boydti.fawe.object.mask.SimplexMask;
//import com.boydti.fawe.object.mask.SkyLightMask;
//import com.boydti.fawe.object.mask.SurfaceMask;
//import com.boydti.fawe.object.mask.WallMask;
//import com.boydti.fawe.function.mask.XAxisMask;
//import com.boydti.fawe.function.mask.YAxisMask;
//import com.boydti.fawe.function.mask.ZAxisMask;
//import com.sk89q.worldedit.IncompleteRegionException;
//import com.sk89q.worldedit.LocalSession;
//import com.sk89q.worldedit.WorldEdit;
//import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
//import com.sk89q.worldedit.entity.Player;
//import com.sk89q.worldedit.extent.Extent;
//import com.sk89q.worldedit.function.mask.BlockMaskBuilder;
//import com.sk89q.worldedit.function.mask.ExistingBlockMask;
//import com.sk89q.worldedit.function.mask.ExpressionMask;
//import com.sk89q.worldedit.function.mask.Mask;
//import com.sk89q.worldedit.function.mask.MaskIntersection;
//import com.sk89q.worldedit.function.mask.MaskUnion;
//import com.sk89q.worldedit.function.mask.Masks;
//import com.sk89q.worldedit.function.mask.OffsetMask;
//import com.sk89q.worldedit.function.mask.RegionMask;
//import com.sk89q.worldedit.function.mask.SolidBlockMask;
//import com.sk89q.worldedit.internal.expression.Expression;
//import com.sk89q.worldedit.internal.expression.ExpressionEnvironment;
//import com.sk89q.worldedit.internal.expression.ExpressionException;
//import com.sk89q.worldedit.math.BlockVector3;
//import com.sk89q.worldedit.math.Vector3;
//import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
//import com.sk89q.worldedit.session.request.RequestSelection;
//import com.sk89q.worldedit.world.biome.BiomeType;
//import org.enginehub.piston.annotation.Command;
//import org.enginehub.piston.annotation.CommandContainer;
//import org.enginehub.piston.annotation.param.Arg;
//import org.enginehub.piston.annotation.param.Switch;
//
////@Command(aliases = {"masks"},
////        desc = "Help for the various masks. [More Info](https://git.io/v9r4K)",
////        descFooter = "Masks determine if a block can be placed\n" +
////                " - Use [brackets] for arguments\n" +
////                " - Use , to OR multiple\n" +
////                " - Use & to AND multiple\n" +
////                "e.g. >[stone,dirt],#light[0][5],$jungle\n" +
////                "More Info: https://git.io/v9r4K"
////)
//@CommandContainer//(superTypes = CommandPermissionsConditionGenerator.Registration.class)
//public class MaskCommands {
//    private final WorldEdit worldEdit;
//
//    public MaskCommands(WorldEdit worldEdit) {
//        this.worldEdit = worldEdit;
//    }
//
//    @Command(
//            name = "#simplex",
//            desc = "Use simplex noise as the mask"
//    )
//    public Mask simplex(@Arg(desc = "double scale") double scale, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        scale = 1d / Math.max(1, scale);
//        minInt = (minInt - 50) / 50;
//        maxInt = (maxInt - 50) / 50;
//        return new SimplexMask(scale, minInt, maxInt);
//    }
//
//    @Command(
//            name = "#light",
//            desc = "Restrict to specific light levels"
//    )
//    public Mask light(Extent extent, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        return new LightMask(extent, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "#skylight",
//            desc = "Restrict to specific sky light levels"
//    )
//    public Mask skylight(Extent extent, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        return new SkyLightMask(extent, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "#blocklight",
//            aliases = {"#emittedlight"},
//            desc = "Restrict to specific block light levels"
//    )
//    public Mask blocklight(Extent extent, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        return new BlockLightMask(extent, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "#opacity",
//            desc = "Restrict to specific opacity levels"
//    )
//    public Mask opacity(Extent extent, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        return new OpacityMask(extent, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "#brightness",
//            desc = "Restrict to specific block brightness"
//    )
//    public Mask brightness(Extent extent, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) {
//        return new BrightnessMask(extent, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "#haslight",
//            desc = "Restricts to blocks with light (sky or emitted)"
//    )
//    public Mask haslight(Extent extent) {
//        return new LightMask(extent, 1, Integer.MAX_VALUE);
//    }
//
//    @Command(
//            name = "#nolight",
//            desc = "Restrict to blocks without light (sky or emitted)"
//    )
//    public Mask nolight(Extent extent) {
//        return new LightMask(extent, 0, 0);
//    }
//
//    @Command(
//            name = "#region",
//            aliases = {"#selection", "#sel"},
//            desc = "inside the provided selection"
//    )
//    public Mask selection(Player player, LocalSession session) throws IncompleteRegionException {
//        return new RegionMask(session.getSelection(player.getWorld()).clone());
//    }
//
//    @Command(
//            name = "#xaxis",
//            desc = "Restrict to initial x axis"
//    )
//    public Mask xaxis() {
//        return new XAxisMask();
//    }
//
//    @Command(
//            name = "#yaxis",
//            desc = "Restrict to initial y axis"
//    )
//    public Mask yaxis() {
//        return new YAxisMask();
//    }
//
//    @Command(
//            name = "#zaxis",
//            desc = "Restrict to initial z axis"
//    )
//    public Mask zaxis() {
//        return new ZAxisMask();
//    }
//
//    @Command(
//            name = "#id",
//            desc = "Restrict to initial id"
//    )
//    public Mask id(Extent extent) {
//        return new IdMask(extent);
//    }
//
//    @Command(
//            name = "#data",
//            desc = "Restrict to initial data"
//    )
//    public Mask data(Extent extent) {
//        return new DataMask(extent);
//    }
//
//    @Command(
//            name = "#iddata",
//            desc = "Restrict to initial block id and data"
//    )
//    public Mask iddata(Extent extent) {
//        return new IdDataMask(extent);
//    }
//
//    @Command(
//            name = "#wall",
//            desc = "Restrict to walls (any block n,e,s,w of air)"
//    )
//    public Mask wall(Extent extent) {
//        Mask blockMask = air(extent);
//        return new MaskUnion(new ExistingBlockMask(extent), new WallMask(blockMask, 1, 8));
//    }
//
//    @Command(
//            name = "#surface",
//            desc = "Restrict to surfaces (any solid block touching air)"
//    )
//    public Mask surface(Extent extent) {
//        return new SurfaceMask(extent);
//    }
//
//    @Command(
//            name = "\\",
//            aliases = {"/", "#angle", "#\\", "#/"},
//            desc = "Restrict to specific terrain angle",
//            descFooter = "Restrict to specific terrain angle\n" +
//                    "The -o flag will only overlay\n" +
//                    "Example: /[0d][45d]\n" +
//                    "Explanation: Allows any block where the adjacent block is between 0 and 45 degrees.\n" +
//                    "Example: /[3][20]\n" +
//                    "Explanation: Allows any block where the adjacent block is between 3 and 20 blocks below"
//)
//    public Mask angle(Extent extent, @Arg(name="min", desc = "min angle") String minStr, @Arg(name="max", desc = "max angle") String maxStr, @Switch(name = 'o', desc = "TODO") boolean overlay, @Arg(name = "distance", desc = "int", def = "1") int distanceOpt) throws ExpressionException {
//        double y1, y2;
//        boolean override;
//        if (maxStr.endsWith("d")) {
//            double y1d = Expression.compile(minStr.substring(0, minStr.length() - 1)).evaluate();
//            double y2d = Expression.compile(maxStr.substring(0, maxStr.length() - 1)).evaluate();
//            y1 = Math.tan(y1d * (Math.PI / 180));
//            y2 = Math.tan(y2d * (Math.PI / 180));
//        } else {
//            y1 = Expression.compile(minStr).evaluate();
//            y2 = Expression.compile(maxStr).evaluate();
//        }
//        return new AngleMask(extent, y1, y2, overlay, distanceOpt);
//    }
//
//    @Command(
//            name = "(",
//            aliases = {")", "#roc", "#(", "#)"},
//            desc = "Restrict to near specific terrain slope rate of change",
//            descFooter = "Restrict to near specific terrain slope rate of change\n" +
//                    "The -o flag will only overlay\n" +
//                    "Example: ([0d][45d][5]\n" +
//                    "Explanation: Restrict near where the angle changes between 0-45 degrees within 5 blocks\n" +
//                    "Note: Use negatives for decreasing slope"
//)
//    public Mask roc(Extent extent, @Arg(name="min", desc = "min angle") String minStr, @Arg(name="max", desc = "max angle") String maxStr, @Switch(name = 'o', desc = "TODO") boolean overlay, @Arg(name = "distance", desc = "int", def = "4") int distanceOpt) throws ExpressionException {
//        double y1, y2;
//        boolean override;
//        if (maxStr.endsWith("d")) {
//            double y1d = Expression.compile(minStr.substring(0, minStr.length() - 1)).evaluate();
//            double y2d = Expression.compile(maxStr.substring(0, maxStr.length() - 1)).evaluate();
//            y1 = Math.tan(y1d * (Math.PI / 180));
//            y2 = Math.tan(y2d * (Math.PI / 180));
//        } else {
//            y1 = Expression.compile(minStr).evaluate();
//            y2 = Expression.compile(maxStr).evaluate();
//        }
//        return new ROCAngleMask(extent, y1, y2, overlay, distanceOpt);
//    }
//
//    @Command(
//            name = "^",
//            aliases = {"#extrema", "#^"},
//            desc = "Restrict to near specific terrain extrema",
//            descFooter = "Restrict to near specific terrain extrema\n" +
//                    "The -o flag will only overlay\n" +
//                    "Example: ([0d][45d][5]\n" +
//                    "Explanation: Restrict to near 45 degrees of local maxima\n" +
//                    "Note: Use negatives for local minima"
//)
//    public Mask extrema(Extent extent, @Arg(name="min", desc = "min angle") String minStr, @Arg(name="max", desc = "max angle") String maxStr, @Switch(name = 'o', desc = "TODO") boolean overlay, @Arg(name = "distance", desc = "int", def = "4") int distanceOpt) throws ExpressionException {
//        double y1, y2;
//        boolean override;
//        if (maxStr.endsWith("d")) {
//            double y1d = Expression.compile(minStr.substring(0, minStr.length() - 1)).evaluate();
//            double y2d = Expression.compile(maxStr.substring(0, maxStr.length() - 1)).evaluate();
//            y1 = Math.tan(y1d * (Math.PI / 180));
//            y2 = Math.tan(y2d * (Math.PI / 180));
//        } else {
//            y1 = Expression.compile(minStr).evaluate();
//            y2 = Expression.compile(maxStr).evaluate();
//        }
//        return new ExtremaMask(extent, y1, y2, overlay, distanceOpt);
//    }
//
//    @Command(
//            name = "{",
//            aliases = {"#{"},
//            desc = "Restricts blocks to within a specific radius range of the initial block"
//)
//    public Mask radius(@Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) throws ExpressionException {
//        return new RadiusMask((int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "|",
//            aliases = {"#|", "#side"},
//            desc = "sides with a specific number of other blocks"
//)
//    public Mask wall(@Arg(desc = "Mask") Mask mask, @Arg(name="mine", desc = "min light") double minInt, @Arg(name="mine", desc = "max light") double maxInt) throws ExpressionException {
//        return new WallMask(mask, (int) minInt, (int) maxInt);
//    }
//
//    @Command(
//            name = "~",
//            aliases = {"#~", "#adjacent"},
//            desc = "Adjacent to a specific number of other blocks"
//)
//    public Mask adjacent(@Arg(desc = "Mask") Mask mask, @Arg(name = "min", desc = "double", def = "-1") double min, @Arg(name = "max", desc = "double", def = "-1") double max) throws ExpressionException {
//        if (min == -1 && max == -1) {
//            min = 1;
//            max = 8;
//        } else if (max == -1) max = min;
//        if (max >= 8 && min == 1) {
//            return new AdjacentAnyMask(mask);
//        }
//        return new AdjacentMask(mask, (int) min, (int) max);
//    }
//
//}
