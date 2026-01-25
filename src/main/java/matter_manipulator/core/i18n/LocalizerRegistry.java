package matter_manipulator.core.i18n;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class LocalizerRegistry {

    private static final BiMap<ResourceLocation, ILocalizer> LOCALIZERS = HashBiMap.create();

    public static void register(ResourceLocation id, ILocalizer localizer) {
        ILocalizer existing = LOCALIZERS.put(id, localizer);

        if (existing != null) {
            throw new IllegalArgumentException("Localizer ID " + id + " is already used by " + existing);
        }
    }

    public static ILocalizer getLocalizer(ResourceLocation id) {
        return LOCALIZERS.get(id);
    }

    public static ResourceLocation getLocalizerID(ILocalizer message) {
        return LOCALIZERS.inverse().get(message);
    }
}
