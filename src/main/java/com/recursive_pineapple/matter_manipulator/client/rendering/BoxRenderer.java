package com.recursive_pineapple.matter_manipulator.client.rendering;

import java.nio.ByteBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import com.gtnewhorizon.gtnhlib.client.renderer.CapturingTessellator;
import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.quad.QuadView;
import com.gtnewhorizon.gtnhlib.client.renderer.shader.ShaderProgram;
import com.gtnewhorizon.gtnhlib.client.renderer.vbo.VertexBuffer;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.DefaultVertexFormat;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class BoxRenderer {

    public static final BoxRenderer INSTANCE = new BoxRenderer();

    private final ShaderProgram program;
    private final int time_location;

    public BoxRenderer() {
        program = new ShaderProgram(
            Mods.MatterManipulator.resourceDomain,
            "shaders/fancybox.vert.glsl",
            "shaders/fancybox.frag.glsl"
        );

        time_location = program.getUniformLocation("time");
    }

    private CapturingTessellator tes;

    /**
     * Starts rendering fancy boxes. Should only be called once per frame, to allow quad sorting.
     *
     * @param partialTickTime
     */
    public void start(double partialTickTime) {
        TessellatorManager.startCapturing();

        tes = (CapturingTessellator) TessellatorManager.get();

        tes.startDrawing(GL11.GL_QUADS);

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;

        tes.setTranslation(-d0, -d1, -d2);
    }

    /**
     * Draws a fancy box around an AABB.
     */
    public void drawAround(AxisAlignedBB aabb, Vector3f colour) {
        aabb = aabb.copy()
            .expand(0.01, 0.01, 0.01);

        tes.setColorRGBA_F(colour.x, colour.y, colour.z, 0.25f);

        tes.storeTranslation();
        tes.addTranslation((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ);

        float dX = (float) (aabb.maxX - aabb.minX);
        float dY = (float) (aabb.maxY - aabb.minY);
        float dZ = (float) (aabb.maxZ - aabb.minZ);

        // spotless:off
        // bottom face
        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(dX, 0, 0, dX, 0);
        tes.addVertexWithUV(dX, 0, dZ, dX, dZ);
        tes.addVertexWithUV(0, 0, dZ, 0, dZ);

        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(0, 0, dZ, 0, dZ);
        tes.addVertexWithUV(dX, 0, dZ, dX, dZ);
        tes.addVertexWithUV(dX, 0, 0, dX, 0);

        // top face
        tes.addVertexWithUV(0, dY, 0, dY + 0, 0);
        tes.addVertexWithUV(0, dY, dZ, dY + 0, dZ);
        tes.addVertexWithUV(dX, dY, dZ, dY + dX, dZ);
        tes.addVertexWithUV(dX, dY, 0, dY + dX, 0);

        tes.addVertexWithUV(0, dY, 0, dY + 0, 0);
        tes.addVertexWithUV(dX, dY, 0, dY + dX, 0);
        tes.addVertexWithUV(dX, dY, dZ, dY + dX, dZ);
        tes.addVertexWithUV(0, dY, dZ, dY + 0, dZ);

        // west face
        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(0, 0, dZ, 0, dZ);
        tes.addVertexWithUV(0, dY, dZ, dY, dZ);
        tes.addVertexWithUV(0, dY, 0, dY, 0);

        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(0, dY, 0, dY, 0);
        tes.addVertexWithUV(0, dY, dZ, dY, dZ);
        tes.addVertexWithUV(0, 0, dZ, 0, dZ);

        // east face
        tes.addVertexWithUV(dX, dY, dZ, dX + dY, dZ);
        tes.addVertexWithUV(dX, 0, dZ, dX + 0, dZ);
        tes.addVertexWithUV(dX, 0, 0, dX + 0, 0);
        tes.addVertexWithUV(dX, dY, 0, dX + dY, 0);

        tes.addVertexWithUV(dX, dY, dZ, dX + dY, dZ);
        tes.addVertexWithUV(dX, dY, 0, dX + dY, 0);
        tes.addVertexWithUV(dX, 0, 0, dX + 0, 0);
        tes.addVertexWithUV(dX, 0, dZ, dX + 0, dZ);

        // north face
        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(dX, 0, 0, dX, 0);
        tes.addVertexWithUV(dX, dY, 0, dX, dY);
        tes.addVertexWithUV(0, dY, 0, 0, dY);

        tes.addVertexWithUV(0, 0, 0, 0, 0);
        tes.addVertexWithUV(0, dY, 0, 0, dY);
        tes.addVertexWithUV(dX, dY, 0, dX, dY);
        tes.addVertexWithUV(dX, 0, 0, dX, 0);

        // south face
        tes.addVertexWithUV(0, 0, dZ, dZ + 0, 0);
        tes.addVertexWithUV(0, dY, dZ, dZ + 0, dY);
        tes.addVertexWithUV(dX, dY, dZ, dZ + dX, dY);
        tes.addVertexWithUV(dX, 0, dZ, dZ + dX, 0);

        tes.addVertexWithUV(0, 0, dZ, dZ + 0, 0);
        tes.addVertexWithUV(dX, 0, dZ, dZ + dX, 0);
        tes.addVertexWithUV(dX, dY, dZ, dZ + dX, dY);
        tes.addVertexWithUV(0, dY, dZ, dZ + 0, dY);
        // spotless:on

        tes.restoreTranslation();
    }

    /**
     * Actually draws the stored boxes.
     */
    public void finish() {
        List<QuadView> quads = TessellatorManager.stopCapturingToPooledQuads();

        QuadViewComparator quadSorter = new QuadViewComparator();
        quads.sort(quadSorter);

        ByteBuffer bytes = CapturingTessellator.quadsToBuffer(quads, DefaultVertexFormat.POSITION_COLOR_TEXTURE);

        tes.clearQuads();

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        program.use();

        // this should only be done once a frame, but there aren't any side effects from calling it more
        GL20.glUniform1f(time_location, (((float) (System.currentTimeMillis() % 2500)) / 1000f));

        try (VertexBuffer buffer = new VertexBuffer(DefaultVertexFormat.POSITION_COLOR_TEXTURE, GL11.GL_QUADS);) {
            buffer.upload(bytes);
            buffer.render();
        }

        ShaderProgram.clear();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
}
