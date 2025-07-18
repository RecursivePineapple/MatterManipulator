package com.recursive_pineapple.matter_manipulator.common.building.providers;

import java.util.Arrays;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;

import com.recursive_pineapple.matter_manipulator.common.building.IPseudoInventory;
import com.recursive_pineapple.matter_manipulator.common.utils.BigItemStack;

import org.jetbrains.annotations.Nullable;

/**
 * An item provider that creates encoded AE patterns from blank patterns.
 */
public class PatternItemProvider implements IItemProvider {

    private Integer amount;
    private NBTTagCompound pattern;

    private static final IItemDefinition BLANK_PATTERN = AEApi.instance()
        .definitions()
        .materials()
        .blankPattern();
    private static final IItemDefinition PATTERN = AEApi.instance()
        .definitions()
        .items()
        .encodedPattern();

    public PatternItemProvider() {}

    public static PatternItemProvider fromPattern(ItemStack stack) {
        if (!PATTERN.isSameAs(stack)) { return null; }

        PatternItemProvider pattern = new PatternItemProvider();

        pattern.amount = stack.stackSize != 1 ? stack.stackSize : null;
        pattern.pattern = stack.getTagCompound();

        return pattern;
    }

    @Override
    public @Nullable ItemStack getStack(IPseudoInventory inv, boolean consume) {
        ItemStack stack = PATTERN.maybeStack(1).get();

        stack.stackSize = amount == null ? 1 : amount;
        stack.setTagCompound(pattern != null ? (NBTTagCompound) pattern.copy() : null);

        if (consume) {
            if (!inv.tryConsumeItems(Arrays.asList(BigItemStack.create(stack)), IPseudoInventory.CONSUME_REAL_ONLY).firstBoolean()) {
                ItemStack toConsume = BLANK_PATTERN.maybeStack(amount == null ? 1 : amount).get();
                if (!inv.tryConsumeItems(toConsume)) return null;
            }
        }

        return stack;
    }

    @Override
    public String describe() {
        return BLANK_PATTERN.maybeStack(1).get().getDisplayName();
    }

    @Override
    public PatternItemProvider clone() {
        PatternItemProvider dup = new PatternItemProvider();

        dup.amount = amount;
        dup.pattern = (NBTTagCompound) pattern.copy();

        return dup;
    }
}
