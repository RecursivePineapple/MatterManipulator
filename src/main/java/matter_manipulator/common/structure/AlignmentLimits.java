package matter_manipulator.common.structure;

import net.minecraft.util.EnumFacing;

import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

class AlignmentLimits implements IAlignmentLimits {

    protected final boolean[] validStates;

    AlignmentLimits(boolean[] validStates) {
        this.validStates = validStates;
    }

    @Override
    public boolean isNewExtendedFacingValid(EnumFacing direction, Rotation rotation, Flip flip) {
        return validStates[IAlignment.getAlignmentIndex(direction, rotation, flip)];
    }
}
