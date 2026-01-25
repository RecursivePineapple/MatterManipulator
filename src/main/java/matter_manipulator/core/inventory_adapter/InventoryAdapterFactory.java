package matter_manipulator.core.inventory_adapter;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface InventoryAdapterFactory {

    /// Tries to get an adapter for the given tile. The side may be null if there is no explicit side requested.
    /// If a side is required but not given, a sensible default is allowed to be used instead.
    /// @return The adapter, or null if this factory cannot adapt the given tile.
    @Nullable
    InventoryAdapter getAdapter(@NotNull TileEntity te, @Nullable EnumFacing side);
}
