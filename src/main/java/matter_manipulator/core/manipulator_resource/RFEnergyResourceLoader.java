package matter_manipulator.core.manipulator_resource;

import java.util.Optional;

import net.minecraft.util.ResourceLocation;

import matter_manipulator.Tags;
import matter_manipulator.core.context.StackManipulatorContext;
import matter_manipulator.core.persist.IDataStorage;

public class RFEnergyResourceLoader implements ManipulatorResourceLoader<RFEnergyManipulatorResource> {

    @Override
    public ResourceLocation getResourceID() {
        return new ResourceLocation(Tags.MODID, "rf");
    }

    @Override
    public Optional<RFEnergyManipulatorResource> load(StackManipulatorContext context, IDataStorage storage) {
        return Optional.of(new RFEnergyManipulatorResource(context.getTier(), storage.getSandbox(getResourceID())));
    }
}
