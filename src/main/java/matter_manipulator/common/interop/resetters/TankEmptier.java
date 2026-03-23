package matter_manipulator.common.interop.resetters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.interop.BlockResetter;
import matter_manipulator.core.inventory_adapter.InventoryAdapter;
import matter_manipulator.core.resources.ResourceStack;
import matter_manipulator.core.resources.fluid.FluidResourceStack;

public class TankEmptier implements BlockResetter {

    @Override
    public List<ResourceStack> resetBlock(ManipulatorContext context, BlockPos pos) {
        TileEntity te = context.getWorld().getTileEntity(pos);

        if (te == null) return Collections.emptyList();

        List<ResourceStack> resources = new ArrayList<>();

        emptyTank(resources, te, null);

        for (EnumFacing side : EnumFacing.VALUES) {
            emptyTank(resources, te, side);
        }

        return resources;
    }

    private static void emptyTank(List<ResourceStack> out, TileEntity te, EnumFacing side) {
        InventoryAdapter<? extends FluidResourceStack> adapter = MMRegistriesInternal.getTankAdapter(te, side);

        if (adapter == null) return;

        for (int slot : adapter.getSlots().toIntArray()) {
            ResourceStack stack = adapter.getStackInSlot(slot);

            if (stack == null || stack.isEmpty()) continue;

            if (!adapter.canExtract(slot)) continue;

            ResourceStack extracted = adapter.extract(slot);

            if (extracted == null || extracted.isEmpty()) continue;

            out.add(extracted);
        }
    }
}
