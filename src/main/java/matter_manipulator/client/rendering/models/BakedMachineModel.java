package matter_manipulator.client.rendering.models;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.math.Transform;

public class BakedMachineModel implements IBakedModel {

    private static final Matrix4f[] ROTATIONS = new Matrix4f[ExtendedFacing.values().length];
    private static final Matrix4f[] TRANSFORMS = new Matrix4f[ExtendedFacing.values().length];

    public final IBakedModel base;
    public final VertexFormat format;
    private final IUnlistedProperty<ExtendedFacing> facingProp;

    private final int posX, posY, posZ, stride;

    private final UV[] uvs;

    @Desugar
    private record UV(int offset, int len) {

        public void take(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                dst[i + dstOffset * len] = data[stride * vert + this.offset + i];
            }
        }

        public void put(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                data[stride * vert + this.offset + i] = dst[i + dstOffset * len];
            }
        }

        public void swap(int[] data, int vert, int stride, int[] dst, int dstOffset) {
            for (int i = 0; i < len; i++) {
                int temp = dst[i + dstOffset * len];

                dst[i + dstOffset * len] = data[stride * vert + this.offset + i];

                data[stride * vert + this.offset + i] = temp;
            }
        }
    }

    public BakedMachineModel(IBakedModel base, VertexFormat format, IUnlistedProperty<ExtendedFacing> facingProp) {
        this.base = base;
        this.format = format;
        this.facingProp = facingProp;

        int offset = 0;
        VertexFormatElement element = null;

        for (var e : format.getElements()) {
            if (e.isPositionElement()) {
                element = e;
                break;
            }

            offset += e.getSize();
        }

        if (offset % 4 != 0) throw new IllegalStateException("Invalid vertex format: expected position to be int-aligned: " + format);
        if (element == null) throw new IllegalStateException("Invalid vertex format: position element was missing: " + format);
        if (element.getType() != EnumType.FLOAT) throw new IllegalStateException("Invalid vertex format: position element must be float: " + format);

        offset /= 4;

        posX = offset;
        posY = offset + 1;
        posZ = offset + 2;

        stride = format.getIntegerSize();

        int offset2 = 0;

        ArrayList<UV> uvs = new ArrayList<>();

        for (var e : format.getElements()) {
            switch (e.getUsage()) {
                case COLOR, UV -> {
                    if ((offset2 % 4) != 0) throw new IllegalStateException("Invalid vertex format: expected element to be int-aligned: " + e);

                    int len = e.getSize();

                    len = (len / 4) + (len % 4 == 0 ? 0 : 1);

                    uvs.add(new UV(offset2 / 4, len));
                }
            }

            offset2 += e.getSize();
        }

        this.uvs = uvs.toArray(new UV[0]);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state == null) {
            return base.getQuads(null, side, rand);
        }

        IExtendedBlockState extended = (IExtendedBlockState) state;

        ExtendedFacing facing = extended.getValue(facingProp);

        EnumFacing realSide = facing.getWorldDirectionInverse(side);

        List<BakedQuad> quads = base.getQuads(state, realSide, rand);

        Vector3f temp = new Vector3f();

        int scratchLength = 0;

        for (UV uv : uvs) {
            scratchLength += uv.len;
        }

        int[] scratch = new int[scratchLength * 4];

        return DataUtils.mapToList(quads, quad -> transformQuad(quad, side, facing, temp, scratch));
    }

    private BakedQuad transformQuad(BakedQuad src, EnumFacing side, ExtendedFacing facing, Vector3f temp, int[] tempInts) {
        facing = facing.with(Flip.HORIZONTAL);

        BakedQuad out = new BakedQuad(
            src.getVertexData().clone(),
            src.getTintIndex(),
            side,
            src.getSprite(),
            src.shouldApplyDiffuseLighting(),
            src.getFormat());

        int[] data = out.getVertexData();

//        if (facing.getFlip().isHorizontallyFlipped()) {
//            for (UV uv : uvs) {
//                flipX(data, uv, tempInts);
//            }
//        }
//
//        if (facing.getFlip().isVerticallyFlipped()) {
//            for (UV uv : uvs) {
//                flipY(data, uv, tempInts);
//            }
//        }

        int offset = 0;

        Matrix4f mat = TRANSFORMS[facing.ordinal()];

        float dx = 0.5f, dy = 0.5f, dz = 0.5f;

        switch (facing.getDirection()) {
            case DOWN, UP -> {
                dy = 0;
            }
            case NORTH, SOUTH -> {
                dz = 0;
            }
            case WEST, EAST -> {
                dx = 0;
            }
        }

        for (int i = 0; i < 4; i++) {
            temp.x = Float.intBitsToFloat(data[offset + posX]) - dx;
            temp.y = Float.intBitsToFloat(data[offset + posY]) - dy;
            temp.z = Float.intBitsToFloat(data[offset + posZ]) - dz;

            temp = facing.getIntegerAxisSwap().inverseTranslate(temp);

            data[offset + posX] = Float.floatToIntBits(temp.x + dx);
            data[offset + posY] = Float.floatToIntBits(temp.y + dy);
            data[offset + posZ] = Float.floatToIntBits(temp.z + dz);

            offset += stride;
        }

//        if (facing.getFlip().isEitherFlipped()) {
//            swap(data, stride, 3 * stride, stride);
//        }

        return out;
    }

    private static void swap(int[] data, int a, int b, int len) {
        for(int i = 0; i < len; i++) {
            int x = data[a + i];
            data[a + i] = data[b + i];
            data[b + i] = x;
        }
    }

    @Override
    public boolean isAmbientOcclusion() {
        return base.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return base.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return base.getParticleTexture();
    }

    @Override
    public @NotNull ItemOverrideList getOverrides() {
        return base.getOverrides();
    }

    static {
        int i = 0;

        for (ExtendedFacing facing : ExtendedFacing.VALUES) {
            TRANSFORMS[i++] = Transform.fromFacing(facing);
        }
    }
}
