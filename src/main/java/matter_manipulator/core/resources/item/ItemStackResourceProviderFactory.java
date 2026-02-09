package matter_manipulator.core.resources.item;

import java.util.Collections;

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
    public ItemStackResourceProvider createProvider(ManipulatorContext context) {
        return new ItemStackResourceProvider(Collections.emptyList());
    }
}
