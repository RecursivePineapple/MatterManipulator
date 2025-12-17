package matter_manipulator.common.compat;

import net.minecraft.world.World;

public interface FloatProperty extends BlockProperty<Float> {

    default Float getValue(World world, int x, int y, int z) {
        return getFloat(world, x, y, z);
    }

    default void setValue(World world, int x, int y, int z, Float value) {
        setFloat(world, x, y, z, value);
    }

    @Override
    default Float parse(String text) throws Exception {
        return Float.parseFloat(text);
    }

    float getFloat(World world, int x, int y, int z);

    void setFloat(World world, int x, int y, int z, float value);
}
