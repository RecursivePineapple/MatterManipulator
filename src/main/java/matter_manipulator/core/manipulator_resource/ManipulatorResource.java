package matter_manipulator.core.manipulator_resource;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;
import matter_manipulator.core.persist.IStateSandbox;

/// Some sort of resource storage for a matter manipulator. This can be anything, but fluids must instead implement
/// [FluidManipulatorResource] and energies must implement [EnergyManipulatorResource].
/// Operations on resources must be immediately flushed to the backing data storage to prevent dupes and other
/// synchronization bugs. To achieve this, the [IStateSandbox] retrieved from the [IDataStorage] in
/// [ManipulatorResourceLoader#load(ManipulatorContext, IDataStorage)] should be stored in the [ManipulatorResource]. If
/// this resource interacts with external machines or stacks, care should be taken to avoid 'use-after-invalidate' bugs.
/// As an example, try to avoid [ItemStack] references to persistent stacks and instead store a coordinate or a slot
/// index.
public interface ManipulatorResource extends ICapabilityProvider {

    @Override
    default boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return false;
    }

    @Override
    @Nullable
    default <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return null;
    }

    default void addManipulatorTooltipInfo(ManipulatorContext context, List<String> lines) {

    }
}
