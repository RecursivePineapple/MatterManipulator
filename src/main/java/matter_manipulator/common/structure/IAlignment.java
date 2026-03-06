package matter_manipulator.common.structure;

import net.minecraft.util.EnumFacing;

import matter_manipulator.common.utils.enums.Direction;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

public interface IAlignment extends IAlignmentLimits, IAlignmentProvider {

    ExtendedFacing getExtendedFacing();

    /**
     * Set the facing without additional checks. Tools altering facing should use
     * {@link #toolSetExtendedFacing(ExtendedFacing)} instead.
     */
    void setExtendedFacing(ExtendedFacing alignment);

    IAlignmentLimits getAlignmentLimits();

    @Override
    default IAlignment getAlignment() {
        return this;
    }

    static int getAlignmentIndex(EnumFacing direction, Rotation rotation, Flip flip) {
        return (direction.ordinal() * ROTATIONS_COUNT + rotation.getIndex()) * FLIPS_COUNT + flip.getIndex();
    }

    default EnumFacing getDirection() {
        return getExtendedFacing().getDirection();
    }

    default void setDirection(EnumFacing direction) {
        setExtendedFacing(getExtendedFacing().with(direction));
    }

    default Rotation getRotation() {
        return getExtendedFacing().getRotation();
    }

    default void setRotation(Rotation rotation) {
        setExtendedFacing(getExtendedFacing().with(rotation));
    }

    default Flip getFlip() {
        return getExtendedFacing().getFlip();
    }

    default void setFlip(Flip flip) {
        setExtendedFacing(getExtendedFacing().with(flip));
    }

    default boolean toolSetDirection(EnumFacing direction) {
        if (direction == null || direction == null) {
            for (int i = 0, j = getDirection().ordinal() + 1, valuesLength = Direction.VALUES.length; i
                < valuesLength; i++) {
                if (toolSetDirection(Direction.VALUES[(j + i) % valuesLength].getFacing())) {
                    return true;
                }
            }
        } else {
            for (ExtendedFacing extendedFacing : ExtendedFacing.FOR_FACING.get(direction)) {
                if (checkedSetExtendedFacing(extendedFacing)) {
                    return true;
                }
            }
        }
        return false;
    }

    default boolean checkedSetDirection(EnumFacing direction) {
        if (isNewDirectionValid(direction)) {
            setDirection(direction);
            return true;
        }
        return false;
    }

    default boolean canSetToDirectionAny(EnumFacing direction) {
        for (ExtendedFacing extendedFacing : ExtendedFacing.FOR_FACING.get(direction)) {
            if (isNewExtendedFacingValid(extendedFacing)) {
                return true;
            }
        }
        return false;
    }

    default boolean toolSetRotation(Rotation rotation) {
        if (rotation == null) {
            int flips = Flip.VALUES.length;
            int rotations = Rotation.VALUES.length;
            for (int ii = 0, jj = getFlip().ordinal(); ii < flips; ii++) {
                for (int i = 1, j = getRotation().ordinal(); i < rotations; i++) {
                    if (checkedSetExtendedFacing(
                        ExtendedFacing.of(
                            getDirection(),
                            Rotation.VALUES[(j + i) % rotations],
                            Flip.VALUES[(jj + ii) % flips]))) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return checkedSetRotation(rotation);
        }
    }

    default boolean checkedSetRotation(Rotation rotation) {
        if (isNewRotationValid(rotation)) {
            setRotation(rotation);
            return true;
        }
        return false;
    }

    default boolean toolSetFlip(Flip flip) {
        if (flip == null) {
            for (int i = 1, j = getFlip().ordinal(), valuesLength = Flip.VALUES.length; i < valuesLength; i++) {
                if (checkedSetFlip(Flip.VALUES[(j + i) % valuesLength])) {
                    return true;
                }
            }
            return false;
        } else {
            return checkedSetFlip(flip);
        }
    }

    default boolean checkedSetFlip(Flip flip) {
        if (isNewFlipValid(flip)) {
            setFlip(flip);
            return true;
        }
        return false;
    }

    default boolean toolSetExtendedFacing(ExtendedFacing extendedFacing) {
        if (extendedFacing == null) {
            for (int i = 0, j = getExtendedFacing().ordinal() + 1, valuesLength = ExtendedFacing.VALUES.length; i
                < valuesLength; i++) {
                if (checkedSetExtendedFacing(ExtendedFacing.VALUES[(j + i) % valuesLength])) {
                    return true;
                }
            }
            return false;
        } else {
            return checkedSetExtendedFacing(extendedFacing);
        }
    }

    default boolean checkedSetExtendedFacing(ExtendedFacing alignment) {
        if (isNewExtendedFacingValid(alignment)) {
            setExtendedFacing(alignment);
            return true;
        }
        return false;
    }

    default boolean isNewDirectionValid(EnumFacing direction) {
        return isNewExtendedFacingValid(direction, getRotation(), getFlip());
    }

    default boolean isNewRotationValid(Rotation rotation) {
        return isNewExtendedFacingValid(getDirection(), rotation, getFlip());
    }

    default boolean isNewFlipValid(Flip flip) {
        return isNewExtendedFacingValid(getDirection(), getRotation(), flip);
    }

    default boolean isExtendedFacingValid() {
        return isNewExtendedFacingValid(getDirection(), getRotation(), getFlip());
    }

    @Override
    default boolean isNewExtendedFacingValid(EnumFacing direction, Rotation rotation, Flip flip) {
        return getAlignmentLimits().isNewExtendedFacingValid(direction, rotation, flip);
    }

    @Override
    default boolean isNewExtendedFacingValid(ExtendedFacing alignment) {
        return getAlignmentLimits()
            .isNewExtendedFacingValid(alignment.getDirection(), alignment.getRotation(), alignment.getFlip());
    }

    /**
     * Check if this object support a flip change, assuming both direction and rotation is not changed.
     */
    default boolean isFlipChangeAllowed() {
        ExtendedFacing facing = getExtendedFacing();
        for (Flip flip : Flip.VALUES) {
            if (flip == getFlip()) continue;
            if (isNewExtendedFacingValid(facing.with(flip))) return true;
        }
        return false;
    }

    /**
     * Check if this object support a rotation change, assuming both direction and flip is not changed.
     */
    default boolean isRotationChangeAllowed() {
        ExtendedFacing facing = getExtendedFacing();
        for (Rotation rotation : Rotation.VALUES) {
            if (rotation == getRotation()) continue;
            if (isNewExtendedFacingValid(facing.with(rotation))) return true;
        }
        return false;
    }
}
