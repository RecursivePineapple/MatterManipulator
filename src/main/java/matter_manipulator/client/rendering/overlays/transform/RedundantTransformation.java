package matter_manipulator.client.rendering.overlays.transform;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RedundantTransformation extends Transformation {

    @Override
    public void apply(Vector3f vec) {}

    @Override
    public void apply(Matrix4f mat) {}

    @Override
    public void applyNormal(Vector3f normal) {}

    @Override
    public Transformation at(Vector3f point) {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {}

    @Override
    public Transformation inverse() {
        return this;
    }

    @Override
    public Transformation merge(Transformation next) {
        return next;
    }

    @Override
    public boolean isRedundant() {
        return true;
    }

    @Override
    public String toString() {
        return "Nothing()";
    }
}
