package matter_manipulator.core.resources.item;

import java.util.List;

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

    @Override
    public boolean canExtract(ResourceStack request) {
        return true;
    }

    @Override
    public boolean extract(ResourceStack request) {
        if (!(request instanceof ItemResourceStack item)) return false;

        return true;
    }

    @Override
    public boolean insert(ResourceStack stack) {
        if (!(stack instanceof ItemResourceStack item)) return false;

        return true;
    }
}
