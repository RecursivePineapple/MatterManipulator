package matter_manipulator;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import matter_manipulator.client.rendering.MMRenderer;
import matter_manipulator.client.rendering.models.MachineModelRegistry;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        MachineModelRegistry.init();

        MachineModelRegistry.register(new ResourceLocation(Tags.MODID, "block/uplink-controller-off"));
        MachineModelRegistry.register(new ResourceLocation(Tags.MODID, "block/uplink-controller-idle"));
        MachineModelRegistry.register(new ResourceLocation(Tags.MODID, "block/uplink-controller-active"));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        MMRenderer.init();
    }

    @Override
    public void registerModel(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    @Override
    public void registerItems(Register<Item> event) {
        super.registerItems(event);

        META_ITEM.configureModels();
    }

    @Override
    public void registerBlocks(Register<Block> event) {
        super.registerBlocks(event);
    }

    @Override
    public EntityPlayer getThePlayer() {
        return FMLClientHandler.instance().getClientPlayerEntity();
    }
}
