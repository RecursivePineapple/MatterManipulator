package matter_manipulator.common.structure.coords;

/// The structure definition-local coords. 0,0,0 is the front-most left-most top-most element. Coordinates increase as
/// you go down, right, or back.
public class StructureDefinitionCoords implements CoordinateSystem<StructureDefinitionCoords, StructureRelativeCoords> {

    public final int offsetX, offsetY, offsetZ;

    public StructureDefinitionCoords(int offsetX, int offsetY, int offsetZ) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
    }

    @Override
    public Position<StructureDefinitionCoords> translate(Position<StructureRelativeCoords> position) {
        return translate(position, offsetX, offsetY, offsetZ);
    }

    @Override
    public Position<StructureRelativeCoords> translateInverse(Position<StructureDefinitionCoords> position) {
        return translateInverse(position, offsetX, offsetY, offsetZ);
    }

    public static Position<StructureDefinitionCoords> translate(Position<StructureRelativeCoords> position, int offsetX, int offsetY, int offsetZ) {
        position.add(offsetX, offsetY, offsetZ);

        return CoordinateSystem.transmute(position);
    }

    public static Position<StructureRelativeCoords> translateInverse(Position<StructureDefinitionCoords> position, int offsetX, int offsetY, int offsetZ) {
        position.sub(offsetX, offsetY, offsetZ);

        return CoordinateSystem.transmute(position);
    }
}
