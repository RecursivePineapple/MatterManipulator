package matter_manipulator.client.rendering.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IUnlistedProperty;

import org.jetbrains.annotations.NotNull;

import matter_manipulator.common.utils.enums.ExtendedFacing;
import mcp.MethodsReturnNonnullByDefault;

public class MachineModelRegistry {

    /// Known machine model paths. {path with $machine suffix -> base model}
    private static final Map<ResourceLocation, ResourceLocation> MODELS = new HashMap<>();

    public static final IUnlistedProperty<ExtendedFacing> EXTENDED_FACING = new IUnlistedProperty<>() {

        @Override
        public String getName() {
            return "extended-facing";
        }

        @Override
        public boolean isValid(ExtendedFacing value) {
            return true;
        }

        @Override
        public Class<ExtendedFacing> getType() {
            return ExtendedFacing.class;
        }

        @Override
        public String valueToString(ExtendedFacing value) {
            return value.name().toLowerCase();
        }
    };

    public static void init() {
        ModelLoaderRegistry.registerLoader(new MachineModelLoader());
    }

    public static void register(ResourceLocation base) {
        MODELS.put(new ResourceLocation(base.getNamespace(), "models/" + base.getPath() + "#machine"), base);
    }

    private static class MachineModelLoader implements ICustomModelLoader {

        private IResourceManager manager;

        @Override
        public void onResourceManagerReload(@NotNull IResourceManager resourceManager) {
            this.manager = resourceManager;
        }

        @Override
        public boolean accepts(@NotNull ResourceLocation modelLocation) {
            return MODELS.containsKey(modelLocation);
        }

        @Override
        public @NotNull IModel loadModel(@NotNull ResourceLocation modelLocation) throws Exception {
            IModel actual = ModelLoaderRegistry.getModel(MODELS.get(modelLocation));

            return new RawMachineModel(actual);
        }
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    private static class RawMachineModel implements IModel {

        public final IModel base;

        public RawMachineModel(IModel base) {
            this.base = base;
        }

        @Override
        public Collection<ResourceLocation> getDependencies() {
            return base.getDependencies();
        }

        @Override
        public Collection<ResourceLocation> getTextures() {
            return base.getTextures();
        }

        @Override
        public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
            return new BakedMachineModel(base.bake(base.getDefaultState(), format, bakedTextureGetter), format, EXTENDED_FACING);
        }
    }
}
