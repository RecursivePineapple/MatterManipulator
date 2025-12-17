package matter_manipulator.common.compat;

import java.util.Objects;

import net.minecraft.world.World;

public interface BlockProperty<TValue> {

    default boolean appliesTo(Object obj) {
        return true;
    }

    String getName();

    TValue getValue(World world, int x, int y, int z);

    void setValue(World world, int x, int y, int z, TValue value);

    default void setValueFromText(World world, int x, int y, int z, String text) throws Exception {
        setValue(world, x, y, z, parse(text));
    }

    TValue parse(String text) throws Exception;

    default String getValueAsString(World world, int x, int y, int z) {
        return stringify(getValue(world, x, y, z));
    }

    default String stringify(TValue value) {
        return Objects.toString(value);
    }
}
