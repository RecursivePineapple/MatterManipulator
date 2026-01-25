package matter_manipulator.core.building;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;

public enum CopyableProperty {

    FACING,
    FORWARD,
    UP,
    LEFT,
    TOP,
    ROTATION,
    MODE,
    TEXT,
    ORIENTATION,
    DELAY,
    INVERTED,
    COLOR,
    ROTATION_STATE,
    ;

    public static final CopyableProperty[] VALUES = values();

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    @SuppressWarnings("rawtypes")
    private static final IProperty[] ZERO_LENGTH_PROP_ARRAY = new IProperty[0];

    /// Removes any properties that aren't explicitly copyable. Third party integrations can copy those if they want to.
    @SuppressWarnings("rawtypes")
    public static IBlockState sanitizeBlockState(IBlockState original) {
        IBlockState def = original.getBlock().getDefaultState();

        IProperty[] properties = original.getPropertyKeys().toArray(ZERO_LENGTH_PROP_ARRAY);

        outer: for (CopyableProperty copyable : VALUES) {
            for (IProperty prop : properties) {
                if (prop.getName().equals(copyable.toString())) {
                    def = def.withProperty(prop, original.getValue(prop));
                    continue outer;
                }
            }
        }

        return def;
    }
}
