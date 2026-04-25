package matter_manipulator.client.rendering.overlays;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.client.rendering.overlays.transform.Rotation;
import matter_manipulator.client.rendering.overlays.transform.Scale;
import matter_manipulator.client.rendering.overlays.transform.Transformation;
import matter_manipulator.client.rendering.overlays.transform.Translation;
import matter_manipulator.common.items.MMItemList;
import matter_manipulator.common.structure.IAlignment;
import matter_manipulator.common.structure.IAlignmentProvider;

@EventBusSubscriber(Side.CLIENT)
public class WrenchOverlayRenderer {

    private static final int ROTATION_MARKER_RESOLUTION = 120;

    private static final int[][] GRID_SWITCH_TABLE = new int[][] {
        { 0, 5, 3, 1, 2, 4 },
        { 5, 0, 1, 3, 2, 4 },
        { 1, 3, 0, 5, 2, 4 },
        { 3, 1, 5, 0, 2, 4 },
        { 4, 2, 3, 1, 0, 5 },
        { 2, 4, 3, 1, 5, 0 },
    };

    // don't ask. these "just works"
    private static final Transformation ROTATION_MARKER_TRANSFORM_CENTER = new Scale(0.5f);
    private static final Transformation[] ROTATION_MARKER_TRANSFORMS_SIDES_TRANSFORMS = {
        new Scale(0.25f).with(new Translation(0, 0, 0.375f)).compile(),
        new Scale(0.25f).with(new Translation(0.375f, 0, 0)).compile(),
        new Scale(0.25f).with(new Translation(0, 0, -0.375f)).compile(),
        new Scale(0.25f).with(new Translation(-0.375f, 0, 0)).compile(), };
    private static final int[] ROTATION_MARKER_TRANSFORMS_SIDES = { -1, -1, 2, 0, 3, 1, -1, -1, 0, 2, 3, 1, 0, 2, -1,
        -1, 3, 1, 2, 0, -1, -1, 3, 1, 1, 3, 2, 0, -1, -1, 3, 1, 2, 0, -1, -1 };
    private static final Transformation[] ROTATION_MARKER_TRANSFORMS_CORNER = {
        new Scale(0.25f).with(new Translation(0.375f, 0, 0.375f)).compile(),
        new Scale(0.25f).with(new Translation(-0.375f, 0, 0.375f)).compile(),
        new Scale(0.25f).with(new Translation(0.375f, 0, -0.375f)).compile(),
        new Scale(0.25f).with(new Translation(-0.375f, 0, -0.375f)).compile(), };
    private static int rotationMarkerDisplayList;
    private static boolean rotationMarkerDisplayListCompiled = false;

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        EntityPlayer player = event.getPlayer();
        World world = player.getEntityWorld();
        BlockPos pos = target.getBlockPos();

        // No idea why this happens, but it does
        //noinspection ConstantValue
        if (pos == null) return;

        final IBlockState block = world.getBlockState(pos);
        final TileEntity tile = world.getTileEntity(pos);

        if (!isWrench(player.getHeldItemOffhand()) && !isWrench(player.getHeldItemMainhand())) return;
        if (!isWrenchable(block, tile)) return;

        drawGrid(
            event, event.getPlayer()
                .isSneaking());
    }

    private static boolean isWrench(ItemStack stack) {
        return MMItemList.Wrench.matches(stack);
    }

    private static boolean isWrenchable(@SuppressWarnings("unused") IBlockState state, TileEntity tile) {
        return tile instanceof IAlignmentProvider provider && provider.getAlignment() != null;
    }

    private static void drawGrid(DrawBlockHighlightEvent event, boolean isSneaking) {
        event.setCanceled(true);

        GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // pause shader
        int program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        GL20.glUseProgram(0);
        GL11.glLineWidth(calculateLineWidth());

        RayTraceResult target = event.getTarget();
        EntityPlayer player = event.getPlayer();
        World world = player.getEntityWorld();
        BlockPos pos = target.getBlockPos();
        final IBlockState block = world.getBlockState(pos);
        final TileEntity tile = world.getTileEntity(pos);
        Vector3d cam = MMRenderUtils.getPlayerPosition(event.getPartialTicks());

        GL11.glColor4f(0.25f, 0.25f, 0.25f, 1f);

        // draw block outline
        if (block.getMaterial() != Material.AIR) {
            AxisAlignedBB box = block.getSelectedBoundingBox(world, pos)
                .grow(0.002D)
                .offset(-cam.x, -cam.y, -cam.z);

            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINE_STRIP);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glEnd();

            GL11.glBegin(GL11.GL_LINES);
            GL11.glVertex3d(box.minX, box.minY, box.minZ);
            GL11.glVertex3d(box.minX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.minZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.minZ);
            GL11.glVertex3d(box.maxX, box.minY, box.maxZ);
            GL11.glVertex3d(box.maxX, box.maxY, box.maxZ);
            GL11.glVertex3d(box.minX, box.minY, box.maxZ);
            GL11.glVertex3d(box.minX, box.maxY, box.maxZ);
            GL11.glEnd();
        }

        GL11.glTranslated(pos.getX() - (int) cam.x, pos.getY() - (int) cam.y, pos.getZ() - (int) cam.z);
        GL11.glTranslated(0.5D - (cam.x - (int) cam.x), 0.5D - (cam.y - (int) cam.y), 0.5D - (cam.z - (int) cam.z));

        final int sideHit = target.sideHit.ordinal();
        Rotation.sideRotations[sideHit].glApply();

        // draw grid
        GL11.glTranslated(0.0D, -0.502D, 0.0D);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(0.50D, 0.0D, -0.25D);
        GL11.glVertex3d(-0.50D, 0.0D, -0.25D);
        GL11.glVertex3d(0.50D, 0.0D, 0.25D);
        GL11.glVertex3d(-0.50D, 0.0D, 0.25D);
        GL11.glVertex3d(0.25D, 0.0D, -0.50D);
        GL11.glVertex3d(0.25D, 0.0D, 0.50D);
        GL11.glVertex3d(-0.25D, 0.0D, -0.50D);
        GL11.glVertex3d(-0.25D, 0.0D, 0.50D);

        int connections = 0;

        if (tile instanceof IAlignmentProvider provider) {
            IAlignment alignment = provider.getAlignment();

            if (alignment != null) {
                connections |= 1 << alignment.getExtendedFacing().getDirection().ordinal();
            }
        }

        if (connections != 0) {
            for (EnumFacing side : EnumFacing.VALUES) {
                if ((connections & (1 << side.ordinal())) == 0) continue;

                switch (GRID_SWITCH_TABLE[target.sideHit.ordinal()][side.ordinal()]) {
                    case 0 -> {
                        GL11.glVertex3d(0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, -0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.25D);
                    }
                    case 1 -> {
                        GL11.glVertex3d(-0.25D, 0.0D, 0.50D);
                        GL11.glVertex3d(0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, 0.50D);
                    }
                    case 2 -> {
                        GL11.glVertex3d(-0.50D, 0.0D, -0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.50D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, -0.25D);
                    }
                    case 3 -> {
                        GL11.glVertex3d(-0.25D, 0.0D, -0.50D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, -0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.50D);
                    }
                    case 4 -> {
                        GL11.glVertex3d(0.50D, 0.0D, -0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.50D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.25D);
                    }
                    case 5 -> {
                        GL11.glVertex3d(0.50D, 0.0D, 0.50D);
                        GL11.glVertex3d(0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.50D, 0.0D, 0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, 0.50D);
                        GL11.glVertex3d(0.50D, 0.0D, -0.50D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.25D);
                        GL11.glVertex3d(0.50D, 0.0D, -0.25D);
                        GL11.glVertex3d(0.25D, 0.0D, -0.50D);
                        GL11.glVertex3d(-0.50D, 0.0D, 0.50D);
                        GL11.glVertex3d(-0.25D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.50D, 0.0D, 0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, 0.50D);
                        GL11.glVertex3d(-0.50D, 0.0D, -0.50D);
                        GL11.glVertex3d(-0.25D, 0.0D, -0.25D);
                        GL11.glVertex3d(-0.50D, 0.0D, -0.25D);
                        GL11.glVertex3d(-0.25D, 0.0D, -0.50D);
                    }
                }
            }
        }

        GL11.glEnd();

        // draw turning indicator
        if (tile instanceof IAlignmentProvider provider) {
            final IAlignment alignment = provider.getAlignment();

            if (alignment != null) {
                for (var transform : getTransform(alignment.getDirection(), sideHit)) {
                    drawExtendedRotationMarker(transform, isSneaking, alignment);
                }
            }
        }

        GL20.glUseProgram(program); // resume shader
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix(); // get back to player center
    }

    private static Transformation @NotNull [] getTransform(EnumFacing direction, int sideHit) {
        try {
            if (direction.ordinal() == sideHit) {
                return new Transformation[] { ROTATION_MARKER_TRANSFORM_CENTER };
            } else if (direction.getOpposite().ordinal() == sideHit) {
                return ROTATION_MARKER_TRANSFORMS_CORNER;
            } else {
                return new Transformation[] {
                    ROTATION_MARKER_TRANSFORMS_SIDES_TRANSFORMS[ROTATION_MARKER_TRANSFORMS_SIDES[sideHit * 6
                        + direction.ordinal()]] };
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return new Transformation[] {};
        }
    }

    private static void drawExtendedRotationMarker(Transformation transform, boolean sneaking, IAlignment alignment) {
        if (sneaking) {
            if (alignment.isFlipChangeAllowed()) {
                drawFlipMarker(transform);
            }
        } else {
            if (alignment.isRotationChangeAllowed()) {
                drawRotationMarker(transform);
            }
        }
    }

    private static void drawRotationMarker(Transformation transform) {
        if (!rotationMarkerDisplayListCompiled) {
            rotationMarkerDisplayList = GLAllocation.generateDisplayLists(1);
            compileRotationMarkerDisplayList(rotationMarkerDisplayList);
            rotationMarkerDisplayListCompiled = true;
        }
        GL11.glPushMatrix();
        transform.glApply();
        GL11.glCallList(rotationMarkerDisplayList);
        GL11.glPopMatrix();
    }

    private static void compileRotationMarkerDisplayList(int displayList) {
        GL11.glNewList(displayList, GL11.GL_COMPILE);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int i = 0; i <= ROTATION_MARKER_RESOLUTION; i++) {
            GL11.glVertex3d(
                Math.cos(i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION) * 0.4,
                0,
                Math.sin(i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION) * 0.4);
        }

        for (int i = ROTATION_MARKER_RESOLUTION; i >= 0; i--) {
            GL11.glVertex3d(
                Math.cos(i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION) * 0.24,
                0,
                Math.sin(i * Math.PI * 1.75 / ROTATION_MARKER_RESOLUTION) * 0.24);
        }

        GL11.glVertex3d(0.141114561800, 0, 0);
        GL11.glVertex3d(0.32, 0, -0.178885438199);
        GL11.glVertex3d(0.498885438199, 0, 0);

        GL11.glEnd();

        GL11.glEndList();
    }

    private static void drawFlipMarker(Transformation transform) {
        GL11.glPushMatrix();
        transform.glApply();
        // right shape
        GL11.glLineStipple(4, (short) 0xAAAA);
        GL11.glEnable(GL11.GL_LINE_STIPPLE);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(0.1d, 0d, 0.04d);
        GL11.glVertex3d(0.1d, 0d, 0.2d);
        GL11.glVertex3d(0.35d, 0d, 0.35d);
        GL11.glVertex3d(0.35d, 0d, -0.35d);
        GL11.glVertex3d(0.1d, 0d, -0.2d);
        GL11.glVertex3d(0.1d, 0d, -0.04d);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_STIPPLE);
        // left shape
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(-0.1d, 0d, 0.04d);
        GL11.glVertex3d(-0.1d, 0d, 0.2d);
        GL11.glVertex3d(-0.35d, 0d, 0.35d);
        GL11.glVertex3d(-0.35d, 0d, -0.35d);
        GL11.glVertex3d(-0.1d, 0d, -0.2d);
        GL11.glVertex3d(-0.1d, 0d, -0.04d);
        GL11.glEnd();
        // arrow
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex3d(0.15d, 0d, -0.04d);
        GL11.glVertex3d(0.15d, 0d, -0.1d);
        GL11.glVertex3d(0.25d, 0d, 0.d);
        GL11.glVertex3d(0.15d, 0d, 0.1d);
        GL11.glVertex3d(0.15d, 0d, 0.04d);
        GL11.glVertex3d(-0.15d, 0d, 0.04d);
        GL11.glVertex3d(-0.15d, 0d, 0.1d);
        GL11.glVertex3d(-0.25d, 0d, 0.d);
        GL11.glVertex3d(-0.15d, 0d, -0.1d);
        GL11.glVertex3d(-0.15d, 0d, -0.04d);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private static float calculateLineWidth() {
        // Assume default resolution has the same height as a standard Full HD monitor
        final float baseHeight = 1080F;

        // Calculate deviation using the actual height of the application window,
        // higher resolutions result in thicker lines,
        // lower resolutions result in thinner lines.
        return 2.5f * (Minecraft.getMinecraft().displayHeight / baseHeight);
    }
}
