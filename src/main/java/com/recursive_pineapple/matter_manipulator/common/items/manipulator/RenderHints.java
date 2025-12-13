package com.recursive_pineapple.matter_manipulator.common.items.manipulator;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

import com.gtnewhorizon.gtnhlib.client.renderer.TessellatorManager;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.DefaultVertexFormat;
import com.gtnewhorizon.gtnhlib.client.renderer.vertex.VertexFormat;
import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import com.recursive_pineapple.matter_manipulator.MMMod;

import org.joml.Vector3d;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.glu.GLU;

import lombok.Setter;

@EventBusSubscriber(side = Side.CLIENT)
public class RenderHints {

    private static final int BYTES_PER_HINT = DefaultVertexFormat.POSITION_TEXTURE_COLOR.getVertexSize() * 4 * 6;

    private static final ArrayList<Hint> HINTS = new ArrayList<>(10000);

    private static final Vector3d LAST_PLAYER_POSITION = new Vector3d();
    private static final Vector3i LAST_RENDERED_PLAYER_POSITION = new Vector3i();

    private static boolean vboNeedsRebuild = false;
    /** The VBO being used for rendering */
    private static VertexBuffer activeVBO;
    /** The VBO that's mapped and is being written to */
    private static VertexBuffer pendingVBO;

    private static final ExecutorService WORKER_THREAD = Executors.newFixedThreadPool(1);
    private static Future<VBOResult> renderTask;

    @Setter
    private static boolean drawOnTop = false;

    public static void reset() {
        if (renderTask != null) {
            renderTask.cancel(true);
            renderTask = null;
        }

        HINTS.clear();

        vboNeedsRebuild = true;
    }

    public static void addHint(int x, int y, int z, Block block, int meta, short[] tint) {

        Hint hint = new Hint();

        hint.x = x;
        hint.y = y;
        hint.z = z;
        hint.icons = new IIcon[6];
        hint.tint = tint;

        for (int i = 0; i < 6; i++) {
            hint.icons[i] = block.getIcon(i, meta);
        }

        HINTS.add(hint);
    }

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load e) {
        if (e.world.isRemote) {
            reset();
        }
    }

    private static VBOResult buildVBO(ByteBuffer buffer, ArrayList<Hint> hints, double xd, double yd, double zd, int xi, int yi, int zi) {
        try {
            Vector3d eyes = new Vector3d(xd, yd, zd);

            hints.sort(Comparator.comparingDouble(info -> -eyes.distanceSquared(info.x + 0.5, info.y + 0.5, info.z + 0.5)));

            TessellatorManager.startCapturing();

            Tessellator tes = TessellatorManager.get();

            tes.startDrawing(GL11.GL_QUADS);

            int hintCount = hints.size();

            // noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < hintCount; i++) {
                hints.get(i).draw(tes, xd, yd, zd, xi, yi, zi);
            }

            final var quads = TessellatorManager.stopCapturingToPooledQuads();

            long expectedSize = (long) DefaultVertexFormat.POSITION_TEXTURE_COLOR.getVertexSize() * quads.size() * 4;

            buffer.rewind();

            if (expectedSize > buffer.capacity()) {
                MMMod.LOG.error(
                    "Could not upload hint VBO: Could not insert hint quads into GL buffer (expectedSize={}, buffer.capacity={})",
                    expectedSize,
                    buffer.capacity()
                );

                return new VBOResult(new Vector3i(xi, yi, zi), 0);
            }

            // noinspection ForLoopReplaceableByForEach
            for (int i = 0, quadsSize = quads.size(); i < quadsSize; i++) {
                DefaultVertexFormat.POSITION_TEXTURE_COLOR.writeQuad(quads.get(i), buffer);
            }

            buffer.rewind();

            return new VBOResult(new Vector3i(xi, yi, zi), quads.size() * 4);
        } finally {
            TessellatorManager.cleanup();
        }
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent e) {
        if (HINTS.isEmpty()) return;

        Profiler p = Minecraft.getMinecraft().mcProfiler;

        p.startSection("Render MM Hints");

        Entity player = Minecraft.getMinecraft().renderViewEntity;
        double xd = player.lastTickPosX + (player.posX - player.lastTickPosX) * e.partialTicks;
        double yd = player.lastTickPosY + (player.posY - player.lastTickPosY) * e.partialTicks;
        double zd = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * e.partialTicks;
        int xi = (int) xd, yi = (int) yd, zi = (int) zd;

        Vector3d currentPos = new Vector3d(xd, yd, zd);

        if (activeVBO == null) {
            activeVBO = new VertexBuffer(DefaultVertexFormat.POSITION_TEXTURE_COLOR, GL11.GL_QUADS);
        }

        if (pendingVBO == null) {
            pendingVBO = new VertexBuffer(DefaultVertexFormat.POSITION_TEXTURE_COLOR, GL11.GL_QUADS);
        }

        if (renderTask != null && renderTask.isDone()) {
            VBOResult result = null;
            try {
                result = renderTask.get();
            } catch (InterruptedException | ExecutionException ex) {
                MMMod.LOG.error("Could not cancel render hints", ex);
            }

            renderTask = null;
            pendingVBO.unmap();

            if (result != null) {
                LAST_RENDERED_PLAYER_POSITION.set(result.playerPosition);
                pendingVBO.vertexCount = result.vertexCount;

                VertexBuffer temp = activeVBO;
                activeVBO = pendingVBO;
                pendingVBO = temp;
            }
        }

        if (renderTask == null && (vboNeedsRebuild || currentPos.distance(LAST_PLAYER_POSITION) > 1.0)) {
            LAST_PLAYER_POSITION.set(currentPos);
            vboNeedsRebuild = false;

            ArrayList<Hint> hints = new ArrayList<>(HINTS);

            if (pendingVBO.mapped) pendingVBO.unmap();

            pendingVBO.ensureSize((long) hints.size() * BYTES_PER_HINT, GL15.GL_STREAM_DRAW);
            ByteBuffer buffer = pendingVBO.map(GL15.GL_WRITE_ONLY);

            if (buffer != null) {
                renderTask = WORKER_THREAD.submit(() -> buildVBO(buffer, hints, xd, yd, zd, xi, yi, zi));
            }
        }

        if (activeVBO.vertexCount > 0) {
            p.startSection("Draw MM Hints");

            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);

            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

            GL11.glTranslated(-xd + LAST_RENDERED_PLAYER_POSITION.x, -yd + LAST_RENDERED_PLAYER_POSITION.y, -zd + LAST_RENDERED_PLAYER_POSITION.z);

            // we need the back facing rendered because the thing is transparent
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_BLEND); // enable blend so it is transparent
            GL11.glBlendFunc(GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_SRC_ALPHA);

            if (drawOnTop) {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }

            // There aren't any frames in flight, so we can re-use this buffer on the next frame without issue
            activeVBO.render();

            GL11.glPopAttrib();
            GL11.glPopMatrix();

            p.endSection();
        }

        p.endSection();
    }

    private static class Hint {

        public int x, y, z;
        public IIcon[] icons;
        public short[] tint;

        public void draw(
            Tessellator tes,
            double eyeX,
            double eyeY,
            double eyeZ,
            int eyeXint,
            int eyeYint,
            int eyeZint
        ) {
            double size = 0.5;

            World w = Minecraft.getMinecraft().theWorld;

            int brightness = w.blockExists(x, 0, z) ? w.getLightBrightnessForSkyBlocks(x, y, z, 0) : 0;
            tes.setBrightness(brightness);

            tes.setColorRGBA(tint[0], tint[1], tint[2], 150);

            double X = (x - eyeXint) + 0.25;
            double Y = (y - eyeYint) + 0.25;
            double Z = (z - eyeZint) + 0.25;
            double worldX = x + 0.25;
            double worldY = y + 0.25;
            double worldZ = z + 0.25;

            // this rendering code is independently written by glee8e on July 10th, 2023
            // and is released as part of StructureLib under LGPL terms, just like everything else in this project
            // cube is a very special model. its facings can be rendered correctly by viewer distance without using
            // surface normals and view vector
            // here we do a 2 pass render.
            // first pass we draw obstructed faces (i.e. faces that are further away from player)
            // second pass we draw unobstructed faces
            for (int j = 0; j < 2; j++) {
                boolean unobstructedPass = j == 1;
                for (int i = 0; i < 6; i++) {
                    if (icons[i] == null) continue;

                    double u = icons[i].getMinU();
                    double U = icons[i].getMaxU();
                    double v = icons[i].getMinV();
                    double V = icons[i].getMaxV();

                    switch (i) { // {DOWN, UP, NORTH, SOUTH, WEST, EAST}
                        case 0 -> {
                            // all these ifs is in form if ((is face unobstructed) != (is in unobstructred pass))
                            if ((worldY >= eyeY) != unobstructedPass) continue;
                            tes.setNormal(0, -1, 0);
                            tes.addVertexWithUV(X, Y, Z, u, v);
                            tes.addVertexWithUV(X + size, Y, Z, U, v);
                            tes.addVertexWithUV(X + size, Y, Z + size, U, V);
                            tes.addVertexWithUV(X, Y, Z + size, u, V);
                        }
                        case 1 -> {
                            if ((worldY + size <= eyeY) != unobstructedPass) continue;
                            tes.setNormal(0, 1, 0);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            tes.addVertexWithUV(X, Y + size, Z + size, u, V);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z, U, v);
                        }
                        case 2 -> {
                            if ((worldZ >= eyeZ) != unobstructedPass) continue;
                            tes.setNormal(0, 0, -1);
                            tes.addVertexWithUV(X, Y, Z, U, V);
                            tes.addVertexWithUV(X, Y + size, Z, U, v);
                            tes.addVertexWithUV(X + size, Y + size, Z, u, v);
                            tes.addVertexWithUV(X + size, Y, Z, u, V);
                        }
                        case 3 -> {
                            if ((worldZ + size <= eyeZ) != unobstructedPass) continue;
                            tes.setNormal(0, 0, 1);
                            tes.addVertexWithUV(X + size, Y, Z + size, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, U, v);
                            tes.addVertexWithUV(X, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X, Y, Z + size, u, V);
                        }
                        case 4 -> {
                            if ((worldX >= eyeX) != unobstructedPass) continue;
                            tes.setNormal(-1, 0, 0);
                            tes.addVertexWithUV(X, Y, Z + size, U, V);
                            tes.addVertexWithUV(X, Y + size, Z + size, U, v);
                            tes.addVertexWithUV(X, Y + size, Z, u, v);
                            tes.addVertexWithUV(X, Y, Z, u, V);
                        }
                        case 5 -> {
                            if ((worldX + size <= eyeX) != unobstructedPass) continue;
                            tes.setNormal(1, 0, 0);
                            tes.addVertexWithUV(X + size, Y, Z, U, V);
                            tes.addVertexWithUV(X + size, Y + size, Z, U, v);
                            tes.addVertexWithUV(X + size, Y + size, Z + size, u, v);
                            tes.addVertexWithUV(X + size, Y, Z + size, u, V);
                        }
                    }
                }
            }
        }
    }

    private static class VBOResult {

        public Vector3i playerPosition;
        public int vertexCount;

        public VBOResult(Vector3i playerPosition, int vertexCount) {
            this.playerPosition = playerPosition;
            this.vertexCount = vertexCount;
        }
    }

    private static class VertexBuffer implements AutoCloseable {

        private int id;
        private volatile int vertexCount;
        private VertexFormat format;
        private int drawMode;

        private volatile long currentSize;
        private volatile int currentUsage;
        private volatile ByteBuffer oldMap;
        private volatile boolean mapped;

        public VertexBuffer() {
            this.id = GL15.glGenBuffers();
        }

        public VertexBuffer(VertexFormat format, int drawMode) {
            this();
            this.format = format;
            this.drawMode = drawMode;
        }

        public void bind() {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.id);
        }

        public void unbind() {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        }

        public void upload(int usage, ByteBuffer buffer, int vertexCount) {
            if (this.id != -1) {
                this.vertexCount = vertexCount;
                this.bind();
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, usage);
                this.unbind();
            }
        }

        public void upload(ByteBuffer buffer) {
            if (this.format == null) {
                throw new IllegalStateException("No format specified for VBO upload");
            } else {
                this.upload(GL15.GL_STATIC_DRAW, buffer, buffer.remaining() / this.format.getVertexSize());
            }
        }

        public void close() {
            if (this.id >= 0) {
                GL15.glDeleteBuffers(this.id);
                this.id = -1;
            }
        }

        public void draw(FloatBuffer floatBuffer) {
            GL11.glPushMatrix();
            GL11.glLoadIdentity();
            GL11.glMultMatrix(floatBuffer);
            this.draw();
            GL11.glPopMatrix();
        }

        public void draw() {
            GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
        }

        public void setupState() {
            if (this.format == null) {
                throw new IllegalStateException("No format specified for VBO setup");
            } else {
                this.bind();
                this.format.setupBufferState(0L);
            }
        }

        public void cleanupState() {
            this.format.clearBufferState();
            this.unbind();
        }

        public void render() {
            this.setupState();
            this.draw();
            this.cleanupState();
        }

        public void ensureSize(long size, int usage) {
            if (size > currentSize || currentSize / 4 > size || currentUsage != usage) {
                bind();

                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, size, usage);
                currentSize = size;
                currentUsage = usage;

                unbind();
            }
        }

        @SuppressWarnings("NonAtomicOperationOnVolatileField")
        public ByteBuffer map(int access) {
            if (mapped) throw new IllegalStateException("cannot map the same buffer twice");
            if (currentSize == 0) throw new IllegalStateException("cannot map an empty buffer");

            bind();

            GL11.glGetError();

            oldMap = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, access, currentSize, oldMap);

            if (oldMap == null) {
                MMMod.LOG.error("Error mapping buffer: {}", GLU.gluErrorString(GL11.glGetError()));
            } else {
                mapped = true;
            }

            unbind();

            return oldMap;
        }

        public void unmap() {
            if (!mapped) throw new IllegalStateException("cannot unmap the same buffer twice");

            bind();

            GL11.glGetError();

            GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
            int error = GL11.glGetError();

            if (error != 0) {
                MMMod.LOG.error("Error unmapping buffer: {}", GLU.gluErrorString(error));
            }

            mapped = false;

            unbind();
        }
    }
}
