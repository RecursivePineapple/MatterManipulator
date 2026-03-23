package matter_manipulator.core.inventory_adapter;

import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.fluid.FluidResource;
import matter_manipulator.core.resources.fluid.FluidResourceStack;
import matter_manipulator.core.resources.fluid.IntFluidResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemResourceStack;

public class EmptyInventoryAdapter<R extends ResourceStack> implements InventoryAdapter<R> {

    public static final EmptyInventoryAdapter<ItemResourceStack> ITEMS = new EmptyInventoryAdapter<>(
        ItemResource.ITEMS,
        IntItemResourceStack.EMPTY);

    public static final EmptyInventoryAdapter<FluidResourceStack> FLUIDS = new EmptyInventoryAdapter<>(
        FluidResource.FLUIDS,
        IntFluidResourceStack.EMPTY);

    private final Resource<?> resource;
    private final R empty;

    public EmptyInventoryAdapter(Resource<?> resource, R empty) {
        this.resource = resource;
        this.empty = empty;
    }

    @Override
    public Resource<?> getResource() {
        return resource;
    }

    @Override
    public boolean validate(BlockPlacingContext context) {
        return true;
    }

    @Override
    public IntList getSlots() {
        return IntList.of();
    }

    @Override
    public boolean canExtract(int slot) {
        return false;
    }

    @Override
    public boolean canInsert(int slot, R stack) {
        return false;
    }

    @Override
    public R getStackInSlot(int slot) {
        return empty;
    }

    @Override
    public R extract(int slot) {
        return empty;
    }

    @Override
    public R insert(int slot, R stack) {
        return stack;
    }
}
