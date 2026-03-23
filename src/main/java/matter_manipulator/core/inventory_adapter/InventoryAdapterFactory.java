package matter_manipulator.core.inventory_adapter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import matter_manipulator.core.resources.ResourceStack;

/// Something that can inspect a tile and retrieve some sort of inventory for a given resource. The inventory may
/// contain items, fluids, or anything else. The 'inventory' part of the name is a misnomer, but it is the best
/// metaphor for this object.
public interface InventoryAdapterFactory<R extends ResourceStack> {

    /// Tries to get an adapter for the given tile. The side may be null if there is no explicit side requested.
    /// If a side is required but not given, null must be returned.
    /// @return The adapter, or null if this factory cannot adapt the given tile.
    @Nullable
    InventoryAdapter<R> getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side);
}
