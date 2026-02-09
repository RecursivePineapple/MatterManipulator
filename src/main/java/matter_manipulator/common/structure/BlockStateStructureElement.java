package matter_manipulator.common.structure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.client.rendering.MMRenderUtils;
import matter_manipulator.common.block_spec.StandardBlockSpec;
import matter_manipulator.core.meta.MetaKey;

/// A simple [IStructureElement] that only checks if the block is a specific [IBlockState].
public class BlockStateStructureElement<T> implements IStructureElement<T> {

    public final IBlockState state;

    public BlockStateStructureElement(IBlockState state) {
        this.state = state;
    }

    @Override
    public <K> K getMetadata(MetaKey<K> key) {
        return null;
    }

    @Override
    public boolean check(StructureContext<? extends T> context, BlockPos pos) {
        return context.getWorld().getBlockState(pos) == state;
    }

    @Override
    public boolean build(StructureContext<? extends T> context, BlockPos pos) {
        return false;
    }

    @Override
    public void emitHint(StructureContext<? extends T> context, BlockPos pos) {
        if (!check(context, pos)) {
            context.emitHint(pos, new StandardBlockSpec(state), MMRenderUtils.WHITE);
        }
    }
}
