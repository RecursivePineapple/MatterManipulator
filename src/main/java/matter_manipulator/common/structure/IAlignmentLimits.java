package matter_manipulator.common.structure;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.util.EnumFacing;

import matter_manipulator.common.utils.enums.Direction;
import matter_manipulator.common.utils.enums.ExtendedFacing;
import matter_manipulator.common.utils.enums.Flip;
import matter_manipulator.common.utils.enums.Rotation;

public interface IAlignmentLimits {

    int DIRECTIONS_COUNT = Direction.VALUES.length;
    int ROTATIONS_COUNT = Rotation.VALUES.length;
    int FLIPS_COUNT = Flip.VALUES.length;
    int STATES_COUNT = ExtendedFacing.STATES_COUNT;

    /**
     * Can be placed in any orientation.
     */
    IAlignmentLimits UNLIMITED = (direction, rotation, flip) -> true;

    /**
     * Must be standing upright; i.e., top side of the block facing up.
     */
    IAlignmentLimits UPRIGHT = (direction, rotation, flip) -> direction.getYOffset() == 0
        && (flip.isVerticallyFlipped() ? rotation.isUpsideDown() : rotation.isNotRotated());

    /**
     * Can be in any orientation except upside down; i.e., top side of the block facing down.
     */
    IAlignmentLimits NOT_UPSIDE_DOWN = (direction, rotation, flip) -> direction.getYOffset() != 0
        || (flip.isVerticallyFlipped() ? !rotation.isNotRotated() : !rotation.isUpsideDown());

    /**
     * Front side of the block must be facing up.
     */
    IAlignmentLimits FACING_UP = (direction, rotation, flip) -> direction == EnumFacing.UP;

    boolean isNewExtendedFacingValid(EnumFacing direction, Rotation rotation, Flip flip);

    default boolean isNewExtendedFacingValid(ExtendedFacing alignment) {
        return isNewExtendedFacingValid(alignment.getDirection(), alignment.getRotation(), alignment.getFlip());
    }

    static IAlignmentLimits allowOnly(ExtendedFacing... allowedFacings) {
        Builder builder = Builder.denyAll();
        for (ExtendedFacing allowedFacing : allowedFacings) {
            builder.allow(allowedFacing);
        }
        return builder.build();
    }

    static IAlignmentLimits denyOnly(ExtendedFacing... allowedFacings) {
        Builder builder = Builder.allowAll();
        for (ExtendedFacing allowedFacing : allowedFacings) {
            builder.deny(allowedFacing);
        }
        return builder.build();
    }

    class Builder {

        protected final boolean[] validStates = new boolean[STATES_COUNT];

        private Builder() {}

        public static Builder allowAll() {
            Builder b = new Builder();
            Arrays.fill(b.validStates, true);
            return b;
        }

        public static Builder denyAll() {
            Builder b = new Builder();
            Arrays.fill(b.validStates, true);
            return b;
        }

        public Builder deny(EnumFacing fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = false);
            return this;
        }

        public Builder allow(EnumFacing fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = true);
            return this;
        }

        public Builder deny(ExtendedFacing o) {
            validStates[o.getIndex()] = false;
            return this;
        }

        public Builder allow(ExtendedFacing o) {
            validStates[o.getIndex()] = true;
            return this;
        }

        public Builder deny(Rotation fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = false);
            return this;
        }

        public Builder allow(Rotation fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = true);
            return this;
        }

        public Builder deny(Flip fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = false);
            return this;
        }

        public Builder allow(Flip fd) {
            ExtendedFacing.getAllWith(fd).stream().mapToInt(ExtendedFacing::getIndex)
                .forEach(v -> validStates[v] = true);
            return this;
        }

        public Builder filter(Function<ExtendedFacing, Optional<Boolean>> predicate) {
            for (ExtendedFacing value : ExtendedFacing.VALUES) {
                predicate.apply(value).ifPresent(bool -> validStates[value.getIndex()] = bool);
            }
            return this;
        }

        public Builder ensureDuplicates() {
            for (ExtendedFacing value : ExtendedFacing.VALUES) {
                if (validStates[value.getIndex()]) {
                    validStates[value.getDuplicate().getIndex()] = true;
                }
            }
            return this;
        }

        /**
         * Prefers rotation to flip, so both flip will get translated to opposite rotation and no flip
         *
         * @param flip the preferred flip to be used Horizontal or vertical
         * @return this
         */
        public Builder ensureNoDuplicates(Flip flip) {
            if (flip == Flip.BOTH || flip == Flip.NONE) {
                throw new IllegalArgumentException("Preffered Flip must be Horizontal or Vertical");
            }
            flip = flip.getOpposite();
            for (ExtendedFacing value : ExtendedFacing.VALUES) {
                if (validStates[value.getIndex()]) {
                    if (value.getFlip() == Flip.BOTH || value.getFlip() == flip) {
                        validStates[value.getIndex()] = false;
                        validStates[value.getDuplicate().getIndex()] = true;
                    }
                }
            }
            return this;
        }

        public IAlignmentLimits build() {
            return new AlignmentLimits(validStates);
        }
    }
}
