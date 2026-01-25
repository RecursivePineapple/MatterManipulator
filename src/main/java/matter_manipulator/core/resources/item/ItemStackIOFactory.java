package matter_manipulator.core.resources.item;

import java.util.Optional;

import matter_manipulator.core.context.ManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public interface ItemStackIOFactory {

    Optional<ItemStackIO> getIO(ManipulatorContext context, IDataStorage storage);

}
