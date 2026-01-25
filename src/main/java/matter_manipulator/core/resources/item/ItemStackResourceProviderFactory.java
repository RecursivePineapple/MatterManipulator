package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.resources.ResourceProviderFactory;

public class ItemStackResourceProviderFactory implements ResourceProviderFactory<ItemStackResourceProvider> {

    public static final ItemStackResourceProviderFactory INSTANCE = new ItemStackResourceProviderFactory();

    private ItemStackResourceProviderFactory() { }

    @Override
    public ItemStackResource getResource() {
        return ItemStackResource.ITEMS;
    }

    @Override
    public boolean supports(Object stack) {
        return stack instanceof ItemStack || stack instanceof ImmutableItemStack;
    }

    @Override
    public boolean areEqual(Object a, Object b) {
        return false;
    }

    @Override
    public String getLocalizedName(Object stack, int multiplier) {
        return "";
    }

    @Override
    public ItemStackResourceProvider createProvider(ManipulatorContext context) {
        throw new UnsupportedOperationException();
    }
}
