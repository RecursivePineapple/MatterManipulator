package matter_manipulator.core.resources.item;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus.Internal;

import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceStack;

public class ItemStackResourceProvider implements ResourceProvider {

    private final List<ItemStackIO> ios;

    @Internal
    public ItemStackResourceProvider(List<ItemStackIO> ios) {
        this.ios = ios;
    }

    @Override
    public ItemStackResourceProviderFactory getFactory() {
        return ItemStackResourceProviderFactory.INSTANCE;
    }

    public int extract(ItemStack stack) {
        //noinspection DataFlowIssue
        return (int) extract((ResourceStack) (Object) stack);
    }

    public boolean insert(ItemStack stack) {
        //noinspection DataFlowIssue
        return insert((ResourceStack) (Object) stack);
    }

    @Override
    public long extract(ResourceStack request) {
        return 0;
    }

    @Override
    public boolean insert(ResourceStack stack) {
        return false;
    }

}
