package matter_manipulator.core.context;

import net.minecraft.util.math.BlockPos;

import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.i18n.Localized;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemResourceProvider;

public interface BlockPlacingContext extends BuildingContextBase {

    void setTarget(BlockPos pos, IBlockSpec spec);

    IBlockSpec getSpec();

    <Provider extends ResourceProvider<?>> Provider resource(Resource<Provider> resource);

    default ItemResourceProvider items() {
        return resource(ItemResource.ITEMS);
    }

    default void insert(Iterable<ResourceStack> stacks) {
        for (ResourceStack stack : stacks) {
            //noinspection unchecked
            resource(stack.getResource()).insert(stack);
        }
    }

    boolean drainEnergy(double multiplier);
    boolean drainEnergy(BlockPos pos, double multiplier);

    /// Removes the block at the current target (see [#setTarget(BlockPos, IBlockSpec)]).
    void removeBlock();

    void pushMessageContext(Localized context);
    void popMessageContext();

    void warn(Localized message);

    void error(Localized message);

    /// Emits a warning for the current block that a stack could not be extracted. To prevent spam, these are grouped by
    /// the resource identity and their amounts are summed. Each resource type will only print one message per build
    /// tick.
    void extractionFailure(ResourceStack stack);
}
