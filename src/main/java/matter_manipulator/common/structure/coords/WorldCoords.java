package matter_manipulator.common.structure.coords;

/**
 * World block coordinates
 */
public class WorldCoords implements CoordinateSystem<WorldCoords, WorldCoords> {

    public static final WorldCoords INSTANCE = new WorldCoords();

    private WorldCoords() {}

    @Override
    public Position<WorldCoords> translate(Position<WorldCoords> position) {
        return position;
    }

    @Override
    public Position<WorldCoords> translateInverse(Position<WorldCoords> position) {
        return position;
    }
}
