package matter_manipulator.client.rendering;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Comparator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import com.github.bsideup.jabel.Desugar;
import it.unimi.dsi.fastutil.Arrays;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.core.color.RGBColor;

public class MMRenderUtils {

    public static final ImmutableColor BLUE = new RGBColor(38, 153, 192, 100);
    public static final ImmutableColor ORANGE = new RGBColor(192, 128, 38, 100);
    public static final ImmutableColor GREEN = new RGBColor(102, 192, 38, 100);

    private static Vector3d getVecForDir(EnumFacing dir) {
        return new Vector3d(dir.getXOffset(), dir.getYOffset(), dir.getZOffset());
    }

    private static final int RULER_LENGTH = 128;

    public static void drawRulers(EntityPlayer player, BlockPos pos, boolean fromSurface, float partialTickTime) {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        GL11.glPointSize(4);

        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        GL11.glPushMatrix();

        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTickTime;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTickTime;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTickTime;
        GL11.glTranslated(pos.getX() - d0 + 0.5, pos.getY() - d1 + 0.5, pos.getZ() - d2 + 0.5);

        GL11.glBegin(GL11.GL_LINES);

        for (EnumFacing dir : EnumFacing.VALUES) {
            Vector3d delta = getVecForDir(dir);

            if (fromSurface) {
                GL11.glVertex3d(delta.x * 0.5, delta.y * 0.5, delta.z * 0.5);
            } else {
                GL11.glVertex3d(0, 0, 0);
            }

            GL11.glVertex3d(delta.x * RULER_LENGTH, delta.y * RULER_LENGTH, delta.z * RULER_LENGTH);
        }

        GL11.glEnd();

        GL11.glPopMatrix();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Desugar
    public record Quad(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {

        public Vector3f getVertex(int index) {
            return switch (index) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                default -> throw new IllegalArgumentException("Invalid index: " + index);
            };
        }
    }

    private static void loadVertex(ByteBuffer data, int offset, Vector3f out) {
        out.x = data.getFloat(offset);
        out.y = data.getFloat(offset + 4);
        out.z = data.getFloat(offset + 8);
    }

    public static void sortQuads(ByteBuffer data, int quadOffset, int quadCount, VertexFormat format, Comparator<Quad> comparator) {
        Quad quad1 = new Quad(new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f());
        Quad quad2 = new Quad(new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f());

        int vertStride = format.getSize();
        int quadStride = vertStride * 4;

        IntBuffer asInts = data.asIntBuffer();

        // No mult here because 4 verts go into a quad, but each vert is in bytes.
        // int stride = vert stride (bytes) * 4 / 4.
        int quadStrideInt = vertStride;

        int offsetIntoVert = 0;

        for (VertexFormatElement el : format.getElements()) {
            if (el.isPositionElement()) {
                if (el.getType() != EnumType.FLOAT) {
                    throw new IllegalArgumentException("Cannot sort buffer with non-float positions");
                }

                break;
            }

            offsetIntoVert += el.getSize();
        }

        final int offsetIntoVert2 = offsetIntoVert;

        Arrays.quickSort(
            quadOffset,
            quadCount,
            (quadIndex1, quadIndex2) -> {
                int a = quadIndex1 * quadStride;
                int b = quadIndex2 * quadStride;

                for (int i = 0; i < 4; i++) {
                    loadVertex(data, a + vertStride * i + offsetIntoVert2, quad1.getVertex(i));
                    loadVertex(data, b + vertStride * i + offsetIntoVert2, quad2.getVertex(i));
                }

                return comparator.compare(quad1, quad2);
            },
            (quadIndex1, quadIndex2) -> {
                quadIndex1 *= quadStrideInt;
                quadIndex2 *= quadStrideInt;

                for (int i = 0; i < quadStrideInt; i++) {
                    int a = asInts.get(quadIndex1 + i);
                    int b = asInts.get(quadIndex2 + i);

                    asInts.put(quadIndex1 + i, b);
                    asInts.put(quadIndex2 + i, a);
                }
            });
    }

    public static Vector3d getPlayerPosition(double partialTicks) {
        Entity player = Minecraft.getMinecraft().getRenderViewEntity();
        assert player != null;

        double xd = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double yd = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double zd = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        return new Vector3d(xd, yd, zd);
    }
}
