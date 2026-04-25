package matter_manipulator.core.inventory_adapter;

import it.unimi.dsi.fastutil.ints.IntList;
import matter_manipulator.core.context.BlockPlacingContext;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceStack;

/// Something that wraps an object which stores some resource. This can contain a generic resource, but in practice it
/// will be an item or a fluid.  The 'inventory' part of the name is a misnomer, but it is the best metaphor for this
/// object.
public interface InventoryAdapter<R extends ResourceStack> {

    Resource<?> getResource();

    boolean validate(BlockPlacingContext context);

    IntList getSlots();

    boolean canExtract(int slot);

    boolean canInsert(int slot, R stack);

    R getStackInSlot(int slot);

    R extract(int slot);

    R insert(int slot, R stack);

}
