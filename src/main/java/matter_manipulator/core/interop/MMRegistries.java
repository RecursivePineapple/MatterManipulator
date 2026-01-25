package matter_manipulator.core.interop;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.Pair;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.deps.IDependencyGraph;
import matter_manipulator.core.block_spec.ICopyInteropModule;
import matter_manipulator.core.interop.interfaces.BlockAdapter;
import matter_manipulator.core.interop.interfaces.BlockResetter;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.manipulator_resource.ManipulatorResourceLoader;
import matter_manipulator.core.resources.item.ItemStackIO;
import matter_manipulator.core.resources.item.ItemStackIOFactory;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.settings.ManipulatorSetting;

/// All registries available for third party mods to add their own Matter Manipulator integrations.
/// See the static initializers in [MMRegistriesInternal] for built-in targets.
@SuppressWarnings("rawtypes")
public class MMRegistries {

    /// These are called when a block is removed, to erase its configuration before returning any items to the player.
    public static IDependencyGraph<BlockResetter> blockResetters() {
        return MMRegistriesInternal.BLOCK_RESETTERS;
    }

    /// These are used to copy, save, load, and apply some configuration item from a block. They have free rein over
    /// any field or trait of the block, so long as they only affect the requested block.
    public static IDependencyGraph<ICopyInteropModule<?>> interop() {
        return MMRegistriesInternal.INTEROP_MODULES;
    }

    /// [InventoryAdapterFactory]s are iterated in order until one returns a non-null [InventoryAdapter], which is used
    /// to inspect and modify inventories. Many machines have custom inventory logic that cannot be represented through
    /// an [IInventory], and this is the mechanism through which that logic is expressed (for manipulators).
    public static IDependencyGraph<InventoryAdapterFactory> inventoryAdapters() {
        return MMRegistriesInternal.INV_ADAPTERS;
    }

    /// Block adapters are used to convert between a [Block] and an [ItemStack]. They do not handle drops from block
    /// destruction, those are handled by [BlockResetter]s. This is only used for determining which
    public static IDependencyGraph<BlockAdapter> blockAdapters() {
        return MMRegistriesInternal.BLOCK_ADAPTERS;
    }

    /// ItemStackIOFactories are used to create [ItemStackIO]s. They have full access to any piece of state on
    /// manipulators or the player, but caution must be taken to avoid corrupting anything. If an IO requires its own
    /// state, a [IDataStorage] object is provided to the factory. State modifications must be immediately flushed back
    /// to the [IDataStorage], and inserts or extracts must immediately update the world to prevent dupes or deletions.
    public static IDependencyGraph<ItemStackIOFactory> itemIOFactories() {
        return MMRegistriesInternal.ITEM_IO_FACTORIES;
    }

    public static void registerManipulatorMode(ManipulatorMode<?, ?> mode) {
        MMRegistriesInternal.MODES.put(mode.getModeID(), mode);
    }

    public static void registerManipulatorSetting(ResourceLocation id, ManipulatorSetting<?> mode) {
        MMRegistriesInternal.SETTINGS.put(id, mode);
    }

    public static <Provider extends ResourceProvider> void registerResourceType(Resource<Provider> resource, ResourceProviderFactory<Provider> factory) {
        MMRegistriesInternal.RESOURCES.put(resource, factory);
        MMRegistriesInternal.RESOURCE_ARRAY = MMRegistriesInternal.RESOURCES.entrySet()
            .stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .toArray(Pair[]::new);
    }

    public static void registerManipulatorResourceLoader(ManipulatorResourceLoader loader) {
        MMRegistriesInternal.RESOURCE_LOADERS.put(loader.getResourceID(), loader);
    }
}
