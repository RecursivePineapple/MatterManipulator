package matter_manipulator.core.interop;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.Pair;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.deps.IDependencyGraph;
import matter_manipulator.core.block_spec.IBlockSpecLoader;
import matter_manipulator.core.block_spec.IInteropModule;
import matter_manipulator.core.fluid.FluidStackIO;
import matter_manipulator.core.i18n.ILocalizer;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.inventory_adapter.InventoryAdapterFactory;
import matter_manipulator.core.item.ImmutableItemStack;
import matter_manipulator.core.item.ItemStackIO;
import matter_manipulator.core.keybind.ManipulatorKeybind;
import matter_manipulator.core.manipulator_resource.ManipulatorResourceLoader;
import matter_manipulator.core.modes.ManipulatorMode;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.resources.Resource;
import matter_manipulator.core.resources.ResourceIOFactory;
import matter_manipulator.core.resources.ResourceProvider;
import matter_manipulator.core.resources.ResourceProviderFactory;
import matter_manipulator.core.resources.fluid.FluidResourceStack;
import matter_manipulator.core.resources.item.IntItemResourceStack;
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
    public static IDependencyGraph<IInteropModule<?>> interop() {
        return MMRegistriesInternal.INTEROP_MODULES;
    }

    /// [InventoryAdapterFactory]s are iterated in order until one returns a non-null [InventoryAdapter], which is used
    /// to inspect and modify inventories. Many machines have custom inventory logic that cannot be represented through
    /// an [IInventory], and this is the mechanism through which that logic is expressed (for manipulators).
    public static IDependencyGraph<InventoryAdapterFactory<? extends IntItemResourceStack>> inventoryAdapters() {
        return MMRegistriesInternal.INV_ADAPTERS;
    }

    /// [InventoryAdapterFactory]s are iterated in order until one returns a non-null [InventoryAdapter], which is used
    /// to inspect and modify inventories. Many machines have custom inventory logic that cannot be represented through
    /// an [IInventory], and this is the mechanism through which that logic is expressed (for manipulators).
    public static IDependencyGraph<InventoryAdapterFactory<? extends FluidResourceStack>> tankAdapters() {
        return MMRegistriesInternal.TANK_ADAPTERS;
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
    public static IDependencyGraph<ResourceIOFactory<ItemStackIO>> itemIOFactories() {
        return MMRegistriesInternal.ITEM_IO_FACTORIES;
    }

    /// FluidStackIOFactories are used to create [FluidStackIO]s. They have full access to any piece of state on
    /// manipulators or the player, but caution must be taken to avoid corrupting anything. If an IO requires its own
    /// state, a [IDataStorage] object is provided to the factory. State modifications must be immediately flushed back
    /// to the [IDataStorage], and inserts or extracts must immediately update the world to prevent dupes or deletions.
    public static IDependencyGraph<ResourceIOFactory<FluidStackIO>> fluidIOFactories() {
        return MMRegistriesInternal.FLUID_IO_FACTORIES;
    }

    public static void registerManipulatorMode(ManipulatorMode<?, ?> mode) {
        MMRegistriesInternal.MODES.put(mode.getModeID(), mode);
    }

    public static void registerManipulatorSetting(ManipulatorSetting<?> mode) {
        MMRegistriesInternal.SETTINGS.put(mode.getSettingID(), mode);
    }

    public static <Provider extends ResourceProvider<?>> void registerResourceType(Resource<Provider> resource, ResourceProviderFactory<Provider> factory) {
        MMRegistriesInternal.RESOURCES.put(resource, factory);
        //noinspection unchecked
        MMRegistriesInternal.RESOURCE_ARRAY = MMRegistriesInternal.RESOURCES.entrySet()
            .stream()
            .map(e -> Pair.of(e.getKey(), e.getValue()))
            .toArray(Pair[]::new);
    }

    public static void registerManipulatorResourceLoader(ManipulatorResourceLoader loader) {
        MMRegistriesInternal.RESOURCE_LOADERS.put(loader.getResourceID(), loader);
    }

    public static void registerSpecLoader(IBlockSpecLoader loader) {
        MMRegistriesInternal.LOADERS.put(loader.getKey(), loader);
    }

    public static IDependencyGraph<BlockStateTransformer> blockStateTransformers() {
        return MMRegistriesInternal.BLOCK_STATE_TRANSFORMERS;
    }

    public static void registerKeybind(ManipulatorKeybind keybind) {
        MMRegistriesInternal.KEYBINDS.put(keybind.getKeybindId(), keybind);
    }

    public static ManipulatorKeybind getKeybind(ResourceLocation id) {
        return MMRegistriesInternal.KEYBINDS.get(id);
    }

    public static void registerLocalizer(ResourceLocation id, ILocalizer localizer) {
        ILocalizer existing = MMRegistriesInternal.LOCALIZERS.put(id, localizer);

        if (existing != null) {
            throw new IllegalArgumentException("Localizer ID " + id + " is already used by " + existing);
        }
    }

    public static void addFreeItem(ImmutableItemStack stack) {
        MMRegistriesInternal.FREE_ITEMS = DataUtils.concat(MMRegistriesInternal.FREE_ITEMS, stack);
    }
}
