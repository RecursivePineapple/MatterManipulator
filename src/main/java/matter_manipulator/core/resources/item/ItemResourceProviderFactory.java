package matter_manipulator.core.resources.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.persist.DataStorage;
import matter_manipulator.core.resources.ResourceProviderFactory;

public class ItemResourceProviderFactory implements ResourceProviderFactory<ItemResourceProvider> {

    public static final ItemResourceProviderFactory INSTANCE = new ItemResourceProviderFactory();

    private ItemResourceProviderFactory() { }

    @Override
    public ItemResource getResource() {
        return ItemResource.ITEMS;
    }

    @Override
    public ItemResourceProvider createProvider(ManipulatorContext context) {
        List<ItemStackIO> ios = new ArrayList<>();

        DataStorage storage = context.getState().ioState;

        for (var factory : MMRegistriesInternal.ITEM_IO_FACTORIES.sorted()) {
            Optional<ItemStackIO> io = factory.getIO(context, storage);

            if (io.isPresent()) ios.add(io.get());
        }

        return new ItemResourceProvider(ios.toArray(new ItemStackIO[0]));
    }
}
