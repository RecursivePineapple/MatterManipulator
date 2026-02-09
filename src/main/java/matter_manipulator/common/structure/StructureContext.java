package matter_manipulator.common.structure;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.structure.coords.Offset;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.color.ImmutableColor;

public interface StructureContext<T> {

    /// Arbitrary user data (usually the invoking tile entity).
    T getData();

    IStructureDefinition<? super T> getStructureDefinition();

    World getWorld();

    ExtendedFacing getOrientation();

    ControllerRelativeCoords getControllerPos();

    String getPartName();
    Offset<StructureRelativeCoords> getPartOffset();

    @Nullable
    EntityPlayer getPlayer();

    @Nullable
    ItemStack getTrigger();

    void emitHint(BlockPos pos, IBlockSpec spec, ImmutableColor tint);

    /// Attempts to consume one block of placing quota and returns true when this was possible.
    boolean consumePlaceQuota();
}
