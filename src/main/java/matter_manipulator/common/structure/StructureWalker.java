package matter_manipulator.common.structure;

import matter_manipulator.common.structure.coords.Position;
import matter_manipulator.common.structure.coords.WorldCoords;

@FunctionalInterface
public interface StructureWalker<T> {

    boolean step(StructureContext<? extends T> context, Position<WorldCoords> pos, IStructureElement<? super T> element);

}
