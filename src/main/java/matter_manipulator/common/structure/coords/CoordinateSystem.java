package matter_manipulator.common.structure.coords;

import matter_manipulator.common.utils.enums.ExtendedFacing;

/// A coordinate system is a statically-checked helper for determining which coordinates a [Position] has.
/// Translation methods never copy a [Position] - if a position must remain unchanged, it must be copied
/// separately. Most coordinate systems will have static methods that allow you to avoid allocations, though some (such
/// as the ones provided by [ExtendedFacing#asCoordinateSystem()]) have no reason to be static.
public interface CoordinateSystem<Self extends CoordinateSystem<Self, Parent>, Parent extends CoordinateSystem<Parent, ?>> {

    /// Translates a position from the parent coordinate system into this one.
    Position<Self> translate(Position<Parent> position);

    /// Translates a position from this coordinate system into the parent.
    Position<Parent> translateInverse(Position<Self> position);

    /// Helper method that converts a position's coordinate system. Should not be used unless you know it's correct.
    static <P1 extends CoordinateSystem<P1, ?>, P2 extends CoordinateSystem<P2, ?>> Position<P1> transmute(Position<P2> pos) {
        // noinspection unchecked
        return (Position<P1>) pos;
    }
}
