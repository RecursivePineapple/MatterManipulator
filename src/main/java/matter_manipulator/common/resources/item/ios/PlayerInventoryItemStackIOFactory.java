package matter_manipulator.common.resources.item.ios;

import java.util.Optional;

import net.minecraft.entity.player.InventoryPlayer;

import net.minecraftforge.items.wrapper.InvWrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.resources.item.ItemHandlerIterator;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.item.ItemStackIO;
import matter_manipulator.core.resources.item.ItemStackIOFactory;
import matter_manipulator.core.resources.item.ItemStackIterator;
import matter_manipulator.core.resources.item.ItemStackIteratorBuilder;
import matter_manipulator.core.resources.item.ItemStackPredicate;

public class PlayerInventoryItemStackIOFactory implements ItemStackIOFactory {

    @Override
    public Optional<ItemStackIO> getIO(ManipulatorContext context, IDataStorage storage) {
        InventoryPlayer inv = context.getRealPlayer().inventory;

        return Optional.of(new ItemStackIO() {

            @Override
            @NotNull
            public ItemStackIteratorBuilder iterator() {
                return new ItemStackIteratorBuilder() {

                    private ItemStackPredicate filter;
                    private boolean patterns;

                    @Override
                    public ItemStackIteratorBuilder setItemFilter(@Nullable ItemStackPredicate filter) {
                        this.filter = filter;
                        return this;
                    }

                    @Override
                    public ItemStackIteratorBuilder iteratePatterns(boolean onlyPatterns) {
                        this.patterns = onlyPatterns;
                        return this;
                    }

                    @Override
                    public ItemStackIteratorBuilder setUsage(@NotNull Usage usage) {
                        return this;
                    }

                    @Override
                    public ItemStackIterator build() {
                        if (patterns) return ItemStackIterator.EMPTY;

                        return new ItemHandlerIterator(new InvWrapper(inv), filter);
                    }
                };
            }
        });
    }
}
