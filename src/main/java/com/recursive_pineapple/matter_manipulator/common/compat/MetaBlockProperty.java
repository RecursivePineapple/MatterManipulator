package com.recursive_pineapple.matter_manipulator.common.compat;

import net.minecraft.world.World;

public interface MetaBlockProperty<T> extends BlockProperty<T> {
    
    default boolean needsExisting() {
        return true;
    }

    int getMeta(T value, int existing);

    T getValue(int meta);

    default T getValue(World world, int x, int y, int z) {
        return getValue(world.getBlockMetadata(x, y, z));
    }

    default void setValue(World world, int x, int y, int z, T value) {
        boolean needsExisting = needsExisting();

        int existing = needsExisting ? world.getBlockMetadata(x, y, z) : 0;

        int meta = getMeta(value, existing);

        if (!needsExisting || existing != meta) {
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        }
    }
}
