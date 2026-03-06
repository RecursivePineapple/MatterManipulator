package matter_manipulator.client.rendering.overlays.transform;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class Scale extends Transformation {

    public Vector3f factor;

    public Scale(Vector3f factor) {
        this.factor = factor;
    }

    public Scale(float factor) {
        this(new Vector3f(factor, factor, factor));
    }

    public Scale(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    @Override
    public void apply(Vector3f vec) {
        vec.mul(factor);
    }

    @Override
    public void applyNormal(Vector3f normal) {}

    @Override
    public void apply(Matrix4f mat) {
        mat.scale(factor);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {
        GL11.glScaled(factor.x, factor.y, factor.z);
    }

    @Override
    public Transformation inverse() {
        return new Scale(1 / factor.x, 1 / factor.y, 1 / factor.z);
    }

    @Override
    public Transformation merge(Transformation next) {
        if (next instanceof Scale) return new Scale(new Vector3f(factor).mul(((Scale) next).factor));

        return null;
    }

    private static final Vector3f ONE = new Vector3f(1, 1, 1);

    @Override
    public boolean isRedundant() {
        return factor.equals(ONE, 0.0001f);
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Scale(" + new BigDecimal(factor.x, cont)
            + ", "
            + new BigDecimal(factor.y, cont)
            + ", "
            + new BigDecimal(factor.z, cont)
            + ")";
    }
}
