package matter_manipulator.client.rendering.models;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

public interface QuadFactory {

    @Nullable
    @Contract("_, _, _, _ -> new")
    BakedQuad getQuad(@Nullable EnumFacing facing, long rand, Rotation rotation, Flip flip);
}
