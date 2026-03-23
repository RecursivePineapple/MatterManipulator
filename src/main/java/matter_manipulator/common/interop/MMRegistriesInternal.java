package matter_manipulator.common.interop;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.common.block_spec.StandardBlockSpecLoader;
import matter_manipulator.common.interop.block_adapters.AirBlockAdapter;
import matter_manipulator.common.interop.block_adapters.DroppedBlockAdapter;
import matter_manipulator.common.interop.block_adapters.ItemBlockSpecialAdapter;
import matter_manipulator.common.interop.block_adapters.RedstoneBlockAdapter;
import matter_manipulator.common.interop.block_adapters.SlabBlockAdapter;
import matter_manipulator.common.interop.block_adapters.StandardBlockAdapter;
import matter_manipulator.common.interop.block_state_transformers.PropertyCopyStateTransformer;
import matter_manipulator.common.interop.resetters.BlockRemover;
import matter_manipulator.common.interop.resetters.InventoryEmptier;
import matter_manipulator.common.interop.resetters.TankEmptier;
import matter_manipulator.common.inventory_adapter.ItemHandlerInventoryAdapterFactory;
import matter_manipulator.common.inventory_adapter.StandardInventoryAdapterFactory;
import matter_manipulator.common.modes.copying.CopyingManipulatorMode;
import matter_manipulator.common.modes.geometry.GeometryManipulatorMode;
import matter_manipulator.common.resources.item.ios.DroppingItemStackIOFactory;
import matter_manipulator.common.resources.item.ios.PlayerInventoryItemStackIOFactory;
import matter_manipulator.common.utils.deps.DependencyGraph;
import matter_manipulator.core.block_spec.ApplyResult;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.fluid.FluidStackIO;
import matter_manipulator.core.interop.BlockAdapter;
import matter_manipulator.core.interop.BlockResetter;
import matter_manipulator.core.interop.BlockStateTransformer;
import matter_manipulator.core.interop.MMRegistries;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.keybind.ManipulatorKeybind;
import matter_manipulator.core.manipulator_resource.ManipulatorResourceLoader;
import matter_manipulator.core.manipulator_resource.RFEnergyResourceLoader;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIOFactory;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.fluid.FluidResourceStack;
import matter_manipulator.core.resources.item.ItemResource;
import matter_manipulator.core.resources.item.ItemResourceProviderFactory;
import matter_manipulator.core.resources.item.ItemResourceStack;
import matter_manipulator.core.settings.ManipulatorSetting;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Internal
public class MMRegistriesInternal {

    public static final DependencyGraph<BlockResetter> BLOCK_RESETTERS = new DependencyGraph<>(new BlockResetter[0]);
    public static final DependencyGraph<ICopyInteropModule<?>> INTEROP_MODULES = new DependencyGraph<ICopyInteropModule<?>>(new ICopyInteropModule[0]);
    public static final DependencyGraph<InventoryAdapterFactory<? extends ItemResourceStack>> INV_ADAPTERS = new DependencyGraph<InventoryAdapterFactory<? extends ItemResourceStack>>(new InventoryAdapterFactory[0]);
    public static final DependencyGraph<InventoryAdapterFactory<? extends FluidResourceStack>> TANK_ADAPTERS = new DependencyGraph<InventoryAdapterFactory<? extends FluidResourceStack>>(new InventoryAdapterFactory[0]);
    public static final DependencyGraph<BlockAdapter> BLOCK_ADAPTERS = new DependencyGraph<>(new BlockAdapter[0]);
    public static final DependencyGraph<ResourceIOFactory<ItemStackIO>> ITEM_IO_FACTORIES = new DependencyGraph<ResourceIOFactory<ItemStackIO>>(new ResourceIOFactory[0]);
    public static final DependencyGraph<ResourceIOFactory<FluidStackIO>> FLUID_IO_FACTORIES = new DependencyGraph<ResourceIOFactory<FluidStackIO>>(new ResourceIOFactory[0]);
    public static final Map<ResourceLocation, ManipulatorMode<?, ?>> MODES = new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceLocation, ManipulatorSetting<?>> SETTINGS = new Object2ObjectOpenHashMap<>();
    public static final Map<Resource, ResourceProviderFactory> RESOURCES = new Object2ObjectOpenHashMap<>();
    public static final Object2ObjectMap<String, IBlockSpecLoader> LOADERS = new Object2ObjectOpenHashMap<>();
    public static Pair<Resource, ResourceProviderFactory>[] RESOURCE_ARRAY = new Pair[0];
    public static final Map<ResourceLocation, ManipulatorResourceLoader<?>> RESOURCE_LOADERS = new Object2ObjectOpenHashMap<>();
    public static final DependencyGraph<BlockStateTransformer> BLOCK_STATE_TRANSFORMERS = new DependencyGraph<>(new BlockStateTransformer[0]);
    public static final Map<ResourceLocation, ManipulatorKeybind> KEYBINDS = new HashMap<>();

    static {
        // Anything that removes the contents of a block. This includes things like emptying inventories and tanks.
        BLOCK_RESETTERS.addSubgraph("contents");
        // Anything that removes contained items
        BLOCK_RESETTERS.addSubgraph("contents/items");
        // Anything that removes contained fluids
        BLOCK_RESETTERS.addSubgraph("contents/fluids", "after:items");
        // Anything that changes the identity of a block prior to removal. This includes things like resetting the
        // colors on dynamically coloured blocks.
        BLOCK_RESETTERS.addSubgraph("identity", "after:contents");
        // Anything that actually removes the block.
        BLOCK_RESETTERS.addSubgraph("removing", "after:identity");

        BLOCK_RESETTERS.addObject("contents/items/empty-inv", new InventoryEmptier());
        BLOCK_RESETTERS.addObject("contents/fluids/empty-tank", new TankEmptier());
        BLOCK_RESETTERS.addObject("removing/block-remover", new BlockRemover());
    }

    static {
        // Anything that changes the identity of a block. These may erase tile settings.
        INTEROP_MODULES.addSubgraph("block-identity");
        // Any ephemeral settings for a tile (anything that is not saved when it is broken).
        INTEROP_MODULES.addSubgraph("tile-config", "after:block-identity");
    }

    static {
        INV_ADAPTERS.addObject("item-handler", new ItemHandlerInventoryAdapterFactory());
        INV_ADAPTERS.addObject("inventory", new StandardInventoryAdapterFactory(), "after:item-handler");
    }

    static {
        BLOCK_ADAPTERS.addObject("redstone", new RedstoneBlockAdapter(), "before:standard");
        BLOCK_ADAPTERS.addObject("air", new AirBlockAdapter(), "before:standard");
        BLOCK_ADAPTERS.addObject("special", new ItemBlockSpecialAdapter(), "before:standard");
        BLOCK_ADAPTERS.addObject("slab", new SlabBlockAdapter(), "before:standard");
        BLOCK_ADAPTERS.addObject("standard", new StandardBlockAdapter());
        BLOCK_ADAPTERS.addObject("dropped", new DroppedBlockAdapter(), "after:standard");
    }

    static {
        MMRegistries.registerResourceType(ItemResource.ITEMS, ItemResourceProviderFactory.INSTANCE);
    }

    static {
        // Anything external, like AE systems
        ITEM_IO_FACTORIES.addSubgraph("external");
        // Any backpacks or bags
        ITEM_IO_FACTORIES.addSubgraph("backpack", "after:external");
        // Any player inventories
        ITEM_IO_FACTORIES.addSubgraph("player", "after:backpack");
        // Anything that interact with the world directly (like items on the ground)
        ITEM_IO_FACTORIES.addSubgraph("world", "after:player");

        ITEM_IO_FACTORIES.addObject("player/player-inv", new PlayerInventoryItemStackIOFactory());
        ITEM_IO_FACTORIES.addObject("world/dump-on-ground", new DroppingItemStackIOFactory());
    }

    static {
        MMRegistries.registerManipulatorMode(new GeometryManipulatorMode());
        MMRegistries.registerManipulatorMode(new CopyingManipulatorMode());
    }

    static {
        MMRegistries.registerManipulatorResourceLoader(new RFEnergyResourceLoader());
    }

    static {
        MMRegistries.registerSpecLoader(StandardBlockSpecLoader.INSTANCE);
    }

    static {
        MMRegistries.blockStateTransformers().addObject("std-facing", new PropertyCopyStateTransformer("facing", EnumFacing.class));
        MMRegistries.blockStateTransformers().addObject("lever-facing", new PropertyCopyStateTransformer("facing", BlockLever.EnumOrientation.class));
        MMRegistries.blockStateTransformers().addObject("slab-half", new PropertyCopyStateTransformer("half", BlockSlab.EnumBlockHalf.class));
    }

    @Nullable
    public static InventoryAdapter<? extends ItemResourceStack> getInventoryAdapter(@Nonnull TileEntity te, @Nullable EnumFacing side) {
        for (var factory : INV_ADAPTERS.sorted()) {
            var adapter = factory.getAdapter(te, side);

            if (adapter != null) return adapter;
        }

        return null;
    }

    @Nullable
    public static InventoryAdapter<? extends FluidResourceStack> getTankAdapter(@Nonnull TileEntity te, @Nullable EnumFacing side) {
        for (var factory : TANK_ADAPTERS.sorted()) {
            var adapter = factory.getAdapter(te, side);

            if (adapter != null) return adapter;
        }

        return null;
    }

    @Nullable
    public static BlockAdapter getBlockAdapter(IBlockState state) {
        for (var adapter : BLOCK_ADAPTERS.sorted()) {
            if (adapter.canAdapt(state)) return adapter;
        }

        return null;
    }

    public static void transformBlock(MutableObject<IBlockState> state, IBlockState target, EnumSet<ApplyResult> result) {
        for (var transformer : BLOCK_STATE_TRANSFORMERS.sorted()) {
            result.add(transformer.transform(state, target));

            if (result.contains(ApplyResult.Error)) break;
        }
    }
}
