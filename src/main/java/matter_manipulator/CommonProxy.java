package matter_manipulator;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.cleanroommc.modularui.factory.GuiManager;
import matter_manipulator.common.items.ItemCluster;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.items.MMItemList;
import matter_manipulator.common.items.MMMetaItem;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.items.RecipeInstallUpgrade;
import matter_manipulator.common.networking.MMNetwork;
import matter_manipulator.common.ui.ManipulatorUIFactory;

public class CommonProxy {

    public static MMMetaItem META_ITEM;

    public void preInit(FMLPreInitializationEvent event) {
        MMMod.LOG.info("Loading Matter Manipulator version " + Tags.VERSION);

        GlobalMMConfig.init();
        MMNetwork.init();
        GuiManager.registerFactory(ManipulatorUIFactory.INSTANCE);
    }

    public void init(FMLInitializationEvent event) {

    }

    public void postInit(FMLPostInitializationEvent event) {

    }

    public void serverStarting(FMLServerStartingEvent event) {

    }

    public EntityPlayer getThePlayer() {
        return null;
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        MMMod.LOG.info("Registering Items");

        MMItemList.Cluster.set(registerItem(new ItemCluster()));

        MMItemList.MK0.set(registerItem(new ItemMatterManipulator(ManipulatorTier.Tier0)));
        MMItemList.MK1.set(registerItem(new ItemMatterManipulator(ManipulatorTier.Tier1)));
        MMItemList.MK2.set(registerItem(new ItemMatterManipulator(ManipulatorTier.Tier2)));
        MMItemList.MK3.set(registerItem(new ItemMatterManipulator(ManipulatorTier.Tier3)));

        registerItem(META_ITEM = new MMMetaItem("metaitem"), false);
    }

    public Block registerBlock(Block block) {
        return registerBlock(block, new ItemBlock(block));
    }

    public Block registerBlock(Block block, ItemBlock itemBlock) {
        ForgeRegistries.BLOCKS.register(block);
        registerItem(itemBlock.setRegistryName(block.getRegistryName()));
        return block;
    }

    public Item registerItem(Item item) {
        return registerItem(item, true);
    }

    public Item registerItem(Item item, boolean model) {
        ForgeRegistries.ITEMS.register(item);
        if (model) registerModel(item);
        return item;
    }

    public void registerModel(Item item) {

    }

    public void registerBlocks(Register<Block> event) {

    }

    public void registerRecipes(Register<IRecipe> event) {
        RecipeInstallUpgrade.register(event);
    }
}
