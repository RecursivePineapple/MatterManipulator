package matter_manipulator.client.rendering.overlays.transform;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import matter_manipulator.client.rendering.MMRenderUtils;

public class Rotation extends Transformation {

    /**
     * Clockwise pi/2 about y looking down
     */
    public static Transformation[] quarterRotations = new Transformation[] { new RedundantTransformation(),
        new VariableTransformation(new Matrix4f(0, 0, -1, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                float d1 = vec.x;
                float d2 = vec.z;
                vec.x = -d2;
                vec.z = d1;
            }

            @Override
            public Transformation inverse() {
                return quarterRotations[3];
            }
        }, new VariableTransformation(new Matrix4f(-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1)) {

        @Override
        public void apply(Vector3f vec) {
            vec.x = -vec.x;
            vec.z = -vec.z;
        }

        @Override
        public Transformation inverse() {
            return this;
        }
    }, new VariableTransformation(new Matrix4f(0, 0, 1, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 1)) {

        @Override
        public void apply(Vector3f vec) {
            float d1 = vec.x;
            float d2 = vec.z;
            vec.x = d2;
            vec.z = -d1;
        }

        @Override
        public Transformation inverse() {
            return quarterRotations[1];
        }
    } };

    public static Transformation[] sideRotations = new Transformation[] {
        new RedundantTransformation(),
        new VariableTransformation(new Matrix4f(1, 0, 0, 0, 0, -1, 0, 0, 0, 0, -1, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                vec.y = -vec.y;
                vec.z = -vec.z;
            }

            @Override
            public Transformation inverse() {
                return this;
            }
        },
        new VariableTransformation(new Matrix4f(1, 0, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                float d1 = vec.y;
                float d2 = vec.z;
                vec.y = d2;
                vec.z = -d1;
            }

            @Override
            public Transformation inverse() {
                return sideRotations[2];
            }
        },
        new VariableTransformation(new Matrix4f(1, 0, 0, 0, 0, 0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                float d1 = vec.y;
                float d2 = vec.z;
                vec.y = -d2;
                vec.z = d1;
            }

            @Override
            public Transformation inverse() {
                return sideRotations[3];
            }
        },
        new VariableTransformation(new Matrix4f(0, -1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                float d0 = vec.x;
                float d1 = vec.y;
                vec.x = -d1;
                vec.y = d0;
            }

            @Override
            public Transformation inverse() {
                return sideRotations[4];
            }
        },
        new VariableTransformation(new Matrix4f(0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1)) {

            @Override
            public void apply(Vector3f vec) {
                float d0 = vec.x;
                float d1 = vec.y;
                vec.x = d1;
                vec.y = -d0;
            }

            @Override
            public Transformation inverse() {
                return sideRotations[5];
            }
        }
    };

    public static Vector3f[] axes = new Vector3f[] { new Vector3f(0, -1, 0), new Vector3f(0, 1, 0),
        new Vector3f(0, 0, -1), new Vector3f(0, 0, 1), new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0) };

    public static int[] sideRotMap = new int[] { 3, 4, 2, 5, 3, 5, 2, 4, 1, 5, 0, 4, 1, 4, 0, 5, 1, 2, 0, 3, 1, 3, 0,
        2 };

    public static int[] rotSideMap = new int[] { -1, -1, 2, 0, 1, 3, -1, -1, 2, 0, 3, 1, 2, 0, -1, -1, 3, 1, 2, 0, -1,
        -1, 1, 3, 2, 0, 1, 3, -1, -1, 2, 0, 3, 1, -1, -1 };

    /**
     * Rotate pi/2 * this offset for [side] about y axis before rotating to the side for the rotation indicies to line
     * up
     */
    public static int[] sideRotOffsets = new int[] { 0, 2, 2, 0, 1, 3 };

    public static int rotateSide(int s, int r) {
        return sideRotMap[s << 2 | r];
    }

    /**
     * Reverse of rotateSide
     */
    public static int rotationTo(int s1, int s2) {
        if ((s1 & 6) == (s2 & 6)) throw new IllegalArgumentException("Faces " + s1 + " and " + s2 + " are opposites");
        return rotSideMap[s1 * 6 + s2];
    }

    /**
     * @param player The placing player, used for obtaining the look vector
     * @param side   The side of the block being placed on
     * @return The rotation for the face == side^1
     */
    public static int getSidedRotation(EntityPlayer player, int side) {
        var look2 = player.getLook(1);
        Vector3f look = new Vector3f((float) look2.x, (float) look2.y, (float) look2.z);

        float max = 0;
        int maxr = 0;
        for (int r = 0; r < 4; r++) {
            Vector3f axis = Rotation.axes[rotateSide(side ^ 1, r)];
            float d = MMRenderUtils.project(look, axis);
            if (d > max) {
                max = d;
                maxr = r;
            }
        }
        return maxr;
    }

    /**
     * @return The rotation quat for side 0 and rotation 0 to side s with rotation r
     */
    public static Transformation sideOrientation(int s, int r) {
        return quarterRotations[(r + sideRotOffsets[s]) % 4].with(sideRotations[s]);
    }

    /**
     * @param entity The placing entity, used for obtaining the look vector
     * @return The side towards which the entity is most directly looking.
     */
    public static int getSideFromLookAngle(EntityLivingBase entity) {
        var look2 = entity.getLook(1);
        Vector3f look = new Vector3f((float) look2.x, (float) look2.y, (float) look2.z);

        float max = 0;
        int maxs = 0;
        for (int s = 0; s < 6; s++) {
            float d = MMRenderUtils.project(look, axes[s]);
            if (d > max) {
                max = d;
                maxs = s;
            }
        }
        return maxs;
    }

    public float angle;
    public Vector3f axis;

    private Quaternionf quat;

    public Rotation(float angle, Vector3f axis) {
        this.angle = angle;
        this.axis = axis;
    }

    public Rotation(float angle, float x, float y, float z) {
        this(angle, new Vector3f(x, y, z));
    }

    public Rotation(Quaternionf quat) {
        this.quat = quat;

        angle = (float) Math.acos(quat.w) * 2;
        if (angle == 0) {
            axis = new Vector3f(0, 1, 0);
        } else {
            float sa = (float) Math.sin(angle * 0.5f);
            axis = new Vector3f(quat.x / sa, quat.y / sa, quat.z / sa);
        }
    }

    @Override
    public void apply(Vector3f vec) {
        if (quat == null) quat = new Quaternionf(new AxisAngle4f(angle, axis));

        vec.rotate(quat);
    }

    @Override
    public void applyNormal(Vector3f normal) {
        apply(normal);
    }

    @Override
    public void apply(Matrix4f mat) {
        mat.rotate(angle, axis);
    }

    public Quaternionf toQuat() {
        if (quat == null) quat = new Quaternionf(new AxisAngle4f(angle, axis));

        return quat;
    }

    private static final float RAD2DEG = (float) (180d / Math.PI);

    @Override
    @SideOnly(Side.CLIENT)
    public void glApply() {
        GL11.glRotatef(angle * RAD2DEG, axis.x, axis.y, axis.z);
    }

    @Override
    public Transformation inverse() {
        return new Rotation(-angle, axis);
    }

    @Override
    public Transformation merge(Transformation next) {
        if (next instanceof Rotation r) {
            if (r.axis.equals(axis, 0.0001f)) return new Rotation(angle + r.angle, axis);

            return new Rotation(new Quaternionf(toQuat()).mul(r.toQuat()));
        }

        return null;
    }

    @Override
    public boolean isRedundant() {
        return -1E-5 <= angle && angle <= 1E-5;
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Rotation(" + new BigDecimal(angle, cont) + ", " + new BigDecimal(axis.x, cont) + ", " + new BigDecimal(
            axis.y,
            cont) + ", " + new BigDecimal(axis.z, cont) + ")";
    }
}
