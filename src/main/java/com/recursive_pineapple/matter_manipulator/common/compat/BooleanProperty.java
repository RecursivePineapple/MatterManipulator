package com.recursive_pineapple.matter_manipulator.common.compat;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public interface BooleanProperty extends BlockProperty<Boolean> {
    
    public default Boolean getValue(World world, int x, int y, int z) {
        return getBoolean(world, x, y, z);
    }

    public default void setValue(World world, int x, int y, int z, Boolean value) {
        setBoolean(world, x, y, z, value.booleanValue());
    }

    @Override
    default Boolean parse(String text) throws Exception {
        return Boolean.parseBoolean(text);
    }

    public boolean getBoolean(World world, int x, int y, int z);

    public void setBoolean(World world, int x, int y, int z, boolean value);

    public static BooleanProperty range(String name, int offset, int size) {
        return new BooleanProperty() {
            @Override
            public String getName() {
                return name;
            }
    
            @Override
            public boolean getBoolean(World world, int x, int y, int z) {
                int meta = world.getBlockMetadata(x, y, z);
                return meta >= offset && meta < offset + size;
            }
    
            @Override
            public void setBoolean(World world, int x, int y, int z, boolean value) {
                int meta = world.getBlockMetadata(x, y, z);
    
                if (meta >= offset && meta < offset + size) meta -= offset;
    
                if (value) meta += offset;
    
                world.setBlockMetadataWithNotify(x, y, z, meta, 2);
            }
        };
    }

    public static FlagBooleanProperty flag(String name, int flag) {
        return new FlagBooleanProperty(name, flag);
    }

    public static class FlagBooleanProperty implements BooleanProperty {
        public final String name;
        public final int flag;

        public FlagBooleanProperty(String name, int flag) {
            this.name = name;
            this.flag = flag;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean getBoolean(World world, int x, int y, int z) {
            int meta = world.getBlockMetadata(x, y, z);
            return (meta & flag) == flag;
        }

        @Override
        public void setBoolean(World world, int x, int y, int z, boolean value) {
            int meta = world.getBlockMetadata(x, y, z);
   
            meta &= ~flag;
            if (value) meta |= flag;
   
            world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        }
    }

    public static BooleanProperty blocks(String name, Block inactive, Block active) {
        return new BooleanProperty() {
            @Override
            public String getName() { return name; }

            @Override
            public boolean getBoolean(World world, int x, int y, int z) {
                return world.getBlock(x, y, z) == active;
            }

            @Override
            public void setBoolean(World world, int x, int y, int z, boolean value) {
                world.setBlock(x, y, z, value ? active : inactive, world.getBlockMetadata(x, y, z), 2);
            }
        };
    }
}
