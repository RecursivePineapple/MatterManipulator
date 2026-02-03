package matter_manipulator.client.rendering;

import java.nio.IntBuffer;
import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

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

    private final BufferBuilder buffer = new BufferBuilder(2_097_152);
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

        Vector3d currentPos = MMRenderUtils.getPlayerPosition(e.getPartialTicks());

        if (vbo == null) {
            vbo = new StreamingVertexBuffer(DefaultVertexFormats.BLOCK, GL11.GL_QUADS);
        }

        if (vboNeedsRebuild) {
            buffer.reset();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

            buffer.setTranslation(0, 0, 0);

            ModelManager modelManager = ((AccessorMinecraft) Minecraft.getMinecraft()).getModelManager();
            ProxiedWorld world = new ProxiedWorld(Minecraft.getMinecraft().world);

            java.nio.ByteBuffer bytes = buffer.getByteBuffer();
            IntBuffer data = buffer.getByteBuffer().asIntBuffer();

            int posOffset = 0, colorOffset = DefaultVertexFormats.BLOCK.getColorOffset();
            int intStride = DefaultVertexFormats.BLOCK.getIntegerSize();
            int byteStride = DefaultVertexFormats.BLOCK.getSize();

            for (VertexFormatElement element : DefaultVertexFormats.BLOCK.getElements()) {
                if (element.isPositionElement()) {
                    break;
                }

                posOffset += element.getSize();
            }

            posOffset /= 4;

            for (Hint hint : hints) {
                IBlockState state = hint.spec.getBlockState();

                int startVert = buffer.getVertexCount();

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

                int endVert = buffer.getVertexCount();

                float kR = hint.tint.getRed() / 255f;
                float kG = hint.tint.getGreen() / 255f;
                float kB = hint.tint.getBlue() / 255f;

                for (int vert = startVert; vert < endVert; vert++) {
                    int pos = vert * intStride + posOffset;

                    float cX = hint.getX() + 0.5f;
                    float cY = hint.getY() + 0.5f;
                    float cZ = hint.getZ() + 0.5f;

                    float x = Float.intBitsToFloat(data.get(pos));
                    float y = Float.intBitsToFloat(data.get(pos + 1));
                    float z = Float.intBitsToFloat(data.get(pos + 2));

                    x = (x - cX) * 0.7f + cX;
                    y = (y - cY) * 0.7f + cY;
                    z = (z - cZ) * 0.7f + cZ;

                    data.put(pos, Float.floatToIntBits(x));
                    data.put(pos + 1, Float.floatToIntBits(y));
                    data.put(pos + 2, Float.floatToIntBits(z));

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

            vboNeedsRebuild = false;
            vboNeedsSort = true;
            vboNeedsUpload = true;
        }

        if (vboNeedsSort || currentPos.distance(lastPlayerPosition) > 1.0) {
            lastPlayerPosition.set(currentPos);

            QuadCentroidComparator comparator = new QuadCentroidComparator();
            comparator.setOrigin((float) currentPos.x, (float) currentPos.y, (float) currentPos.z);

            MMRenderUtils.sortQuads(buffer.getByteBuffer(), 0, buffer.getVertexCount() / 4, buffer.getVertexFormat(), comparator);

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

            GL11.glTranslated(-currentPos.x, -currentPos.y, -currentPos.z);

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
}
