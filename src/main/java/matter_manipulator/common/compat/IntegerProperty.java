package matter_manipulator.common.compat;

import java.util.List;

import net.minecraft.world.World;

import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

public interface IntegerProperty extends BlockProperty<Integer> {

    default Integer getValue(World world, int x, int y, int z) {
        return getInt(world, x, y, z);
    }

    default void setValue(World world, int x, int y, int z, Integer value) {
        setInt(world, x, y, z, value);
    }

    @Override
    default Integer parse(String text) {
        return Integer.parseInt(text);
    }

    int getInt(World world, int x, int y, int z);

    void setInt(World world, int x, int y, int z, int value);

    default BlockProperty<String> map(List<String> values) {
        return new BlockProperty<>() {

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
            public String parse(String text) {
                return text;
            }
        };
    }

    static IntegerProperty meta(String name, int mask, int shift) {
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
