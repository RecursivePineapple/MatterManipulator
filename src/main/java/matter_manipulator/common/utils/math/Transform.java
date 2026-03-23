package matter_manipulator.common.utils.math;

import net.minecraft.util.EnumFacing;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import matter_manipulator.common.utils.MathUtils;
import matter_manipulator.common.utils.enums.ExtendedFacing;

/**
 * Represents the rotation and flipping.
 */
@EqualsAndHashCode
public class Transform implements Cloneable {

    public boolean flipX, flipY, flipZ;
    public EnumFacing forward = EnumFacing.NORTH, up = EnumFacing.UP;

    public transient Matrix4f rotation;

    public Matrix4f getRotation() {
        if (rotation != null) return rotation;

        Matrix4f flip = new Matrix4f();
        flip.scale(flipX ? -1 : 1, flipY ? -1 : 1, flipZ ? -1 : 1);

        Matrix4f rot = new Matrix4f().lookAlong(MathUtils.v(forward), MathUtils.v(up));

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

    public EnumFacing apply(EnumFacing dir) {
        if (dir == null) return null;

        return MathUtils.vprime(MathUtils.v(dir).mulTransposeDirection(getRotation()));
    }

    public byte applyBits(int bitmask) {
        if (bitmask == 0) return 0;

        int out = 0;

        for (EnumFacing facing : EnumFacing.VALUES) {
            if ((bitmask & 0b1 << facing.getIndex()) != 0) {
                out |= 0b1 << apply(facing).getIndex();
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
    public void rotate(EnumFacing dir, int amount) {
        rotation = null;
        Matrix4f rot = new Matrix4f().rotate((float) (Math.PI / 2 * amount), MathUtils.v(dir));

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

    @SneakyThrows
    @Override
    public Transform clone() {
        Transform clone = (Transform) super.clone();

        if (clone.rotation != null) clone.rotation = new Matrix4f(clone.rotation);

        return clone;
    }

    public static Matrix4f fromFacing(ExtendedFacing facing) {
        Transform transform = new Transform();

        transform.flipZ = true;
        transform.forward = facing.getRelativeForwardInWorld();
        transform.up = switch (transform.forward) {
            case UP -> EnumFacing.SOUTH;
            case DOWN -> EnumFacing.NORTH;
            default -> EnumFacing.UP;
        };

        return transform.getRotation();
    }

    public static EnumFacing transform(EnumFacing dir, Matrix4f transform) {
        return MathUtils.vprime(MathUtils.v(dir).mulTransposeDirection(transform));
    }

    public static ExtendedFacing transform(ExtendedFacing facing, Matrix4f transform) {
        EnumFacing forward = transform(facing.getRelativeForwardInWorld(), transform);
        EnumFacing left = transform(facing.getRelativeLeftInWorld(), transform);
        EnumFacing down = transform(facing.getRelativeDownInWorld(), transform);

        for (ExtendedFacing candidate : ExtendedFacing.getAllWith(forward)) {
            if (candidate.getRelativeLeftInWorld() != left) continue;
            if (candidate.getRelativeDownInWorld() != down) continue;
            if (candidate.getFlip().isVerticallyFlipped()) continue;

            return candidate;
        }

        return null;
    }
}
