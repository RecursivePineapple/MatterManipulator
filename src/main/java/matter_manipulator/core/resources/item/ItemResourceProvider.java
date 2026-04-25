package matter_manipulator.core.resources.item;

import net.minecraft.item.ItemStack;

import matter_manipulator.common.resources.item.ios.FreeItemStackIOFactory;
import matter_manipulator.core.item.InsertionItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.item.ItemStackPredicate;
import matter_manipulator.core.meta.MetaMap;
import matter_manipulator.core.resources.ResourceProvider;

public class ItemResourceProvider implements ResourceProvider<IntItemResourceStack> {

    private final MetaMap meta = new MetaMap();
    private final ItemStackIO[] extractIOs, insertIOs;

    private static final FreeItemStackIOFactory FREE = new FreeItemStackIOFactory();

    public ItemResourceProvider(ItemStackIO[] ios) {
        this.extractIOs = new ItemStackIO[ios.length + 1];
        this.insertIOs = new ItemStackIO[ios.length + 1];

        extractIOs[0] = FREE;
        insertIOs[0] = FREE;

        System.arraycopy(ios, 0, insertIOs, 1, ios.length);

        for (int i = 0; i < ios.length; i++) {
            this.extractIOs[ios.length - i] = ios[i];
        }

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
        if (request.isEmpty()) return true;

        meta.clear();

        long amount = 0;

        ItemStackPredicate predicate = ItemStackPredicate.matches(request);

        for (ItemStackIO io : extractIOs) {
            amount += io.getStoredAmount(predicate).orElse(0);
        }

        return amount >= request.getAmountInt();
    }

    @Override
    public IntItemResourceStack extract(IntItemResourceStack request) {
        if (request.isEmpty()) return request.copy();

        meta.clear();

        IntItemResourceStack out = request.emptyCopy();

        ItemStackPredicate predicate = ItemStackPredicate.matches(request);

        for (ItemStackIO io : extractIOs) {
            ItemStack result = io.pull(predicate, request.getAmountInt() - out.getAmountInt());

            if (!result.isEmpty()) {
                out.setAmountInt(out.getAmountInt() + result.getCount());

                if (request.getAmountInt() == out.getAmountInt()) {
                    break;
                }
            }
        }

        return out;
    }

    @Override
    public IntItemResourceStack insert(IntItemResourceStack stack) {
        if (stack.isEmpty()) return IntItemResourceStack.EMPTY;

        meta.clear();

        InsertionItemStack insert = new InsertionItemStack(stack.toStack(stack.getAmountInt()));

        for (ItemStackIO io : insertIOs) {
            insert.set(io.store(insert));

            if (insert.isEmpty()) break;
        }

        return insert.isEmpty() ? IntItemResourceStack.EMPTY : new ItemStackWrapper(insert.toStack());
    }
}
