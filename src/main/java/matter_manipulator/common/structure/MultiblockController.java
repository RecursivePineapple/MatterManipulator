package matter_manipulator.common.structure;

import net.minecraft.util.math.BlockPos;

import matter_manipulator.common.data.ObjectWithBoundingBox;
import matter_manipulator.common.data.ObjectWithPosition;
import matter_manipulator.common.structure.coords.ControllerRelativeCoords;
import matter_manipulator.common.utils.enums.ExtendedFacing;

public interface MultiblockController<Self extends MultiblockController<Self>> extends ObjectWithPosition, ObjectWithBoundingBox {

    void onMachineUpdate(BlockPos pos);

    IStructureDefinition<? super Self> getDefinition();

    ExtendedFacing getOrientation();

    ControllerRelativeCoords getControllerPos();
}
