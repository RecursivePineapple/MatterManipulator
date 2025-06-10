package com.recursive_pineapple.matter_manipulator.client.nei;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.recursive_pineapple.matter_manipulator.common.items.MMItemList;
import com.recursive_pineapple.matter_manipulator.common.items.MMItems;
import com.recursive_pineapple.matter_manipulator.common.items.MMUpgrades;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.ItemMatterManipulator;
import com.recursive_pineapple.matter_manipulator.common.items.manipulator.MMState;

import codechicken.nei.recipe.ShapelessRecipeHandler;

public class UpdateRecipeNEIHandler extends ShapelessRecipeHandler {

    @Override
    public String getOverlayIdentifier() {
        return "mm.recipe.upgrades";
    }

    @Override
    public String getRecipeTabName() {
        return getRecipeName();
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal(getOverlayIdentifier());
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("crafting") || outputId.equals(getOverlayIdentifier())) {
            loadRecipe(MMItemList.MK0.get(1));
            loadRecipe(MMItemList.MK1.get(1));
            loadRecipe(MMItemList.MK2.get(1));
            loadRecipe(MMItemList.MK3.get(1));
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result != null && result.getItem() instanceof ItemMatterManipulator) {
            loadRecipe(result);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient != null && ingredient.getItem() instanceof ItemMatterManipulator) {
            loadRecipe(ingredient);
        }

        if (ingredient != null && ingredient.getItem() == MMItems.META_ITEM) {
            MMUpgrades upgrade = MMUpgrades.UPGRADES_BY_META.get(ingredient.itemDamage);

            if (upgrade != null) {
                for (var tier : ItemMatterManipulator.ManipulatorTier.values()) {
                    if (!tier.allowedUpgrades.contains(upgrade)) continue;

                    arecipes.add(
                        new CachedShapelessRecipe(
                            new Object[] {
                                tier.container.get(1),
                                ingredient.copy(),
                            },
                            modifyMM(tier.container.get(1), upgrade)
                        )
                    );
                }
            }
        }
    }

    private void loadRecipe(ItemStack mm) {
        MMState state = ItemMatterManipulator.getState(mm);

        List<MMUpgrades> upgrades = new ArrayList<>(state.manipulator.tier.allowedUpgrades);
        upgrades.sort(Comparator.comparingInt(Enum::ordinal));

        upgrades.removeIf(upgrade -> !state.couldAcceptUpgrade(state.manipulator.tier, upgrade));

        for (MMUpgrades upgrade : upgrades) {
            arecipes.add(
                new CachedShapelessRecipe(
                    new Object[] {
                        mm.copy(),
                        upgrade.getStack(),
                    },
                    modifyMM(mm, upgrade)
                )
            );
        }
    }

    private ItemStack modifyMM(ItemStack mm, MMUpgrades toInstall) {
        mm = mm.copy();

        MMState state = ItemMatterManipulator.getState(mm);

        state.installUpgrade(toInstall);

        ItemMatterManipulator.setState(mm, state);

        return mm;
    }
}
