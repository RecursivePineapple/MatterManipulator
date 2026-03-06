package matter_manipulator.client.rendering.overlays.transform;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

public class Translation extends Transformation {

    public Vector3f vec;

    public Translation(Vector3f vec) {
        this.vec = vec;
    }

    public Translation(float x, float y, float z) {
        this(new Vector3f(x, y, z));
    }

    @Override
    public void apply(Vector3f vec) {
        vec.add(this.vec);
    }

    @Override
    public void applyNormal(Vector3f normal) {}

    @Override
    public void apply(Matrix4f mat) {
        mat.translate(vec);
    }

    @Override
    public Transformation at(Vector3f point) {
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {
        GL11.glTranslated(vec.x, vec.y, vec.z);
    }

    @Override
    public Transformation inverse() {
        return new Translation(-vec.x, -vec.y, -vec.z);
    }

    @Override
    public Transformation merge(Transformation next) {
        if (next instanceof Translation) return new Translation(new Vector3f(vec).add(((Translation) next).vec));

        return null;
    }

    @Override
    public boolean isRedundant() {
        return vec.lengthSquared() <= 0.001;
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Translation(" + new BigDecimal(vec.x, cont)
            + ", "
            + new BigDecimal(vec.y, cont)
            + ", "
            + new BigDecimal(vec.z, cont)
            + ")";
    }
}
