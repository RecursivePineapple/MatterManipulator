package matter_manipulator.client.rendering.models;

import java.util.Collections;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.Tags;
import matter_manipulator.mixin.mixins.minecraft.AccessorMinecraft;

public class MachineStateMapper implements IStateMapper {

    private static final MachineStateMapper MAPPER_INSTNACE = new MachineStateMapper();
    private static final MachineModel MODEL_INSTANCE = new MachineModel();

    private static final ModelResourceLocation MACHINE = new ModelResourceLocation(new ResourceLocation(Tags.MODID, "machine"), "machine");

    public static void register(Block block) {
        ModelManager modelManager = ((AccessorMinecraft) Minecraft.getMinecraft()).getModelManager();

        modelManager.getBlockModelShapes().registerBlockWithStateMapper(block, MAPPER_INSTNACE);
    }

    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent event) {
        event.getModelRegistry().putObject(MACHINE, MODEL_INSTANCE);
    }

    @Override
    public @NotNull Map<IBlockState, ModelResourceLocation> putStateModelLocations(@NotNull Block block) {
        return Collections.singletonMap(block.getDefaultState(), MACHINE);
    }
}
