package matter_manipulator.common.uplink;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;

public enum UplinkState implements IStringSerializable {
    off,
    idle,
    active;

    @Override
    public @NotNull String getName() {
        return name().toLowerCase();
    }
}
