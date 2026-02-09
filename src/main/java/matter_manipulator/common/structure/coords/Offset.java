package matter_manipulator.common.structure.coords;

import net.minecraft.util.math.Vec3i;

import org.joml.Vector3i;

public class Offset<C extends CoordinateSystem<C, ?>> extends Vector3i {

    public Offset(int x, int y, int z) {
        super(x, y, z);
    }

    public Offset(Vector3i v) {
        super(v.x, v.y, v.z);
    }

    public Offset(Vec3i v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public Offset<C> copy() {
        return new Offset<>(x, y, z);
    }

}
