package com.recursive_pineapple.matter_manipulator.common.uplink;

public enum UplinkStatus {

    OK,
    NO_PLASMA,
    AE_OFFLINE,
    NO_HATCH;

    @Override
    public String toString() {
        return switch (this) {
            case OK -> "Ok";
            case NO_PLASMA -> "Insufficient plasma";
            case AE_OFFLINE -> "Could not connect to the ME system";
            case NO_HATCH -> "Missing ME hatch";
        };
    }
}
