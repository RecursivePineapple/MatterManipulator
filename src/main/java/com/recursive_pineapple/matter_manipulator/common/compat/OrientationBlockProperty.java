package com.recursive_pineapple.matter_manipulator.common.compat;

import net.minecraft.world.World;

public interface OrientationBlockProperty extends BlockProperty<Orientation> {
    
    @Override
    default Orientation parse(String text) throws Exception {
        for (Orientation o : Orientation.values()) {
            if (o.name().equalsIgnoreCase(text)) {
                return o;
            }
        }

        throw new Exception("illegal orientation: '" + text + "'");
    }

    public static interface O2M {
        int getMeta(Orientation dir);
    }

    public static interface M2O {
        Orientation getDir(int meta);
    }

    public static OrientationBlockProperty orientation(int mask, O2M o2m, M2O m2o) {
        return new OrientationBlockProperty() {
            @Override
            public String getName() {
                return "orientation";
            }

            @Override
            public Orientation getValue(World world, int x, int y, int z) {
                return m2o.getDir(world.getBlockMetadata(x, y, z) & mask);
            }

            @Override
            public void setValue(World world, int x, int y, int z, Orientation value) {
                int meta = 0;
                if (mask != -1) {
                    meta = world.getBlockMetadata(x, y, z) & ~mask; 
                }

                world.setBlockMetadataWithNotify(x, y, z, o2m.getMeta(value) | meta, 2);
            }
        };
    }

}
