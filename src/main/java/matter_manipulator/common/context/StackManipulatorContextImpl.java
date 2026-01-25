package matter_manipulator.common.context;

import net.minecraft.item.ItemStack;

import matter_manipulator.common.state.MMState;
import matter_manipulator.core.context.StackManipulatorContext;

public class StackManipulatorContextImpl implements StackManipulatorContext {

    public ItemStack manipulator;
    public MMState state;

    public StackManipulatorContextImpl(ItemStack manipulator, MMState state) {
        this.manipulator = manipulator;
        this.state = state;
    }

    @Override
    public ItemStack getManipulator() {
        return manipulator;
    }

    @Override
    public MMState getState() {
        return state;
    }
}
