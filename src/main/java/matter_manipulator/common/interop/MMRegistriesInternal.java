package matter_manipulator.common.interop;

import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import matter_manipulator.common.interop.block_adapters.RedstoneBlockAdapter;
import matter_manipulator.common.interop.block_adapters.StandardBlockAdapter;
import matter_manipulator.common.interop.resetters.BlockRemover;
import matter_manipulator.common.inventory_adapter.ItemHandlerInventoryAdapterFactory;
import matter_manipulator.common.inventory_adapter.StandardInventoryAdapterFactory;
import matter_manipulator.common.modes.GeometryManipulatorMode;
import matter_manipulator.common.resources.item.ios.DroppingItemStackIOFactory;
import matter_manipulator.common.resources.item.ios.PlayerInventoryItemStackIOFactory;
import matter_manipulator.common.utils.deps.DependencyGraph;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.interop.MMRegistries;
import matter_manipulator.core.interop.interfaces.BlockAdapter;
import matter_manipulator.core.interop.interfaces.BlockResetter;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.manipulator_resource.ManipulatorResourceLoader;
import matter_manipulator.core.manipulator_resource.RFEnergyResourceLoader;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackIOFactory;
import matter_manipulator.core.resources.item.ItemStackResource;
import matter_manipulator.core.resources.item.ItemStackResourceProviderFactory;
import matter_manipulator.core.settings.ManipulatorSetting;

@Internal
public class MMRegistriesInternal {

    public static final DependencyGraph<BlockResetter> BLOCK_RESETTERS = new DependencyGraph<>();
    public static final DependencyGraph<ICopyInteropModule<?>> INTEROP_MODULES = new DependencyGraph<>();
    public static final DependencyGraph<InventoryAdapterFactory> INV_ADAPTERS = new DependencyGraph<>();
    public static final DependencyGraph<BlockAdapter> BLOCK_ADAPTERS = new DependencyGraph<>();
    public static final DependencyGraph<ItemStackIOFactory> ITEM_IO_FACTORIES = new DependencyGraph<>();
    public static final Map<ResourceLocation, ManipulatorMode<?, ?>> MODES = new Object2ObjectOpenHashMap<>();
    public static final Map<ResourceLocation, ManipulatorSetting<?>> SETTINGS = new Object2ObjectOpenHashMap<>();
    @SuppressWarnings("rawtypes")
    public static final Map<Resource, ResourceProviderFactory> RESOURCES = new Object2ObjectOpenHashMap<>();
    @SuppressWarnings("rawtypes")
    public static Pair<Resource, ResourceProviderFactory>[] RESOURCE_ARRAY = new Pair[0];
    public static final Map<ResourceLocation, ManipulatorResourceLoader<?>> RESOURCE_LOADERS = new Object2ObjectOpenHashMap<>();

    static {
        // Anything that empties inventories
        BLOCK_RESETTERS.addTarget("inventory");
        // Anything that empties tanks
        BLOCK_RESETTERS.addTarget("tanks");

        // Anything that removes the contents of a block. The block may drop items if its identity is changed prior to
        // this target.
        BLOCK_RESETTERS.addTarget("contents", "after:inventory", "after:tanks");

        // Anything that changes the 'identity' of a block (colour, direction, connectivity, etc).
        BLOCK_RESETTERS.addTarget("identity", "after:contents");

        // Anything that removes the block entirely
        BLOCK_RESETTERS.addTarget("removed", "after:identity");

        BLOCK_RESETTERS.addObject("block-remover", new BlockRemover(), "after:identity", "before:removed");
    }

    static {
        // Anything that rotates a block
        INTEROP_MODULES.addTarget("rotate");
        // Anything that connects a block to other blocks
        INTEROP_MODULES.addTarget("connect");
        // Anything that colours a block
        INTEROP_MODULES.addTarget("colour");

        // Anything that configures a block, without configuring its tile entity. These may erase or invalidate tile
        // settings.
        INTEROP_MODULES.addTarget("configure-block", "after:rotate", "after:connect", "after:colour");

        // Anything that configures a tile entity
        INTEROP_MODULES.addTarget("configure-tile", "after:configure-block");
    }

    static {
        INV_ADAPTERS.addObject("item-handler", new ItemHandlerInventoryAdapterFactory());
        INV_ADAPTERS.addObject("inventory", new StandardInventoryAdapterFactory(), "after:item-handler");
    }

    static {
        BLOCK_ADAPTERS.addObject("redstone", new RedstoneBlockAdapter(), "before:standard");
        BLOCK_ADAPTERS.addObject("standard", new StandardBlockAdapter());
    }

    static {
        RESOURCES.put(ItemStackResource.ITEMS, ItemStackResourceProviderFactory.INSTANCE);
    }

    static {
        // Anything bound to the player entity: their inv, backpacks, baubles, etc
        ITEM_IO_FACTORIES.addTarget("player-start");

        // Anything backpack-like that can store items
        ITEM_IO_FACTORIES.addTarget("backpacks-start");
        ITEM_IO_FACTORIES.addTarget("backpacks-end", "after:backpacks-start");

        ITEM_IO_FACTORIES.addObject("player-inv", new PlayerInventoryItemStackIOFactory(), "after:backpacks-end", "before:player-end");

        ITEM_IO_FACTORIES.addTarget("player", "after:player-start");

        ITEM_IO_FACTORIES.addObject("dump-on-ground", new DroppingItemStackIOFactory(), "after:player-end");
    }

    static {
        MMRegistries.registerManipulatorMode(new GeometryManipulatorMode());
    }

    static {
        MMRegistries.registerManipulatorResourceLoader(new RFEnergyResourceLoader());
    }

    @Nullable
    public static InventoryAdapter getInventoryAdapter(@Nonnull TileEntity te, @Nullable EnumFacing side) {
        for (var factory : INV_ADAPTERS.sorted()) {
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

    @Nullable
    public static BlockAdapter getBlockAdapter(ResourceStack resource) {
        for (var adapter : BLOCK_ADAPTERS.sorted()) {
            if (adapter.canAdapt(resource)) return adapter;
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Pair<Resource, ResourceProviderFactory> getResourceForStack(ResourceStack stack) {
        for (var pair : RESOURCE_ARRAY) {
            if (pair.right().supports(stack)) {
                return pair;
            }
        }

        return null;
    }
}
