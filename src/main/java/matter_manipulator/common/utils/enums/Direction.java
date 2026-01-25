package matter_manipulator.common.utils.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.util.EnumFacing;

import org.joml.Vector3i;

import lombok.Getter;
import matter_manipulator.common.utils.MathUtils;

public enum Direction {

    DOWN(EnumFacing.DOWN),
    UP(EnumFacing.UP),
    NORTH(EnumFacing.NORTH),
    SOUTH(EnumFacing.SOUTH),
    WEST(EnumFacing.WEST),
    EAST(EnumFacing.EAST);

    @Getter
    private final EnumFacing facing;
    @Getter
    private final Vector3i axisVector;
    public static final Direction[] VALUES = values();
    private static final Map<Vector3i, Direction> reverseMap = Arrays.stream(VALUES)
        .collect(Collectors.toMap(d -> d.axisVector, Function.identity()));

    Direction(EnumFacing facing) {
        this.facing = facing;
        axisVector = MathUtils.vi(facing);
    }

    public static Vector3i getAxisVector(EnumFacing facing) {
        return VALUES[facing.ordinal()].axisVector;
    }

    public static Direction getByAxisVector(Vector3i vec3) {
        return reverseMap.get(vec3);
    }
}
