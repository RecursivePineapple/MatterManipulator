package matter_manipulator.client.rendering.overlays.transform;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import matter_manipulator.client.rendering.MMRenderUtils;

public class MatrixTransformation extends Transformation {

    public Matrix4f mat;

    public MatrixTransformation(Matrix4f mat) {
        this.mat = mat;
    }

    @Override
    public void applyNormal(Vector3f normal) {
        mat.transformDirection(normal);
    }

    @Override
    public void apply(Matrix4f mat) {
        mat.mul(this.mat);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {
        MMRenderUtils.glMultMatrix(mat);
    }

    @Override
    public void apply(Vector3f vec) {
        mat.transformPosition(vec);
    }

    @Override
    public Transformation inverse() {
        return new MatrixTransformation(new Matrix4f(mat).invert());
    }
}
