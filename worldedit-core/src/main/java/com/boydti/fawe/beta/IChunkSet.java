package com.boydti.fawe.beta;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

/**
 * Interface for setting blocks
 */
public interface IChunkSet extends IBlocks, OutputExtent {

    @Override
    boolean setBiome(int x, int y, int z, BiomeType biome);

    @Override
    <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T holder);

    void setBlocks(int layer, char[] data);

    boolean isEmpty();

    @Override
    boolean setTile(int x, int y, int z, CompoundTag tile);

    void setEntity(CompoundTag tag);

    void removeEntity(UUID uuid);

    Set<UUID> getEntityRemoves();

    BiomeType[] getBiomes();

    default boolean hasBiomes() {
        return getBiomes() != null;
    }

    default boolean isFastMode() {
        return false;
    }

    //default to avoid tricky child classes. We only need it in a few cases anyway.
    default void setFastMode(boolean fastMode){}

    @Override
    IChunkSet reset();

    @Nullable
    @Override
    default Operation commit() {
        return null;
    }
}
