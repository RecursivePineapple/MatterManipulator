package matter_manipulator.client.rendering.overlays.transform;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import matter_manipulator.client.rendering.MMRenderUtils;

public abstract class VariableTransformation extends Transformation {

    public Matrix4f mat;

    public VariableTransformation(Matrix4f mat) {
        this.mat = mat;
    }

    @Override
    public void applyNormal(Vector3f normal) {
        apply(normal);
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
}
