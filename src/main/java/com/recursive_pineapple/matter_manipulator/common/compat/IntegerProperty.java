package com.recursive_pineapple.matter_manipulator.common.compat;

import java.util.List;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

import net.minecraft.world.World;

public interface IntegerProperty extends BlockProperty<Integer> {
    
    public default Integer getValue(World world, int x, int y, int z) {
        return getInt(world, x, y, z);
    }

    public default void setValue(World world, int x, int y, int z, Integer value) {
        setInt(world, x, y, z, value.intValue());
    }

    @Override
    default Integer parse(String text) throws Exception {
        return Integer.parseInt(text);
    }

    public int getInt(World world, int x, int y, int z);

    public void setInt(World world, int x, int y, int z, int value);

    public default BlockProperty<String> map(List<String> values) {
        return new BlockProperty<String>() {
            @Override
            public String getName() {
                return IntegerProperty.this.getName();
            }

            @Override
            public String getValue(World world, int x, int y, int z) {
                return MMUtils.getIndexSafe(values, IntegerProperty.this.getInt(world, x, y, z));
            }

            @Override
            public void setValue(World world, int x, int y, int z, String value) {
                setInt(world, x, y, z, values.indexOf(value));
            }

            @Override
            public String parse(String text) throws Exception {
                return text;
            }
        };
    }

    public static IntegerProperty meta(String name, int mask, int shift) {
        return new IntegerProperty() {
            @Override
            public String getName() {
                return name;
            }
            
            @Override
            public int getInt(World world, int x, int y, int z) {
                return (world.getBlockMetadata(x, y, z) & mask) >> shift;
            }

            @Override
            public void setInt(World world, int x, int y, int z, int value) {
                int meta = world.getBlockMetadata(x, y, z) & ~mask;

                meta |= (value << shift) & mask;

                world.setBlockMetadataWithNotify(x, y, z, meta, 2);
            }
        };
    }
}
