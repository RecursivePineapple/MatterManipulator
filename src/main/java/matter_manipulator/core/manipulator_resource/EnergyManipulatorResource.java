package matter_manipulator.core.manipulator_resource;

public interface EnergyManipulatorResource extends ManipulatorResource {

    /// Extracts a given amount of charge from this energy resource.
    double extract(double amount);

    double getStored();
    double getCapacity();

}
