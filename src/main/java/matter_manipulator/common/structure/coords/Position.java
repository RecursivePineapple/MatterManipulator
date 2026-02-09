package matter_manipulator.common.structure.coords;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import org.joml.Vector3f;
import org.joml.Vector3i;

public class Position<C extends CoordinateSystem<C, ?>> extends Vector3i {

    public Position() {

    }

    public Position(int x, int y, int z) {
        super(x, y, z);
    }

    public Position(float x, float y, float z) {
        super(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public Position(double x, double y, double z) {
        super(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
    }

    public Position(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ());
    }

    public Position(Vector3i v) {
        super(v.x, v.y, v.z);
    }

    public Position(Vector3f v) {
        this(v.x, v.y, v.z);
    }

    public Position(Vec3i v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public void offset(Offset<C> offset) {
        this.add(offset.x, offset.y, offset.z);
    }

    public Position<C> copy() {
        return new Position<>(x, y, z);
    }

    public Vector3i toVector3i() {
        return new Vector3i(x, y, z);
    }

    public Vector3f toVector3f() {
        return new Vector3f(x, y, z);
    }

    public Vec3i toVec3() {
        return new Vec3i(x, y, z);
    }

    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }
}
