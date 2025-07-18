package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UNKNOWN;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizon.structurelib.alignment.enumerable.ExtendedFacing;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMConfig.VoxelAABB;
import com.recursive_pineapple.matter_manipulator.common.networking.Messages;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

/**
 * Represents the rotation and flipping.
 */
public class Transform {

    public boolean flipX, flipY, flipZ;
    public ForgeDirection forward = ExtendedFacing.DEFAULT.getRelativeForwardInWorld(), up = ForgeDirection.UP;

    public transient Matrix4f rotation;

    public static final int FLIP_X = 0b1, FLIP_Y = 0b10, FLIP_Z = 0b100, FORWARD_MASK = 0b111000, FORWARD_SHIFT = 3,
        UP_MASK = 0b111000000, UP_SHIFT = 6;

    public static void sendRotate(ForgeDirection dir, boolean positive) {
        Messages.RotateTransform.sendToServer((dir.ordinal() & 0xFF) | (positive ? 1 : 0) << 8);
    }

    public Matrix4f getRotation() {
        if (rotation != null) return rotation;

        Matrix4f flip = new Matrix4f();
        flip.scale(flipX ? -1 : 1, flipY ? -1 : 1, flipZ ? -1 : 1);

        Matrix4f rot = new Matrix4f().lookAlong(v(forward), v(up));

        return rot.mul(flip);
    }

    public void cacheRotation() {
        rotation = getRotation();
    }

    public void uncacheRotation() {
        rotation = null;
    }

    public ExtendedFacing apply(ExtendedFacing facing) {
        if (facing == null) return null;

        return transform(facing, getRotation());
    }

    public ForgeDirection apply(ForgeDirection dir) {
        if (dir == null) return null;
        if (dir == UNKNOWN) return UNKNOWN;

        return vprime(v(dir).mulTransposeDirection(getRotation()));
    }

    public byte applyBits(int bitmask) {
        if (bitmask == 0) return 0;

        int out = 0;

        ForgeDirection[] validDirections = ForgeDirection.VALID_DIRECTIONS;
        for (int i = 0, validDirectionsLength = validDirections.length; i < validDirectionsLength; i++) {
            ForgeDirection dir = validDirections[i];
            if ((bitmask & dir.flag) != 0) {
                out |= apply(dir).flag;
            }
        }

        return (byte) out;
    }

    public Vector3i apply(Vector3i v) {
        Vector3f v2 = new Vector3f(v).mulTransposeDirection(getRotation());

        v.x = Math.round(v2.x);
        v.y = Math.round(v2.y);
        v.z = Math.round(v2.z);

        return v;
    }

    public VoxelAABB apply(VoxelAABB bb) {
        bb.a.sub(bb.origin);
        apply(bb.a);
        bb.a.add(bb.origin);

        bb.b.sub(bb.origin);
        apply(bb.b);
        bb.b.add(bb.origin);

        return bb;
    }

    /**
     * Rotates this transform.
     *
     * @param dir The axis to rotate around
     * @param amount The amount to rotate (1 = 90 degrees)
     */
    public void rotate(ForgeDirection dir, int amount) {
        rotation = null;
        Matrix4f rot = new Matrix4f().rotate((float) (Math.PI / 2 * amount), v(dir));

        up = transform(up, rot);
        forward = transform(forward, rot);
    }

    @Override
    public String toString() {
        return "Transform [flipX=" + flipX
            + ", flipY="
            + flipY
            + ", flipZ="
            + flipZ
            + ", forward="
            + forward
            + ", up="
            + up
            + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (flipX ? 1231 : 1237);
        result = prime * result + (flipY ? 1231 : 1237);
        result = prime * result + (flipZ ? 1231 : 1237);
        result = prime * result + ((forward == null) ? 0 : forward.hashCode());
        result = prime * result + ((up == null) ? 0 : up.hashCode());
        result = prime * result + ((rotation == null) ? 0 : rotation.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Transform other = (Transform) obj;
        if (flipX != other.flipX) return false;
        if (flipY != other.flipY) return false;
        if (flipZ != other.flipZ) return false;
        if (forward != other.forward) return false;
        if (up != other.up) return false;
        if (rotation == null) {
            if (other.rotation != null) return false;
        } else if (!rotation.equals(other.rotation)) return false;
        return true;
    }

    private static Vector3f v(ForgeDirection dir) {
        return new Vector3f(dir.offsetX, dir.offsetY, dir.offsetZ);
    }

    private static ForgeDirection vprime(Vector3f dir) {
        return switch (dir.maxComponent()) {
            case 0 -> dir.x > 0 ? EAST : WEST;
            case 1 -> dir.y > 0 ? UP : DOWN;
            case 2 -> dir.z > 0 ? SOUTH : NORTH;
            default -> throw new AssertionError();
        };
    }

    /** Unused, but potentially useful */
    public static Matrix4f fromFacing(ExtendedFacing facing) {
        Matrix4f dir = switch (facing.getDirection()) {
            case UP -> new Matrix4f().lookAlong(v(UP), v(NORTH));
            case DOWN -> new Matrix4f().lookAlong(v(DOWN), v(NORTH));
            case NORTH -> new Matrix4f().lookAlong(v(NORTH), v(UP));
            case SOUTH -> new Matrix4f().lookAlong(v(SOUTH), v(UP));
            case EAST -> new Matrix4f().lookAlong(v(EAST), v(UP));
            case WEST -> new Matrix4f().lookAlong(v(WEST), v(UP));
            default -> throw new AssertionError();
        };

        Matrix4f rot = switch (facing.getRotation()) {
            case CLOCKWISE -> new Matrix4f().rotate((float) (Math.PI / 2), v(NORTH));
            case COUNTER_CLOCKWISE -> new Matrix4f().rotate((float) (-Math.PI / 2), v(NORTH));
            case NORMAL -> new Matrix4f();
            case UPSIDE_DOWN -> new Matrix4f().rotate((float) (Math.PI), v(NORTH));
        };

        Matrix4f flip = new Matrix4f();

        if (facing.getFlip().isHorizontallyFlipped()) {
            flip.scale(-1, 1, 1);
        }

        if (facing.getFlip().isVerticallyFliped()) {
            flip.scale(1, -1, 1);
        }

        return rot.mul(flip)
            .mul(dir);
    }

    public static ForgeDirection transform(ForgeDirection dir, Matrix4f transform) {
        return vprime(v(dir).mulTransposeDirection(transform));
    }

    public static ExtendedFacing transform(ExtendedFacing facing, Matrix4f transform) {
        ForgeDirection forward = transform(facing.getRelativeForwardInWorld(), transform);
        ForgeDirection left = transform(facing.getRelativeLeftInWorld(), transform);
        ForgeDirection down = transform(facing.getRelativeDownInWorld(), transform);

        for (ExtendedFacing candidate : ExtendedFacing.getAllWith(forward)) {
            if (candidate.getRelativeLeftInWorld() != left) continue;
            if (candidate.getRelativeDownInWorld() != down) continue;
            if (candidate.getFlip().isVerticallyFliped()) continue;

            return candidate;
        }

        return null;
    }
}
