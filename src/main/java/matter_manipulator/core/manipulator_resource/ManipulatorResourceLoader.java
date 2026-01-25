package matter_manipulator.core.manipulator_resource;

import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Contract;

import matter_manipulator.core.context.StackManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public interface ManipulatorResourceLoader<Resource extends ManipulatorResource> {

    @Contract(pure = true)
    ResourceLocation getResourceID();

    /// Loads a resource from the storage, or creates it if it doesn't already exist.
    Optional<Resource> load(StackManipulatorContext context, IDataStorage storage);
}
