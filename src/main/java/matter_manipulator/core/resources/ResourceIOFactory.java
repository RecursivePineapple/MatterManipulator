package matter_manipulator.core.resources;

import java.util.Optional;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public interface ResourceIOFactory<IO> {

    /// Creates a resource IO for a given manipulator context.
    /// @param storage A data storage that is shared among all IO factories.
    /// @return The IO, or [Optional#empty()] if this factory could not produce one.
    Optional<IO> getIO(ManipulatorContext context, IDataStorage storage);

}
