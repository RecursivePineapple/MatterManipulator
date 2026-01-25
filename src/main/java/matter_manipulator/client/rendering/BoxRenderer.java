package matter_manipulator.client.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import matter_manipulator.client.rendering.shader.ShaderProgram;
import matter_manipulator.client.rendering.vbo.StreamingVertexBuffer;
import matter_manipulator.client.rendering.vertex.QuadCentroidComparator;
import matter_manipulator.common.utils.Mods;
import matter_manipulator.core.color.ImmutableColor;

public class BoxRenderer {

    public static final BoxRenderer INSTANCE = new BoxRenderer();

    private final ShaderProgram program;
    private final int time_location;

    private final WorldVertexBufferUploader uploader = new WorldVertexBufferUploader();
    private final BufferBuilder buffer = new BufferBuilder(8192);
    private final StreamingVertexBuffer vbo = new StreamingVertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR, GL11.GL_QUADS);

    private boolean drawing = false;

    public BoxRenderer() {
        program = new ShaderProgram(
            Mods.MatterManipulator.resourceDomain,
            "shaders/fancybox.vert.glsl",
            "shaders/fancybox.frag.glsl"
        );

        time_location = program.getUniformLocation("time");
    }

    /**
     * Starts rendering fancy boxes. Should only be called once per frame, to allow quad sorting.
     */
    public void start() {
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        drawing = true;
    }

    /**
     * Draws a fancy box around an AABB.
     */
    public void drawAround(float partialTickTime, AxisAlignedBB aabb, ImmutableColor color) {
        if (!drawing) throw new IllegalStateException("Cannot draw box: BoxRenderer is not drawing");

        EntityPlayer player = Minecraft.getMinecraft().player;
        double pX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
        double pY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
        double pZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;

        buffer.setTranslation(-pX + aabb.minX, -pY + aabb.minY, -pZ + aabb.minZ);

        float dX = (float) (aabb.maxX - aabb.minX);
        float dY = (float) (aabb.maxY - aabb.minY);
        float dZ = (float) (aabb.maxZ - aabb.minZ);

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();

        // spotless:off
        // bottom face
        buffer.pos(0, 0, 0).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, 0).tex(dX, 0).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, dZ).tex(dX, dZ).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, dZ).tex(0, dZ).color(r, g, b, a).endVertex();

        // top face
        buffer.pos(0, dY, 0).tex(dY + 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, dY, dZ).tex(dY + 0, dZ).color(r, g, b, a).endVertex();
        buffer.pos(dX, dY, dZ).tex(dY + dX, dZ).color(r, g, b, a).endVertex();
        buffer.pos(dX, dY, 0).tex(dY + dX, 0).color(r, g, b, a).endVertex();

        // west face
        buffer.pos(0, 0, 0).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, 0, dZ).tex(0, dZ).color(r, g, b, a).endVertex();
        buffer.pos(0, dY, dZ).tex(dY, dZ).color(r, g, b, a).endVertex();
        buffer.pos(0, dY, 0).tex(dY, 0).color(r, g, b, a).endVertex();

        // east face
        buffer.pos(dX, dY, dZ).tex(dX + dY, dZ).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, dZ).tex(dX + 0, dZ).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, 0).tex(dX + 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(dX, dY, 0).tex(dX + dY, 0).color(r, g, b, a).endVertex();

        // north face
        buffer.pos(0, 0, 0).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, 0).tex(dX, 0).color(r, g, b, a).endVertex();
        buffer.pos(dX, dY, 0).tex(dX, dY).color(r, g, b, a).endVertex();
        buffer.pos(0, dY, 0).tex(0, dY).color(r, g, b, a).endVertex();

        // south face
        buffer.pos(0, 0, dZ).tex(dZ + 0, 0).color(r, g, b, a).endVertex();
        buffer.pos(0, dY, dZ).tex(dZ + 0, dY).color(r, g, b, a).endVertex();
        buffer.pos(dX, dY, dZ).tex(dZ + dX, dY).color(r, g, b, a).endVertex();
        buffer.pos(dX, 0, dZ).tex(dZ + dX, 0).color(r, g, b, a).endVertex();
        // spotless:on
    }

    /**
     * Actually draws the stored boxes.
     */
    public void finish() {
        drawing = false;

        buffer.finishDrawing();

        MMRenderUtils.sortQuads(buffer.getByteBuffer(), 0, buffer.getVertexCount() / 4, buffer.getVertexFormat(), new QuadCentroidComparator());

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);

        program.use();

        GL20.glUniform1f(time_location, (float) (System.currentTimeMillis() % 2500) / 1000f);

        uploader.draw(buffer);

        ShaderProgram.clear();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
