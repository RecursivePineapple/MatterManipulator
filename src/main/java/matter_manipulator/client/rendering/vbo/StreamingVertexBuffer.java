package matter_manipulator.client.rendering.vbo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.glu.GLU;

import lombok.Getter;
import matter_manipulator.MMMod;

public class StreamingVertexBuffer implements AutoCloseable {

    @Getter
    protected final VertexFormat format;
    @Getter
    protected final int drawMode;

    @Getter
    protected int id;
    @Getter
    protected int vertexCount;

    @Getter
    protected long length;
    @Getter
    protected int bufferFlags;

    @Getter
    protected ByteBuffer mappedBuffer;
    @Getter
    protected boolean mapped;

    public StreamingVertexBuffer(VertexFormat format, int drawMode) {
        this.id = GL15.glGenBuffers();
        this.format = format;
        this.drawMode = drawMode;
    }

    /// Generates a new vertex buffer and closes the previous one, if present.
    /// This should be used sparingly - it's very expensive to call it each frame.
    public void generate() {
        if (this.id > 0) {
            close();
        }

        this.id = GL15.glGenBuffers();
    }

    @Override
    public void close() {
        if (this.id > 0) {
            if (mapped) unmap();

            GL15.glDeleteBuffers(this.id);

            this.id = 0;
            this.vertexCount = 0;
            this.length = 0;
        }
    }

    public void bind() {
        if (this.id == 0) throw new IllegalStateException("Cannot bind unallocated VBO");

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.id);
    }

    public void unbind() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void upload(int usage, ByteBuffer buffer, int vertexCount) {
        if (this.id > 0) {
            this.vertexCount = vertexCount;
            this.bind();
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, usage);
            this.unbind();
        }
    }

    public void upload(ByteBuffer buffer) {
        this.upload(GL15.GL_DYNAMIC_DRAW, buffer, buffer.remaining() / this.format.getSize());
    }

    public void upload(BufferBuilder buffer) {
        if (!Objects.equals(buffer.getVertexFormat(), this.format)) {
            throw new IllegalArgumentException("Invalid buffer format. Was " + buffer.getVertexFormat() + " but expected " + this.format);
        }

        this.upload(GL15.GL_DYNAMIC_DRAW, buffer.getByteBuffer(), buffer.getVertexCount());
    }

    public void draw(FloatBuffer floatBuffer) {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glMultMatrix(floatBuffer);
        this.draw();
        GL11.glPopMatrix();
    }

    public void draw() {
        if (mapped) throw new IllegalStateException("Cannot draw a buffer that is mapped");

        GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
    }

    public void setupState() {
        this.bind();
        setupVertexFormat(this.format);
    }

    public void cleanupState() {
        cleanupVertexFormat(this.format);
        this.unbind();
    }

    private static void setupVertexFormat(VertexFormat format) {
        int offset = 0;
        int stride = format.getSize();

        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION -> {
                    GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                    GL11.glVertexPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                }
                case UV -> {
                    GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + element.getIndex());
                    GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    GL11.glTexCoordPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                    GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
                }
                case COLOR -> {
                    GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                    GL11.glColorPointer(element.getElementCount(), element.getType().getGlConstant(), stride, offset);
                }
            }

            offset += element.getSize();
        }
    }

    private static void cleanupVertexFormat(VertexFormat format) {
        for (VertexFormatElement element : format.getElements()) {
            switch (element.getUsage()) {
                case POSITION -> {
                    GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                }
                case UV -> {
                    GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + element.getIndex());
                    GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                    GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
                }
                case COLOR -> {
                    GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                }
            }
        }
    }

    public void render() {
        this.setupState();
        this.draw();
        this.cleanupState();
    }

    /// Reallocates the memory stored in this VBO so that the driver can avoid synchronization flushes. Typically
    /// drivers pool memory so there's a good chance it'll just pull it from the pool instead of allocating anything
    /// since the length is the same.
    /// Reference: [Buffer Object Streaming](https://wikis.khronos.org/opengl/Buffer_Object_Streaming)
    public void reallocate() {
        bind();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, length, bufferFlags);
        unbind();
    }

    public void allocate(
        int vertexCount,
        @MagicConstant(intValues = {
            GL15.GL_STREAM_DRAW,
            GL15.GL_STREAM_READ,
            GL15.GL_STREAM_COPY,
            GL15.GL_STATIC_DRAW,
            GL15.GL_STATIC_READ,
            GL15.GL_STATIC_COPY,
            GL15.GL_DYNAMIC_DRAW,
            GL15.GL_DYNAMIC_READ,
            GL15.GL_DYNAMIC_COPY,
        }) int usage
    ) {
        bind();

        this.vertexCount = vertexCount;
        this.length = vertexCount * (long) format.getSize();
        this.bufferFlags = usage;

        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.length, this.bufferFlags);

        unbind();
    }

    /// Maps the buffer into the client memory space (CPU) and returns a [ByteBuffer] wrapper for it.
    /// @param access See [glMapBufferRange](https://docs.gl/es3/glMapBufferRange) for more info.
    public ByteBuffer map(
        @MagicConstant(intValues = {
            GL30.GL_MAP_READ_BIT,
            GL30.GL_MAP_WRITE_BIT,
            GL30.GL_MAP_INVALIDATE_RANGE_BIT,
            GL30.GL_MAP_INVALIDATE_BUFFER_BIT,
            GL30.GL_MAP_FLUSH_EXPLICIT_BIT,
            GL30.GL_MAP_UNSYNCHRONIZED_BIT
        }) int access
    ) {
        if (mapped) throw new IllegalStateException("cannot map the same buffer twice");

        bind();

        if (mappedBuffer != null) {
            mappedBuffer.clear();
        }

        mappedBuffer = GL30.glMapBufferRange(GL15.GL_ARRAY_BUFFER, 0, length, access, mappedBuffer);

        if (mappedBuffer == null) {
            MMMod.LOG.error("Error mapping buffer: {}", GLU.gluErrorString(GL11.glGetError()));
        } else {
            mapped = true;
        }

        unbind();

        return mappedBuffer;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean unmap() {
        if (!mapped) throw new IllegalStateException("cannot unmap the same buffer twice");

        bind();

        boolean valid = true;

        if (!GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER)) {
            // Something happened that corrupted the VBO, it has to be re-initialized
            reallocate();
            valid = false;
        }

        int error = GL11.glGetError();

        if (error != 0) {
            MMMod.LOG.error("Error unmapping buffer: {}", GLU.gluErrorString(error));
        }

        mapped = false;

        unbind();

        return valid;
    }
}
