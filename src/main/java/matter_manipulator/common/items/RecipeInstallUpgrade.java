package com.recursive_pineapple.matter_manipulator.common.items;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import com.github.bsideup.jabel.Desugar;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;

public class RecipeInstallUpgrade implements IRecipe {

    public static final RecipeInstallUpgrade INSTANCE = new RecipeInstallUpgrade();

    public static void register() {
        CraftingManager.getInstance().getRecipeList().add(INSTANCE);
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        if (getItemCount(inv) > 2) return false;

        var manipulator = findManipulator(inv);

        if (manipulator == null) return false;

        var upgrade = findUpgrade(inv, manipulator);

        return upgrade != null;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        if (getItemCount(inv) > 2) return null;

        var manipulator = findManipulator(inv);

        if (manipulator == null) return null;

        var upgrade = findUpgrade(inv, manipulator);

        if (upgrade == null) return null;

        ItemStack stack = manipulator.stack.copy();

        manipulator.state.installUpgrade(upgrade.upgrade);
        ItemMatterManipulator.setState(stack, manipulator.state);

        return stack;
    }

    @Override
    public int getRecipeSize() {
        return 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return null;
    }

    private static int getItemCount(InventoryCrafting inv) {
        int size = inv.getSizeInventory();
        int count = 0;

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack == null || stack.getItem() == null) continue;

            count++;
        }

        return count;
    }

    @Desugar
    private record ManipulatorInfo(ItemStack stack, int slot, MMState state, ItemMatterManipulator.ManipulatorTier tier) {}

    private static ManipulatorInfo findManipulator(InventoryCrafting inv) {
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack == null || stack.getItem() == null) continue;

            if (!(stack.getItem() instanceof ItemMatterManipulator manipulator)) continue;

            return new ManipulatorInfo(stack, i, ItemMatterManipulator.getState(stack), manipulator.tier);
        }

        return null;
    }

    @Desugar
    private record UpgradeInfo(ItemStack stack, int slot, MMUpgrades upgrade) {}

    private static UpgradeInfo findUpgrade(InventoryCrafting inv, ManipulatorInfo manipulator) {
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack == null || stack.getItem() == null) continue;

            if (!(stack.getItem() instanceof MetaItem)) continue;

            MMUpgrades upgrade = MMUpgrades.UPGRADES_BY_META.get(stack.getItemDamage());

            if (upgrade == null) continue;
            if (!manipulator.state.couldAcceptUpgrade(manipulator.tier, upgrade)) continue;

            return new UpgradeInfo(stack, i, upgrade);
        }

        return null;
    }
}
