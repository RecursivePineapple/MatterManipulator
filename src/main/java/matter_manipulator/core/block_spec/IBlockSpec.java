package matter_manipulator.core.block_spec;

import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import matter_manipulator.common.block_spec.BlockSpecImpl;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.ResourceStack;

/// A specification that can recreate a block in full. This includes all block properties and all block entity state.
@NonExtendable
public interface IBlockSpec {

    IBlockSpec AIR = new BlockSpecImpl(Blocks.AIR.getDefaultState());

    boolean isValid();

    IBlockState getBlockState();
    ResourceStack getResource();

    void transform(Transform transform);

    /// Places the spec's block at the context's location but does not update its interop data. Must consume the
    /// required resource from the context.
    ApplyResult place(BlockPlacingContext context);

    /// Updates the spec's interop data at the context's location but does not place the block at all.
    EnumSet<ApplyResult> update(BlockPlacingContext context);

    IBlockSpec clone();

    default boolean isAir() {
        return getBlockState().getBlock() == Blocks.AIR;
    }
}
