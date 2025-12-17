package matter_manipulator.client.nei;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

import com.gtnewhorizon.gtnhlib.eventbus.EventBusSubscriber;
import matter_manipulator.MMMod;
import matter_manipulator.Tags;
import matter_manipulator.common.items.MMItemList;

import codechicken.nei.api.IConfigureNEI;
import codechicken.nei.event.NEIRegisterHandlerInfosEvent;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.HandlerInfo;

@SuppressWarnings("unused")
@EventBusSubscriber(side = Side.CLIENT)
public class NEIMMConfig implements IConfigureNEI {

    private static final UpdateRecipeNEIHandler UPDATE_RECIPE_NEI_HANDLER = new UpdateRecipeNEIHandler();

    @Override
    public void loadConfig() {
        GuiCraftingRecipe.craftinghandlers.add(UPDATE_RECIPE_NEI_HANDLER);
        GuiUsageRecipe.usagehandlers.add(UPDATE_RECIPE_NEI_HANDLER);
    }

    @Override
    public String getName() {
        return "Matter Manipulator";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }

    @SubscribeEvent
    public static void registerHandlerInfo(NEIRegisterHandlerInfosEvent event) {
        event.registerHandlerInfo(
            new HandlerInfo.Builder(
                UPDATE_RECIPE_NEI_HANDLER.getOverlayIdentifier(),
                "Matter Manipulator",
                MMMod.MODID
            )
                .setMaxRecipesPerPage(100)
                .setDisplayStack(MMItemList.UpgradeBlank.get(1))
                .build()
        );
    }
}
