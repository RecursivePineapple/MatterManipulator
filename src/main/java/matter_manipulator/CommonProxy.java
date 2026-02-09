package matter_manipulator;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import com.cleanroommc.modularui.factory.GuiManager;
import matter_manipulator.common.blocks.BlockHint;
import matter_manipulator.common.blocks.BlockUplinkController;
import matter_manipulator.common.items.ItemCluster;
import matter_manipulator.common.items.ItemHologramProjector;
import matter_manipulator.common.items.ItemMatterManipulator;
import matter_manipulator.common.items.MMItemList;
import matter_manipulator.common.items.MMMetaItem;
import matter_manipulator.common.items.ManipulatorTier;
import matter_manipulator.common.items.RecipeInstallUpgrade;
import matter_manipulator.common.networking.MMNetwork;
import matter_manipulator.common.ui.ManipulatorUIFactory;
import matter_manipulator.common.uplink.TileUplinkController;

public class CommonProxy {

    public static MMMetaItem META_ITEM = new MMMetaItem("metaitem");

    public static final BlockHint HINT_BLANK = new BlockHint("hint_blank");
    public static final BlockHint HINT_DOT = new BlockHint("hint_dot");
    public static final BlockHint HINT_WARNING = new BlockHint("hint_warning");
    public static final BlockHint HINT_X = new BlockHint("hint_x");

    public static final ItemHologramProjector HOLOGRAM_PROJECTOR = new ItemHologramProjector();

    public static final BlockUplinkController UPLINK_CONTROLLER = new BlockUplinkController();

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

        MMItemList.HologramProjector.set(registerItem(HOLOGRAM_PROJECTOR));

        registerItem(META_ITEM, false);
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
        MMMod.LOG.info("Registering Items");

        registerBlock(HINT_BLANK);
        registerBlock(HINT_DOT);
        registerBlock(HINT_WARNING);
        registerBlock(HINT_X);
        registerBlock(UPLINK_CONTROLLER);
        TileEntity.register(Tags.MODID + ":uplink", TileUplinkController.class);
    }

    public void registerRecipes(Register<IRecipe> event) {
        RecipeInstallUpgrade.register(event);
    }
}
