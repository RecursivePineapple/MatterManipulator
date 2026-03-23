package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.ApiStatus.Internal;

import matter_manipulator.core.item.InsertionItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.resources.ResourceProvider;

@Internal
public class ItemResourceProvider implements ResourceProvider<IntItemResourceStack> {

    private final MetaMap meta = new MetaMap();
    private final ItemStackIO[] ios;

    @Internal
    public ItemResourceProvider(ItemStackIO[] ios) {
        this.ios = ios;

        for (ItemStackIO io : ios) {
            io.setMetaContainer(meta);
        }
    }

    @Override
    public ItemResourceProviderFactory getFactory() {
        return ItemResourceProviderFactory.INSTANCE;
    }

    @Override
    public boolean canExtract(IntItemResourceStack request) {
        meta.clear();

        long amount = 0;

        ItemStackPredicate predicate = ItemStackPredicate.matches(request);

        for (ItemStackIO io : ios) {
            amount += io.getStoredAmount(predicate).orElse(0);
        }

        return amount >= request.getAmountInt();
    }

    @Override
    public IntItemResourceStack extract(IntItemResourceStack request) {
        meta.clear();

        IntItemResourceStack out = request.emptyCopy();

        ItemStackPredicate predicate = ItemStackPredicate.matches(request);

        for (ItemStackIO io : ios) {
            ItemStack result = io.pull(predicate, request.getAmountInt() - out.getAmountInt());

            if (!result.isEmpty()) {
                out.setAmountInt(out.getAmountInt() + result.getCount());
            }
        }

        return out;
    }

    @Override
    public IntItemResourceStack insert(IntItemResourceStack stack) {
        if (stack.isEmpty()) return IntItemResourceStack.EMPTY;

        meta.clear();

        InsertionItemStack insert = new InsertionItemStack(stack.toStack(stack.getAmountInt()));

        for (ItemStackIO io : ios) {
            insert.set(io.store(insert));

            if (insert.isEmpty()) break;
        }

        return insert.isEmpty() ? IntItemResourceStack.EMPTY : new ItemStackWrapper(insert.toStack());
    }
}
