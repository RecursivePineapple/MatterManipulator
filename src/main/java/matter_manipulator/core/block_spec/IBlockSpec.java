package matter_manipulator.core.block_spec;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.common.utils.math.Transform;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.ResourceStack;

/// A specification that can recreate a block in full. This includes all block properties and all tile entity state.
/// Note that some specs may not completely erase whatever was previously present - as an example, a spec placing an AE
/// cable may keep any existing parts in the same block.
@NonExtendable
public interface IBlockSpec {

    IBlockSpec AIR = new StandardBlockSpec(Blocks.AIR.getDefaultState());

    IBlockSpecLoader getLoader();

    boolean isValid();

    IBlockState getBlockState();
    ResourceStack getResource();

    Localized getDisplayName();

    void transform(Transform transform);

    /// Places the spec's block at the context's location but does not update its interop data. Must consume the
    /// required resource from the context.
    ApplyResult place(BlockPlacingContext context);

    /// Updates the spec's interop data at the context's location but does not place the block at all.
    EnumSet<ApplyResult> update(BlockPlacingContext context);

    IBlockSpec clone();

    IBlockSpec clone(IBlockState newState);

    default boolean isAir() {
        return getBlockState().getMaterial() == Material.AIR;
    }
}
