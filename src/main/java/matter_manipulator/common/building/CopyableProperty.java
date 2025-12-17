package matter_manipulator.common.building;

import com.google.common.collect.ImmutableList;

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

    public static final ImmutableList<CopyableProperty> VALUES = ImmutableList.copyOf(values());

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
