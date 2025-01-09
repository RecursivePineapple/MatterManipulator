package com.recursive_pineapple.matter_manipulator.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;

/**
 * "borrowed" from angelica and adapted to MUI
 * https://github.com/GTNewHorizons/Angelica/blob/99a81369f03d649bc4a48f23b026a1dea09c9e26/src/main/java/com/gtnewhorizons/angelica/debug/F3Direction.java
 */
public class DirectionDrawable implements IDrawable {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void draw(float x, float y, float width, float height, float partialTicks) {
        if (mc.gameSettings.thirdPersonView == 0) {
            GL11.glPushMatrix();

            GL11.glTranslatef(x + width / 2, y + height / 2, -90);

            Entity entity = mc.renderViewEntity;
            GL11.glRotatef(
                entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks,
                1.0F,
                0.0F,
                0.0F);
            GL11.glRotatef(
                entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks,
                0.0F,
                -1.0F,
                0.0F);

            GL11.glScalef(-1.0F, -1.0F, 1.0F);

            renderWorldDirections(Math.min(width / 2, height / 2));

            GL11.glPopMatrix();
        }
    }

    public void renderWorldDirections(double length) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        Tessellator tessellator = Tessellator.instance;

        GL11.glLineWidth(2.0F);
        tessellator.startDrawing(GL11.GL_LINES);

        // X
        tessellator.setColorRGBA(255, 0, 0, 255);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        tessellator.addVertex(length, 0.0D, 0.0D);

        // Z
        tessellator.setColorRGBA(75, 75, 255, 255);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        tessellator.addVertex(0.0D, 0.0D, length);

        // Y
        tessellator.setColorRGBA(0, 255, 0, 255);
        tessellator.addVertex(0.0D, 0.0D, 0.0D);
        tessellator.addVertex(0.0D, length, 0.0D);

        tessellator.draw();

        GL11.glLineWidth(1.0F);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
