package matter_manipulator.common.interop.block_state_transformers;

import java.util.function.Predicate;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import org.apache.commons.lang3.mutable.MutableObject;

import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.interop.BlockStateTransformer;

public class PropertyCopyStateTransformer implements BlockStateTransformer {

    private final Predicate<IProperty<?>> filter;

    public PropertyCopyStateTransformer(String propertyName) {
        filter = p -> p.getName().equals(propertyName);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ApplyResult transform(MutableObject<IBlockState> state, IBlockState target) {
        IBlockState curr = state.getValue();

        if (curr.getBlock() != target.getBlock()) return ApplyResult.NotApplicable;

        IProperty facing = DataUtils.find(curr.getPropertyKeys(), filter);

        if (facing == null || facing.getValueClass() != EnumFacing.class) return ApplyResult.NotApplicable;

        if (curr.getValue(facing) == target.getValue(facing)) return ApplyResult.DidNothing;

        state.setValue(curr.withProperty(facing, target.getValue(facing)));

        return ApplyResult.DidSomething;
    }
}
