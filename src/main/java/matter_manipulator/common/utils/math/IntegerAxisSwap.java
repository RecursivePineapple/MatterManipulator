package matter_manipulator.common.utils.math;

import static java.lang.Math.abs;

import net.minecraft.util.EnumFacing;

import org.joml.Vector3f;
import org.joml.Vector3i;

import matter_manipulator.common.utils.MathUtils;

public class IntegerAxisSwap {

    private final Vector3i forFirstAxis;
    private final Vector3i forSecondAxis;
    private final Vector3i forThirdAxis;

    public IntegerAxisSwap(EnumFacing forFirstAxis, EnumFacing forSecondAxis, EnumFacing forThirdAxis) {
        this.forFirstAxis = MathUtils.vi(forFirstAxis);
        this.forSecondAxis = MathUtils.vi(forSecondAxis);
        this.forThirdAxis = MathUtils.vi(forThirdAxis);
        if (abs(this.forFirstAxis.x()) + abs(this.forSecondAxis.x()) + abs(this.forThirdAxis.x()) != 1
                || abs(this.forFirstAxis.y()) + abs(this.forSecondAxis.y()) + abs(this.forThirdAxis.y()) != 1
                || abs(this.forFirstAxis.z()) + abs(this.forSecondAxis.z()) + abs(this.forThirdAxis.z())
                        != 1) {
            throw new IllegalArgumentException(
                    "Axis are overlapping/missing! " + forFirstAxis
                            .name() + " " + forSecondAxis.name() + " " + forThirdAxis.name());
        }
    }

    public Vector3f translate(Vector3f point) {
        return new Vector3f(
            forFirstAxis.x() * point.x() + forFirstAxis.y() * point.y() + forFirstAxis.z() * point.z(),
            forSecondAxis.x() * point.x() + forSecondAxis.y() * point.y() + forSecondAxis.z() * point.z(),
            forThirdAxis.x() * point.x() + forThirdAxis.y() * point.y() + forThirdAxis.z() * point.z());
    }

    public Vector3f inverseTranslate(Vector3f point) {
        return new Vector3f(
            forFirstAxis.x() * point.x() + forSecondAxis.x() * point.y() + forThirdAxis.x() * point.z(),
            forFirstAxis.y() * point.x() + forSecondAxis.y() * point.y() + forThirdAxis.y() * point.z(),
            forFirstAxis.z() * point.x() + forSecondAxis.z() * point.y() + forThirdAxis.z() * point.z());
    }

    public void translate(int[] point, int[] out) {
        out[0] = forFirstAxis.x() * point[0] + forFirstAxis.y() * point[1] + forFirstAxis.z() * point[2];
        out[1] = forSecondAxis.x() * point[0] + forSecondAxis.y() * point[1] + forSecondAxis.z() * point[2];
        out[2] = forThirdAxis.x() * point[0] + forThirdAxis.y() * point[1] + forThirdAxis.z() * point[2];
    }

    public void inverseTranslate(int[] point, int[] out) {
        out[0] = forFirstAxis.x() * point[0] + forSecondAxis.x() * point[1] + forThirdAxis.x() * point[2];
        out[1] = forFirstAxis.y() * point[0] + forSecondAxis.y() * point[1] + forThirdAxis.y() * point[2];
        out[2] = forFirstAxis.z() * point[0] + forSecondAxis.z() * point[1] + forThirdAxis.z() * point[2];
    }

    public void translate(double[] point, double[] out) {
        out[0] = forFirstAxis.x() * point[0] + forFirstAxis.y() * point[1] + forFirstAxis.z() * point[2];
        out[1] = forSecondAxis.x() * point[0] + forSecondAxis.y() * point[1] + forSecondAxis.z() * point[2];
        out[2] = forThirdAxis.x() * point[0] + forThirdAxis.y() * point[1] + forThirdAxis.z() * point[2];
    }

    public void inverseTranslate(double[] point, double[] out) {
        out[0] = forFirstAxis.x() * point[0] + forSecondAxis.x() * point[1] + forThirdAxis.x() * point[2];
        out[1] = forFirstAxis.y() * point[0] + forSecondAxis.y() * point[1] + forThirdAxis.y() * point[2];
        out[2] = forFirstAxis.z() * point[0] + forSecondAxis.z() * point[1] + forThirdAxis.z() * point[2];
    }

    public Vector3i translate(Vector3i point) {
        return new Vector3i(
                forFirstAxis.x() * point.x() + forFirstAxis.y() * point.y() + forFirstAxis.z() * point.z(),
                forSecondAxis.x() * point.x() + forSecondAxis.y() * point.y() + forSecondAxis.z() * point.z(),
                forThirdAxis.x() * point.x() + forThirdAxis.y() * point.y() + forThirdAxis.z() * point.z());
    }

    public Vector3i inverseTranslate(Vector3i point) {
        return new Vector3i(
                forFirstAxis.x() * point.x() + forSecondAxis.x() * point.y() + forThirdAxis.x() * point.z(),
                forFirstAxis.y() * point.x() + forSecondAxis.y() * point.y() + forThirdAxis.y() * point.z(),
                forFirstAxis.z() * point.x() + forSecondAxis.z() * point.y() + forThirdAxis.z() * point.z());
    }
}
