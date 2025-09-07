package com.recursive_pineapple.matter_manipulator.common.building;

import java.util.Objects;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.recursive_pineapple.matter_manipulator.common.building.BlockAnalyzer.IBlockApplyContext;
import com.recursive_pineapple.matter_manipulator.common.building.providers.AECellItemProvider;
import com.recursive_pineapple.matter_manipulator.common.building.providers.BatteryItemProvider;
import com.recursive_pineapple.matter_manipulator.common.building.providers.IItemProvider;
import com.recursive_pineapple.matter_manipulator.common.building.providers.PatternItemProvider;
import com.recursive_pineapple.matter_manipulator.common.utils.InventoryAdapter;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

/**
 * An analysis for an inventory.
 */
public class InventoryAnalysis {

    public boolean mFuzzy;
    public IItemProvider[] mItems;

    public InventoryAnalysis() {}

    /**
     * Gets an analysis for an inventory.
     *
     * @param fuzzy When true, NBT will be ignored and items will be fuzzily-retrieved.
     */
    public static InventoryAnalysis fromInventory(IInventory inv, boolean fuzzy) {
        InventoryAdapter adapter = InventoryAdapter.findAdapter(inv);

        InventoryAnalysis analysis = new InventoryAnalysis();

        analysis.mFuzzy = fuzzy;
        analysis.mItems = new IItemProvider[adapter.getSizeInventory(inv)];

        for (int slot = 0; slot < analysis.mItems.length; slot++) {
            if (!adapter.isValidSlot(inv, slot)) continue;

            analysis.mItems[slot] = getProviderFor(inv.getStackInSlot(slot), fuzzy);
        }

        return analysis;
    }

    private static IItemProvider getProviderFor(ItemStack stack, boolean fuzzy) {
        if (stack == null || stack.getItem() == null) return null;

        if (Mods.AppliedEnergistics2.isModLoaded()) {
            if (!fuzzy) {
                IItemProvider cell = AECellItemProvider.fromWorkbenchItem(stack);
                if (cell != null) return cell;
            }

            IItemProvider pattern = PatternItemProvider.fromPattern(stack);
            if (pattern != null) return pattern;
        }

        IItemProvider battery = BatteryItemProvider.fromStack(stack);
        if (battery != null) return battery;

        return fuzzy ? new PortableItemStack(stack) : PortableItemStack.withNBT(stack);
    }

    /**
     * Applies the analysis
     *
     * @param consume When true, items will be consumed
     * @param simulate When true, the inventory will not be modified in any way
     * @return True when the inventory was successfully updated
     */
    public boolean apply(IBlockApplyContext context, IInventory inv, boolean consume, boolean simulate) {
        return apply(context, inv, InventoryAdapter.findAdapter(inv), consume, simulate);
    }

    private boolean apply(IBlockApplyContext context, IInventory inv, InventoryAdapter adapter, boolean consume, boolean simulate) {
        if (!adapter.validate(context, inv)) return false;

        if (adapter.getSizeInventory(inv) != mItems.length) {
            context.warn("Inventory was the wrong size (expected " + mItems.length + ", was " + adapter.getSizeInventory(inv) + ")");
            return false;
        }

        boolean didSomething = false;
        boolean success = true;

        for (int slot = 0; slot < mItems.length; slot++) {
            if (!adapter.isValidSlot(inv, slot)) continue;

            IItemProvider target = mItems[slot];
            IItemProvider actual = getProviderFor(inv.getStackInSlot(slot), mFuzzy);

            if (!Objects.equals(target, actual)) {
                ItemStack stack = inv.getStackInSlot(slot);
                if (stack != null) {
                    if (!adapter.canExtract(inv, slot)) {
                        context.warn("Could not extract item in slot " + slot + ": " + MMUtils.stripFormat(stack.getDisplayName()));
                        continue;
                    }

                    if (!simulate) {
                        stack = adapter.extract(inv, slot);
                        if (stack != null) didSomething = true;
                    }

                    if (stack != null && consume) context.givePlayerItems(stack);
                }

                if (target != null) {
                    if (!adapter.canInsert(inv, slot, target.getStack(null, false))) {
                        context.warn("Invalid item for slot " + slot + ": " + MMUtils.stripFormat(target.describe()));
                        continue;
                    }

                    ItemStack toInsert = target.getStack(context, consume);

                    if (toInsert == null) {
                        context.warn("Could not gather item for inventory: " + MMUtils.stripFormat(target.describe()));
                        success = false;
                    } else {
                        if (!simulate) {
                            if (adapter.insert(inv, slot, toInsert)) {
                                didSomething = true;
                            } else {
                                context.givePlayerItems(toInsert);
                            }
                        }
                    }
                }
            }
        }

        if (didSomething) inv.markDirty();

        return success;
    }

    @Override
    protected InventoryAnalysis clone() {
        InventoryAnalysis dup = new InventoryAnalysis();

        dup.mFuzzy = mFuzzy;
        dup.mItems = MMUtils.mapToArray(mItems, IItemProvider[]::new, x -> x == null ? null : x.clone());

        return dup;
    }
}
