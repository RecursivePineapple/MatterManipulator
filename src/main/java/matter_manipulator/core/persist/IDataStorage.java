package matter_manipulator.core.persist;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

public interface IDataStorage {

    @NotNull
    IStateSandbox getSandbox(String domain, String name);

    @NotNull
    default IStateSandbox getSandbox(ResourceLocation resource) {
        return getSandbox(resource.getNamespace(), resource.getPath());
    }
}
