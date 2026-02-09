package matter_manipulator.common.interop.resetters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.interop.BlockResetter;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.item.ItemStackWrapper;

public class InventoryEmptier implements BlockResetter {

    @Override
    public List<ResourceStack> resetBlock(ManipulatorContext context, BlockPos pos) {
        TileEntity te = context.getWorld().getTileEntity(pos);

        if (te == null) return Collections.emptyList();

        List<ResourceStack> resources = new ArrayList<>();

        emptyInventory(resources, te, null);

        for (EnumFacing side : EnumFacing.VALUES) {
            emptyInventory(resources, te, side);
        }

        return resources;
    }

    private static void emptyInventory(List<ResourceStack> out, TileEntity te, EnumFacing side) {
        InventoryAdapter adapter = MMRegistriesInternal.getInventoryAdapter(te, side);

        if (adapter == null) return;

        for (int slot : adapter.getSlots().toIntArray()) {
            ItemStack stack = adapter.getStackInSlot(slot);

            if (stack == null || stack.isEmpty()) continue;

            if (!adapter.canExtract(slot)) continue;

            ItemStack extracted = adapter.extract(slot);

            if (extracted == null || extracted.isEmpty()) continue;

            out.add(new ItemStackWrapper(extracted));
        }
    }
}
