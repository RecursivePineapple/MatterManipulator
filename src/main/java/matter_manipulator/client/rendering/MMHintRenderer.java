package matter_manipulator.client.rendering;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.lwjgl.opengl.GL11;

import matter_manipulator.MMMod;
import matter_manipulator.client.rendering.vbo.StreamingVertexBuffer;
import matter_manipulator.client.rendering.vertex.QuadCentroidComparator;
import matter_manipulator.common.utils.world.ProxiedWorld;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.color.ImmutableColor;
import matter_manipulator.mixin.mixins.minecraft.AccessorMinecraft;

public class MMHintRenderer {

    public static final MMHintRenderer INSTANCE = new MMHintRenderer();
    public static final VertexFormat FORMAT = DefaultVertexFormats.BLOCK;

    /// The most recent batch of hints. This is not used by the renderer, and it must be flushed by calling [#finish()].
    /// This must only be accessed by the client thread.
    private ArrayList<Hint> pendingHints = null;

    /// The latest list of hints. This is not sorted in any way and can only be accessed by the client thread.
    /// The contents of this can only be accessed by the worker thread, but the list reference itself can only be
    /// accessed by the client. The worker thread receives a reference to this list which it uses, but this field can be
    /// replaced arbitrarily.
    private List<Hint> hints = null;

    /// The player position for the most recent buffer. If the player moves too far, it will cause the quads to be
    /// re-sorted.
    private final Vector3d lastPlayerPosition = new Vector3d();

    /// True when the hints have changed and the VBO needs to be rebuilt from scratch
    private boolean vboNeedsRebuild = false;

    private final ExecutorService workerThread = Executors.newFixedThreadPool(1);
    private Future<Void> buildTask;

    /// The buffer builders. Flipping is done by the client thread. Worker threads must only write to the passive
    /// buffer. The client thread must only upload from the active buffer.
    private final FlippableReference<BufferBuilder> buffer = new FlippableReference<>(new BufferBuilder(2_097_152), new BufferBuilder(2_097_152));
    private StreamingVertexBuffer vbo;

    private final ForgeBlockModelRenderer modelRenderer = new ForgeBlockModelRenderer(Minecraft.getMinecraft().getBlockColors());
    private final BlockFluidRenderer fluidRenderer = new BlockFluidRenderer(Minecraft.getMinecraft().getBlockColors());


    private MMHintRenderer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void start() {
        pendingHints = new ArrayList<>();
    }

    public void addHint(int x, int y, int z, IBlockSpec spec, ImmutableColor tint) {
        pendingHints.add(new Hint(x, y, z, spec, tint));
    }

    public void finish() {
        hints = pendingHints == null ? null : Collections.unmodifiableList(pendingHints);
        pendingHints = null;
        vboNeedsRebuild = true;
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load e) {
        if (e.getWorld().isRemote) {
            start();
            finish();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (hints == null || hints.isEmpty()) return;

        Profiler p = Minecraft.getMinecraft().profiler;

        p.startSection("Render MM Hints");

        Vector3dc playerPos = MMRenderUtils.getPlayerPosition(e.getPartialTicks());

        if (vbo == null) {
            vbo = new StreamingVertexBuffer(FORMAT, GL11.GL_QUADS);
        }

        if (vbo.getVertexCount() > 0) {
            p.startSection("Draw MM Hints");

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            GL11.glTranslated(-playerPos.x(), -playerPos.y(), -playerPos.z());

            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            vbo.render();

            GL11.glPopAttrib();
            GL11.glPopMatrix();

            p.endSection();
        }

        if (buildTask != null && buildTask.isDone()) {
            try {
                buildTask.get();

                vbo.upload(buffer.flip());
            } catch (Throwable t) {
                MMMod.LOG.error("Error running background render task", t);
            }

            buildTask = null;
        }

        if (buildTask == null) {
            if (vboNeedsRebuild) {
                vboNeedsRebuild = false;

                BufferBuilder data = buffer.passive();
                List<Hint> hints = this.hints;

                lastPlayerPosition.set(playerPos);

                buildTask = workerThread.submit(() -> {
                    rebuildBuffer(data, hints);
                    sortBuffer(data, playerPos);

                    return null;
                });
            } else if (playerPos.distance(lastPlayerPosition) > 1.0) {
                lastPlayerPosition.set(playerPos);

                BufferBuilder data = buffer.passive();
                BufferBuilder active = buffer.active();

                data.reset();

                if (active.getVertexCount() > 0) {
                    data.begin(GL11.GL_QUADS, FORMAT);
                    data.setVertexState(active.getVertexState());
                    data.finishDrawing();

                    buildTask = workerThread.submit(() -> {
                        sortBuffer(data, playerPos);

                        return null;
                    });
                }
            }
        }

        p.endSection();
    }

    private void rebuildBuffer(BufferBuilder buffer, List<Hint> hints) {
        if (((BufferBuilderExt) buffer).mm$isDrawing()) {
            MMMod.LOG.warn("Resetting buffer that was being drawn: this indicates a crash or logic error somewhere. Any contained geometry will be discarded.");
            buffer.finishDrawing();
        }

        buffer.reset();

        buffer.begin(GL11.GL_QUADS, FORMAT);

        buffer.setTranslation(0, 0, 0);

        ModelManager modelManager = ((AccessorMinecraft) Minecraft.getMinecraft()).getModelManager();
        ProxiedWorld world = new ProxiedWorld(Minecraft.getMinecraft().world);

        int posOffset = 0, colorOffset = FORMAT.getColorOffset();
        int intStride = FORMAT.getIntegerSize();
        int byteStride = FORMAT.getSize();

        for (VertexFormatElement element : FORMAT.getElements()) {
            if (element.isPositionElement()) {
                break;
            }

            posOffset += element.getSize();
        }

        posOffset /= 4;

        for (Hint hint : hints) {
            IBlockState state = hint.spec.getBlockState();

            int startVert = buffer.getVertexCount();

            try {
                switch (state.getRenderType()) {
                    case MODEL -> {
                        world.overrides.clear();
                        world.setBlockState(hint, state);

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
            } catch (Throwable t) {
                MMMod.LOG.error("Failed to render {}", state, t);
                ((BufferBuilderExt) buffer).mm$setVertexCount(startVert);
                break;
            }

            int endVert = buffer.getVertexCount();

            float kR = hint.tint.getRed() / 255f;
            float kG = hint.tint.getGreen() / 255f;
            float kB = hint.tint.getBlue() / 255f;

            ByteBuffer bytes = buffer.getByteBuffer();
            IntBuffer ints = ((BufferBuilderExt) buffer).mm$getIntBuffer();

            for (int vert = startVert; vert < endVert; vert++) {
                int pos = vert * intStride + posOffset;

                float cX = hint.getX() + 0.5f;
                float cY = hint.getY() + 0.5f;
                float cZ = hint.getZ() + 0.5f;

                float x = Float.intBitsToFloat(ints.get(pos));
                float y = Float.intBitsToFloat(ints.get(pos + 1));
                float z = Float.intBitsToFloat(ints.get(pos + 2));

                x = (x - cX) * 0.7f + cX;
                y = (y - cY) * 0.7f + cY;
                z = (z - cZ) * 0.7f + cZ;

                ints.put(pos, Float.floatToIntBits(x));
                ints.put(pos + 1, Float.floatToIntBits(y));
                ints.put(pos + 2, Float.floatToIntBits(z));

                int color = vert * byteStride + colorOffset;

                int r = bytes.get(color) & 0xFF;
                int g = bytes.get(color + 1) & 0xFF;
                int b = bytes.get(color + 2) & 0xFF;

                bytes.put(color, (byte) ((int) (r * kR) & 0xFF));
                bytes.put(color + 1, (byte) ((int) (g * kG) & 0xFF));
                bytes.put(color + 2, (byte) ((int) (b * kB) & 0xFF));
                bytes.put(color + 3, (byte) 100);
            }
        }

        buffer.finishDrawing();
    }

    private void sortBuffer(BufferBuilder buffer, Vector3dc playerPos) {
        QuadCentroidComparator comparator = new QuadCentroidComparator();
        comparator.setOrigin((float) playerPos.x(), (float) playerPos.y(), (float) playerPos.z());

        MMRenderUtils.sortQuads(buffer.getByteBuffer(), 0, buffer.getVertexCount() / 4, FORMAT, comparator);
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
}
