package matter_manipulator.client.rendering;

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

import lombok.Setter;
import matter_manipulator.client.rendering.vbo.StreamingVertexBuffer;
import matter_manipulator.client.rendering.vertex.QuadCentroidComparator;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.mixin.mixins.minecraft.AccessorMinecraft;

public class MMHintRenderer {

    public static final MMHintRenderer INSTANCE = new MMHintRenderer();

    /// The latest list of hints. This is not sorted in any way and can only be accessed by the client thread.
    private final ArrayList<Hint> hints = new ArrayList<>(0);

    /// The player position for the most recent buffer. If the player moves too far, it will cause the quads to be
    /// re-sorted.
    private final Vector3d lastPlayerPosition = new Vector3d();

    /// True when the hints have changed and the VBO needs to be rebuilt from scratch
    private boolean vboNeedsRebuild = false;
    /// True when the quads in the built buffer need to be re-sorted (i.e. from the player moving)
    private boolean vboNeedsSort = false;
    /// True when the cpu buffer needs to be re-uploaded to the VBO.
    private boolean vboNeedsUpload = false;

    private final HintBufferBuilder buffer = new HintBufferBuilder(2_097_152);
    private final ForgeBlockModelRenderer modelRenderer = new ForgeBlockModelRenderer(Minecraft.getMinecraft().getBlockColors());
    private final BlockFluidRenderer fluidRenderer = new BlockFluidRenderer(Minecraft.getMinecraft().getBlockColors());

    /// The VBO that's being used for rendering
    private StreamingVertexBuffer vbo;

    private MMHintRenderer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void reset() {
        if (hints.isEmpty()) {
            hints.trimToSize();
        } else {
            hints.clear();
            vboNeedsRebuild = true;
        }
    }

    public void addHint(int x, int y, int z, IBlockSpec spec, ImmutableColor tint) {
        hints.add(new Hint(x, y, z, spec, tint));
        vboNeedsRebuild = true;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        if (e.getWorld().isRemote) {
            reset();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (hints.isEmpty()) return;

        Profiler p = Minecraft.getMinecraft().profiler;

        p.startSection("Render MM Hints");

        Entity player = Minecraft.getMinecraft().getRenderViewEntity();
        assert player != null;

        double xd = player.lastTickPosX + (player.posX - player.lastTickPosX) * e.getPartialTicks();
        double yd = player.lastTickPosY + (player.posY - player.lastTickPosY) * e.getPartialTicks();
        double zd = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * e.getPartialTicks();

        Vector3d currentPos = new Vector3d(xd, yd, zd);

        if (vbo == null) {
            vbo = new StreamingVertexBuffer(DefaultVertexFormats.BLOCK, GL11.GL_QUADS);
        }

        if (vboNeedsRebuild) {
            buffer.reset();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            ModelManager modelManager = ((AccessorMinecraft) Minecraft.getMinecraft()).getModelManager();
            ProxiedWorld world = new ProxiedWorld(Minecraft.getMinecraft().world);

            Vector3d pos = new Vector3d();

            for (Hint hint : hints) {
                IBlockState state = hint.spec.getBlockState();

                switch (state.getRenderType()) {
                    case MODEL -> {
                        world.overrides.clear();
                        world.setBlockState(hint, state);

                        buffer.setCurrentPos(pos.set(hint.getX() + 0.5, hint.getY() + 0.5, hint.getZ() + 0.5));
                        buffer.setTint(hint.tint);

                        IBakedModel model = modelManager.getBlockModelShapes().getModelForState(state);
                        modelRenderer.renderModel(
                            world,
                            model,
                            state,
                            hint,
                            buffer,
                            false);
                    }
                    case LIQUID -> {
                        world.overrides.clear();
                        world.setBlockState(hint, state);

                        fluidRenderer.renderFluid(world, state, hint, buffer);
                    }
                    default -> {

                    }
                }
            }

            buffer.finishDrawing();

            vboNeedsRebuild = false;
            vboNeedsSort = true;
            vboNeedsUpload = true;
        }

        if (vboNeedsSort || currentPos.distance(lastPlayerPosition) > 1.0) {
            lastPlayerPosition.set(currentPos);

            MMRenderUtils.sortQuads(buffer.getByteBuffer(), 0, buffer.getVertexCount() / 4, buffer.getVertexFormat(), new QuadCentroidComparator());

            vboNeedsSort = false;
            vboNeedsUpload = true;
        }

        if (vboNeedsUpload) {
            vbo.upload(buffer.getByteBuffer());

            vboNeedsUpload = false;
        }

        if (vbo.getVertexCount() > 0) {
            p.startSection("Draw MM Hints");

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GL11.glTranslated(-xd + lastPlayerPosition.x, -yd + lastPlayerPosition.y, -zd + lastPlayerPosition.z);

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);

            GL11.glDisable(GL11.GL_DEPTH_TEST);

            // noinspection SynchronizeOnNonFinalField
            synchronized (vbo) {
                vbo.render();
            }

            GL11.glPopAttrib();
            GL11.glPopMatrix();

            p.endSection();
        }

        p.endSection();
    }

    private static class Hint extends BlockPos {

        public final IBlockSpec spec;
        public final ImmutableColor tint;

        public Hint(int x, int y, int z, IBlockSpec spec, ImmutableColor tint) {
            super(x, y, z);
            this.spec = spec;
            this.tint = tint;
        }
    }

    private static class HintBufferBuilder extends BufferBuilder {

        @Setter
        private Vector3d currentPos;
        @Setter
        private ImmutableColor tint;

        public HintBufferBuilder(int bufferSizeIn) {
            super(bufferSizeIn);
        }

        @Override
        public @NotNull BufferBuilder pos(double x, double y, double z) {
            return super.pos((x - currentPos.x) * 0.8 + currentPos.x, (y - currentPos.y) * 0.8 + currentPos.y, (z - currentPos.z) * 0.8 + currentPos.z);
        }

        @Override
        public @NotNull BufferBuilder color(int red, int green, int blue, int alpha) {
            return super.color(red * tint.getRed(), green * tint.getGreen(), blue * tint.getBlue(), alpha * tint.getAlpha());
        }
    }
}
