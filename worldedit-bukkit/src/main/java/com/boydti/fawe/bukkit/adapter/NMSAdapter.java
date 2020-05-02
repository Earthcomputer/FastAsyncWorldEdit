package com.boydti.fawe.bukkit.adapter;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockID;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.Map;
import java.util.function.Function;

public class NMSAdapter {
    public static int createPalette(int[] blockToPalette, int[] paletteToBlock, int[] blocksCopy,
        int[] num_palette_buffer, char[] set, Map<BlockVector3, Integer> ticking_blocks) {
        int air = 0;
        int num_palette = 0;
        for (int i = 0; i < 4096; i++) {
            char ordinal = set[i];
            switch (ordinal) {
                case 0:
                    ordinal = BlockID.AIR;
                case BlockID.AIR:
                case BlockID.CAVE_AIR:
                case BlockID.VOID_AIR:
                    air++;
                    break;
                default:
                    BlockState state = BlockState.getFromOrdinal(ordinal);
                    if (state.getMaterial().isTicksRandomly()) {
                        ticking_blocks.put(BlockVector3.at(i & 15, (i >> 8) & 15, (i >> 4) & 15),
                            WorldEditPlugin.getInstance().getBukkitImplAdapter()
                                .getInternalBlockStateId(state).orElse(0));
                    }
            }
            int palette = blockToPalette[ordinal];
            if (palette == Integer.MAX_VALUE) {
                blockToPalette[ordinal] = palette = num_palette;
                paletteToBlock[num_palette] = ordinal;
                num_palette++;
            }
            blocksCopy[i] = palette;
        }
        num_palette_buffer[0] = num_palette;
        return air;
    }

    public static int createPalette(int layer, int[] blockToPalette, int[] paletteToBlock,
        int[] blocksCopy, int[] num_palette_buffer, Function<Integer, char[]> get, char[] set,
        Map<BlockVector3, Integer> ticking_blocks) {
        int air = 0;
        int num_palette = 0;
        int i = 0;
        int setblocks = 0;
        outer:
        for (; i < 4096; i++) {
            char ordinal = set[i];
            switch (ordinal) {
                case BlockID.__RESERVED__:
                    continue outer;
                case BlockID.AIR:
                case BlockID.CAVE_AIR:
                case BlockID.VOID_AIR:
                    air++;
                    break;
                default:
                    BlockState state = BlockState.getFromOrdinal(ordinal);
                    if (state.getMaterial().isTicksRandomly()) {
                        ticking_blocks.put(BlockVector3.at(i & 15, (i >> 8) & 15, (i >> 4) & 15),
                            WorldEditPlugin.getInstance().getBukkitImplAdapter()
                                .getInternalBlockStateId(state).orElse(0));
                    }
            }
            int palette = blockToPalette[ordinal];
            if (palette == Integer.MAX_VALUE) {
                blockToPalette[ordinal] = palette = num_palette;
                paletteToBlock[num_palette] = ordinal;
                num_palette++;
            }
            blocksCopy[i] = palette;
            setblocks++;
        }
        if (setblocks != 4096) {
            char[] getArr = get.apply(layer);
            for (i = setblocks; i < 4096; i++) {
                char ordinal = set[i];
                switch (ordinal) {
                    case BlockID.__RESERVED__:
                        ordinal = getArr[i];
                        switch (ordinal) {
                            case BlockID.__RESERVED__:
                                ordinal = BlockID.AIR;
                            case BlockID.AIR:
                            case BlockID.CAVE_AIR:
                            case BlockID.VOID_AIR:
                                air++;
                                break;
                            default:
                                BlockState state = BlockState.getFromOrdinal(ordinal);
                                if (state.getMaterial().isTicksRandomly()) {
                                    ticking_blocks
                                        .put(BlockVector3.at(i & 15, (i >> 8) & 15, (i >> 4) & 15),
                                            WorldEditPlugin.getInstance().getBukkitImplAdapter()
                                                .getInternalBlockStateId(state).orElse(0));
                                }
                                set[i] = ordinal;
                        }
                        break;
                    case BlockID.AIR:
                    case BlockID.CAVE_AIR:
                    case BlockID.VOID_AIR:
                        air++;
                }
                int palette = blockToPalette[ordinal];
                if (palette == Integer.MAX_VALUE) {
                    blockToPalette[ordinal] = palette = num_palette;
                    paletteToBlock[num_palette] = ordinal;
                    num_palette++;
                }
                blocksCopy[i] = palette;
            }
        }

        num_palette_buffer[0] = num_palette;
        return air;
    }
}
