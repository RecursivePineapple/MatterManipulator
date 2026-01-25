package matter_manipulator.common.utils.items;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import matter_manipulator.core.item.ItemId;
import matter_manipulator.core.resources.item.ItemStackSizeCalculator;
import matter_manipulator.mixin.mixins.minecraft.AccessorItemStack;

public class ItemUtils {

    public static List<ItemStack> wrapInventory(IInventory inv) {
        int sizeInventory = inv.getSizeInventory();

        return new AbstractList<>() {

            @Override
            public ItemStack get(int index) {
                return inv.getStackInSlot(index);
            }

            @Override
            public ItemStack set(int index, ItemStack element) {
                ItemStack existing = inv.getStackInSlot(index);
                inv.setInventorySlotContents(index, element);
                return existing;
            }

            @Override
            public int size() {
                return sizeInventory;
            }
        };
    }

    public static void compactStandardInventory(IInventory inv) {
        inv.markDirty();

        ItemStackSizeCalculator stackSizes = (slot, stack) -> Math
            .min(inv.getInventoryStackLimit(), stack == null ? 64 : stack.getMaxStackSize());

        compactInventory(wrapInventory(inv), stackSizes);
    }

    public static void compactInventory(List<ItemStack> inv, ItemStackSizeCalculator stackSizes) {
        int len = inv.size();

        // Filter each ItemStack into their own lists (grouped by Item, meta, and NBT).
        Map<ItemStack, ObjectArrayList<ObjectIntPair<ItemStack>>> slots = new Object2ObjectOpenCustomHashMap<>(
            ItemId.STACK_ITEM_META_NBT_STRATEGY);

        for (int i = 0; i < len; i++) {
            ItemStack stack = inv.get(i);

            if (stack == null) continue;

            slots.computeIfAbsent(stack, ignored -> new ObjectArrayList<>())
                .add(ObjectIntPair.of(stack, i));
        }

        // For each ItemStack, merge stacks from the end of the list to the front
        slots.forEach((ignored, stacks) -> {
            int stackLen = stacks.size();

            int insert = 0;
            int extract = stackLen - 1;

            while (insert < stackLen && insert < extract) {
                // Grab the next stack from the front of the list, to insert into if possible
                var toInflate = stacks.get(insert);
                ItemStack inflateStack = toInflate.left();

                int maxStack = stackSizes.getSlotStackLimit(toInflate.rightInt(), inflateStack);
                int remaining = maxStack - inflateStack.getCount();

                // Scan from the end of the list to the current stack, and try to move items from those stacks into the
                // current stack
                while (insert < extract) {
                    var toBeExtracted = stacks.get(extract);

                    int toTransfer = Math.min(toBeExtracted.left().getCount(), remaining);

                    toBeExtracted.left().shrink(toTransfer);
                    inflateStack.grow(toTransfer);
                    remaining -= toTransfer;

                    if (toBeExtracted.left().getCount() <= 0) {
                        inv.set(toBeExtracted.rightInt(), null);
                        extract--;
                    }

                    if (inflateStack.getCount() >= maxStack) {
                        break;
                    }
                }

                insert++;
            }
        });

        int insert = 0;

        // Put all stacks into the first slots, contiguously
        while (insert < len) {
            if (inv.get(insert) == null) {
                ItemStack stack = null;

                int extract = insert + 1;

                while (extract < len && (stack = inv.get(extract)) == null) {
                    extract++;
                }

                if (stack != null) {
                    inv.set(insert, stack);
                    inv.set(extract, null);
                } else {
                    break;
                }
            }

            insert++;
        }
    }

    public static Object2LongOpenHashMap<ItemId> getItemStackHistogram(Iterable<ItemStack> stacks) {
        return getItemStackHistogram(stacks, true);
    }

    public static Object2LongOpenHashMap<ItemId> getItemStackHistogram(Iterable<ItemStack> stacks, boolean NBTSensitive) {
        Object2LongOpenHashMap<ItemId> histogram = new Object2LongOpenHashMap<>();

        if (stacks == null) return histogram;

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;
            histogram.addTo(ItemId.create(stack), stack.getCount());
        }

        return histogram;
    }

    public static StackMapDiff getStackMapDiff(Object2LongOpenHashMap<ItemId> before, Object2LongOpenHashMap<ItemId> after) {
        HashSet<ItemId> keys = new HashSet<>();
        keys.addAll(before.keySet());
        keys.addAll(after.keySet());

        StackMapDiff diff = new StackMapDiff();

        for (ItemId id : keys) {
            long beforeAmount = before.getLong(id);
            long afterAmount = after.getLong(id);

            if (afterAmount < beforeAmount) {
                diff.removed.addTo(id, beforeAmount - afterAmount);
            } else if (beforeAmount < afterAmount) {
                diff.added.addTo(id, afterAmount - beforeAmount);
            }
        }

        return diff;
    }

    public static List<ItemStack> getStacksOfSize(Object2LongOpenHashMap<ItemId> map, int maxStackSize) {
        ArrayList<ItemStack> list = new ArrayList<>();

        map.forEach((item, amount) -> {
            while (amount > 0) {
                int toRemove = Math
                    .min(amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : amount.intValue(), maxStackSize);

                list.add(item.toStack(toRemove));

                amount -= toRemove;
            }
        });

        return list;
    }

    public static List<ItemStack> getStacksOfSize(List<ItemStack> map, int maxStackSize) {
        ArrayList<ItemStack> list = new ArrayList<>();

        map.forEach(stack -> {
            while (!stack.isEmpty()) {
                list.add(stack.splitStack(Math.min(stack.getCount(), maxStackSize)));
            }
        });

        return list;
    }

    public static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        return mergeStacks(stacks, false, false);
    }

    public static List<ItemStack> mergeStacks(List<ItemStack> stacks, boolean keepNBT, boolean NBTSensitive) {
        ArrayList<ItemStack> out = new ArrayList<>();
        HashMap<ItemId, ItemStack> map = new HashMap<>();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;

            ItemId id = NBTSensitive ? ItemId.create(stack) : ItemId.createWithoutNBT(stack);

            ItemStack match = map.get(id);

            if (match == null) {
                match = stack.copy();
                if (!keepNBT) match.setTagCompound(null);
                map.put(id, match);
                out.add(match);
            } else {
                match.grow(stack.getCount());
            }
        }

        return out;
    }

    public static Stream<ItemStack> streamInventory(IInventory inv) {
        return IntStream.range(0, inv.getSizeInventory())
            .mapToObj(inv::getStackInSlot);
    }

    public static ItemStack[] inventoryToArray(IInventory inv) {
        return inventoryToArray(inv, true);
    }

    public static ItemStack[] inventoryToArray(IInventory inv, boolean copyStacks) {
        ItemStack[] array = new ItemStack[inv.getSizeInventory()];

        for (int i = 0; i < array.length; i++) {
            array[i] = copyStacks ? inv.getStackInSlot(i).copy() : inv.getStackInSlot(i);
        }

        return array;
    }

    /**
     * Removes all items in an inventory without returning them.
     */
    public static void clearInventory(IInventory inv) {
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; i++) {
            inv.setInventorySlotContents(i, ItemStack.EMPTY);
        }

        inv.markDirty();
    }

    /**
     * Merges stacks together and does not preserve order within the inventory.
     * Array will never contain null indices.
     */
    public static ItemStack[] fromInventory(IInventory inventory) {
        List<ItemStack> merged = mergeStacks(Arrays.asList(inventoryToArray(inventory, false)));

        return merged.toArray(new ItemStack[0]);
    }

    /**
     * Doesn't merge stacks and preserves the order of stacks.
     * Empty indices will be null.
     */
    public static ItemStack[] fromInventoryNoMerge(IInventory inventory) {
        ItemStack[] out = new ItemStack[inventory.getSizeInventory()];

        for (int i = 0; i < out.length; i++) {
            ItemStack stack = inventory.getStackInSlot(i);

            out[i] = stack.isEmpty() ? null : stack;
        }

        return out;
    }

    public static boolean areStacksBasicallyEqual(ItemStack a, ItemStack b) {
        if (a == null || b == null) { return a == null && b == null; }

        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
    }

    public static int getStackMeta(ItemStack stack) {
        return Items.FEATHER.getDamage(stack);
    }

    public static @Nullable NBTTagCompound getCapTag(ItemStack stack) {
        CapabilityDispatcher caps = ((AccessorItemStack) (Object) stack).mm$getCapabilities();

        return caps == null ? null : caps.serializeNBT();
    }

    public static ItemStack copyWithAmount(ItemStack stack, int amount) {
        if (stack == ItemStack.EMPTY) return ItemStack.EMPTY;

        stack = stack.copy();

        stack.setCount(amount);

        return stack;
    }
}
