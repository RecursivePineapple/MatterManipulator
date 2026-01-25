package matter_manipulator.common.utils.math;

import net.minecraft.util.math.AxisAlignedBB;

import org.joml.Vector3i;

public class VoxelAABB {

    public Vector3i origin, a, b;

    public VoxelAABB() {
        origin = new Vector3i();
        a = new Vector3i();
        b = new Vector3i();
    }

    public VoxelAABB(Vector3i a, Vector3i b) {
        this.origin = new Vector3i(a);
        this.a = new Vector3i(a);
        this.b = new Vector3i(b);
    }

    public Vector3i min() {
        return new Vector3i(a).min(b);
    }

    public Vector3i max() {
        return new Vector3i(a).max(b);
    }

    public VoxelAABB union(Vector3i v) {
        Vector3i min = min(), max = max();

        a.set(v)
            .min(min);
        b.set(v)
            .max(max);

        return this;
    }

    public VoxelAABB union(VoxelAABB other) {
        Vector3i min = min(), max = max();

        a.set(min)
            .min(other.min());
        b.set(max)
            .max(other.max());

        return this;
    }

    public VoxelAABB moveOrigin(Vector3i newOrigin) {
        b.sub(origin)
            .add(newOrigin);
        a.sub(origin)
            .add(newOrigin);
        origin.set(newOrigin);

        return this;
    }

    public VoxelAABB scale(int x, int y, int z) {
        int dirX = b.x < a.x ? -1 : 1;
        int dirY = b.y < a.y ? -1 : 1;
        int dirZ = b.z < a.z ? -1 : 1;

        Vector3i size = size();

        size.mul(x, y, z);
        size.mul(dirX, dirY, dirZ);
        size.add(origin);

        VoxelAABB other = clone();

        other.moveOrigin(size);

        union(other);

        return this;
    }

    public VoxelAABB clone() {
        VoxelAABB dup = new VoxelAABB();
        dup.origin = new Vector3i(origin);
        dup.a = new Vector3i(a);
        dup.b = new Vector3i(b);
        return dup;
    }

    public Vector3i span() {
        Vector3i min = min(), max = max();

        return new Vector3i(max.x - min.x, max.y - min.y, max.z - min.z);
    }

    public Vector3i size() {
        Vector3i min = min(), max = max();

        return new Vector3i(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
    }

    public AxisAlignedBB toBoundingBox() {
        Vector3i min = min(), max = max();

        return new AxisAlignedBB(min.x, min.y, min.z, max.x + 1, max.y + 1, max.z + 1);
    }

    public String describe() {
        Vector3i size = size();

        return String.format(
            "dX=%,d dY=%,d dZ=%,d V=%,d",
            Math.abs(size.x),
            Math.abs(size.y),
            Math.abs(size.z),
            size.x * size.y * size.z);
    }
}
