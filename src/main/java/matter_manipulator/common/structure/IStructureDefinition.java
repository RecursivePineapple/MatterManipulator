package matter_manipulator.common.structure;

import matter_manipulator.common.structure.coords.Position;
import matter_manipulator.common.structure.coords.StructureDefinitionCoords;
import matter_manipulator.common.structure.coords.StructureRelativeCoords;
import matter_manipulator.common.structure.impl.StructureDefinitionBuilder;

public interface IStructureDefinition<T> {

    StructureDefinitionCoords getPartOffset(String part);

    IStructureElement<? super T> getStructureElement(String part, Position<StructureDefinitionCoords> pos);
    Position<StructureRelativeCoords> getMinCorner(String part);
    Position<StructureRelativeCoords> getMaxCorner(String part);

    void iterate(StructureContext<T> context, StructureWalker<T> walker);

    /// Checks a specific element within a part
    boolean checkElement(StructureContext<T> context, Position<StructureDefinitionCoords> pos);

    /// Scans a whole part and returns true when all elements match.
    boolean checkPart(StructureContext<T> context);

    void build(StructureContext<T> context);

    /// Emits hints for a part.
    void emitHints(StructureContext<T> context);

    static <T> StructureDefinitionBuilder<T> builder() {
        return new StructureDefinitionBuilder<>();
    }
}
