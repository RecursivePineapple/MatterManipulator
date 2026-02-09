package matter_manipulator.client.rendering;

import java.nio.IntBuffer;

public interface BufferBuilderExt {

    boolean mm$isDrawing();

    void mm$setVertexCount(int count);

    IntBuffer mm$getIntBuffer();

}
