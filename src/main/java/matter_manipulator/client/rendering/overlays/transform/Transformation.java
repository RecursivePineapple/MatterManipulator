package matter_manipulator.client.rendering.overlays.transform;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Abstract supertype for any 3D vector transformation
 */
public abstract class Transformation extends ITransformation<Vector3f, Transformation> {

    /**
     * Applies this transformation to a normal (doesn't translate)
     *
     * @param normal The normal to transform
     */
    public abstract void applyNormal(Vector3f normal);

    /**
     * Applies this transformation to a matrix as a multiplication on the right hand side.
     *
     * @param mat The matrix to combine this transformation with
     */
    public abstract void apply(Matrix4f mat);

    public Transformation at(Vector3f point) {
        return new TransformationList(
            new Translation(-point.x, -point.y, -point.z),
            this,
            new Translation(point.x, point.y, point.z));
    }

    public TransformationList with(Transformation t) {
        return new TransformationList(this, t);
    }

    @SideOnly(Side.CLIENT)
    public abstract void glApply();
}
