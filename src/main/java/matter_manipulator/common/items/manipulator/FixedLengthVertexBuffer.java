package matter_manipulator.common.items.manipulator;

import com.gtnewhorizon.gtnhlib.client.renderer.vertex.VertexFormat;

import org.intellij.lang.annotations.MagicConstant;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

/// Note: this doesn't work properly for some reason, don't use it as-is
public class FixedLengthVertexBuffer extends StreamingVertexBuffer {

    public FixedLengthVertexBuffer(VertexFormat format, int drawMode) {
        super(format, drawMode);
    }

    @Override
    public void reallocate() {
        generate();

        // noinspection MagicConstant
        setSize(this.length, this.bufferFlags);
    }

    @Override
    public void allocate(
        int vertexCount,
        @MagicConstant(intValues = {
            ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT,
            GL30.GL_MAP_READ_BIT,
            GL30.GL_MAP_WRITE_BIT,
            ARBBufferStorage.GL_MAP_PERSISTENT_BIT,
            ARBBufferStorage.GL_MAP_COHERENT_BIT,
            ARBBufferStorage.GL_CLIENT_STORAGE_BIT,
        }) int usage
    ) {
        // noinspection MagicConstant
        if (this.id == 0 || vertexCount < this.vertexCount / 4 || vertexCount > this.vertexCount || usage != this.bufferFlags) {
            generate();

            setSize(vertexCount * (long) format.getVertexSize(), usage);
        }

        this.vertexCount = vertexCount;
    }

    public void setSize(
        long length,
        @MagicConstant(intValues = {
            ARBBufferStorage.GL_DYNAMIC_STORAGE_BIT,
            GL30.GL_MAP_READ_BIT,
            GL30.GL_MAP_WRITE_BIT,
            ARBBufferStorage.GL_MAP_PERSISTENT_BIT,
            ARBBufferStorage.GL_MAP_COHERENT_BIT,
            ARBBufferStorage.GL_CLIENT_STORAGE_BIT,
        }) int bufferFlags
    ) {
        if (this.length > 0) throw new IllegalStateException("Cannot resize an immutable (fixed length) vertex buffer");

        bind();

        this.length = length;
        this.bufferFlags = bufferFlags;
        ARBBufferStorage.glBufferStorage(GL15.GL_ARRAY_BUFFER, length, bufferFlags);

        unbind();
    }

    public void flush() {
        bind();
        GL30.glFlushMappedBufferRange(GL15.GL_ARRAY_BUFFER, 0, vertexCount * (long) format.getVertexSize());
        unbind();
    }

    public void flushAll() {
        bind();
        GL30.glFlushMappedBufferRange(GL15.GL_ARRAY_BUFFER, 0, length);
        unbind();
    }
}
