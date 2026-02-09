package matter_manipulator.mixin.mixins.minecraft;

import java.nio.IntBuffer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import matter_manipulator.client.rendering.BufferBuilderExt;

@Mixin(BufferBuilder.class)
public class MixinBufferBuilder implements BufferBuilderExt {

    @Shadow
    private boolean isDrawing;
    @Shadow
    private VertexFormat vertexFormat;
    @Shadow
    private IntBuffer rawIntBuffer;
    @Shadow
    private int vertexCount;

    @Override
    public boolean mm$isDrawing() {
        return this.isDrawing;
    }

    @Override
    public void mm$setVertexCount(int count) {
        rawIntBuffer.position(count * this.vertexFormat.getIntegerSize());
        this.vertexCount = count;
    }

    @Override
    public IntBuffer mm$getIntBuffer() {
        return rawIntBuffer;
    }
}
