package matter_manipulator.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.theme.WidgetTheme;

/// "borrowed" from angelica and adapted to MUI
/// [Source](https://github.com/GTNewHorizons/Angelica/blob/99a81369f03d649bc4a48f23b026a1dea09c9e26/src/main/java/com/gtnewhorizons/angelica/debug/F3Direction.java)
public class DirectionDrawable implements IDrawable {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (mc.gameSettings.thirdPersonView == 0) {
            GL11.glPushMatrix();

            GL11.glTranslatef(x + width / 2f, y + height / 2f, -90);

            Entity entity = mc.getRenderViewEntity();
            assert entity != null;
            GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * context.getPartialTicks(), 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * context.getPartialTicks(), 0.0F, -1.0F, 0.0F);

            GL11.glScalef(-1.0F, -1.0F, 1.0F);

            renderWorldDirections(Math.min(width / 2, height / 2));

            GL11.glPopMatrix();
        }
    }

    public void renderWorldDirections(double length) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        GL11.glLineWidth(2.0F);
        GL11.glBegin(GL11.GL_LINES);

        // X
        GL11.glColor4f(1f, 0, 0, 1f);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(length, 0, 0);

        // Z
        GL11.glColor4f(0.3f, 0.3f, 1f, 1f);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, length);

        // Y
        GL11.glColor4f(0, 1f, 0, 1f);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, length, 0);

        GL11.glEnd();

        GL11.glLineWidth(1.0F);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
