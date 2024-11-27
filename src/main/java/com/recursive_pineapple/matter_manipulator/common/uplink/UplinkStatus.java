package com.recursive_pineapple.matter_manipulator.common.uplink;

public enum UplinkStatus {

    OK,
    NO_PLASMA,
    AE_OFFLINE,
    NO_HATCH;

    @Override
    public String toString() {
        return switch (this) {
            case OK -> "ok";
            case NO_PLASMA -> "insufficient plasma";
            case AE_OFFLINE -> "could not connect to the ME system";
            case NO_HATCH -> "missing ME hatch";
        };
    }
}
