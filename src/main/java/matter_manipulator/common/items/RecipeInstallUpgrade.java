package matter_manipulator.common.items;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.registries.IForgeRegistryEntry.Impl;

import org.jetbrains.annotations.NotNull;

import com.github.bsideup.jabel.Desugar;
import matter_manipulator.Tags;
import matter_manipulator.common.state.MMState;

public class RecipeInstallUpgrade extends Impl<IRecipe> implements IRecipe {

    public static final RecipeInstallUpgrade INSTANCE = new RecipeInstallUpgrade();

    static {
        INSTANCE.setRegistryName(Tags.MODID, "mm-upgrade");
    }

    public static void register(Register<IRecipe> event) {
        event.getRegistry().register(INSTANCE);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inv, @NotNull World world) {
        if (getItemCount(inv) > 2) return false;

        var manipulator = findManipulator(inv);

        if (manipulator == null) return false;

        var upgrade = findUpgrade(inv, manipulator);

        return upgrade != null;
    }

    @Override
    public @NotNull ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
        if (getItemCount(inv) > 2) return ItemStack.EMPTY;

        var manipulator = findManipulator(inv);

        if (manipulator == null) return ItemStack.EMPTY;

        var upgrade = findUpgrade(inv, manipulator);

        if (upgrade == null) return ItemStack.EMPTY;

        ItemStack stack = manipulator.stack.copy();

        manipulator.state.installUpgrade(upgrade.upgrade);
        ItemMatterManipulator.setState(stack, manipulator.state);

        return stack;
    }

    @Override
    public @NotNull ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    private static int getItemCount(InventoryCrafting inv) {
        int size = inv.getSizeInventory();
        int count = 0;

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) continue;

            count++;
        }

        return count;
    }

    @Desugar
    private record ManipulatorInfo(ItemStack stack, int slot, MMState state, ManipulatorTier tier) {}

    private static ManipulatorInfo findManipulator(InventoryCrafting inv) {
        int size = inv.getSizeInventory();

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStackInSlot(i);

            if (stack == ItemStack.EMPTY) continue;

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

            if (stack == ItemStack.EMPTY) continue;

            if (!(stack.getItem() instanceof MMMetaItem)) continue;

            MMUpgrades upgrade = MMUpgrades.UPGRADES_BY_META.get(stack.getItemDamage());

            if (upgrade == null) continue;
            if (!manipulator.state.couldAcceptUpgrade(manipulator.tier, upgrade)) continue;

            return new UpgradeInfo(stack, i, upgrade);
        }

        return null;
    }
}
