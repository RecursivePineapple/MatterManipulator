package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.MMMod;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.common.utils.Mods;

public class ResourceLocationJsonAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {

    @Override
    public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
        CommonName common = null;

        for (CommonName name : CommonName.values()) {
            if (name.mod.ID.equals(src.getNamespace()) && name.name.equals(src.getPath())) {
                common = name;
                break;
            }
        }

        if (common != null) {
            return new JsonPrimitive(common.ordinal());
        } else {
            if ("minecraft".equals(src.getNamespace())) {
                return new JsonPrimitive(src.getPath());
            } else {
                return new JsonPrimitive(src.toString());
            }
        }
    }

    @Override
    public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive primitive)) {
            MMMod.LOG.error("cannot parse ResourceLocation: expected number or string, but got {}", json);
            return new ResourceLocation("minecraft:air");
        }

        if (primitive.isNumber()) {
            int ordinal = primitive.getAsInt();

            CommonName name = DataUtils.getIndexSafe(CommonName.values(), ordinal);

            if (name == null) {
                MMMod.LOG.error("cannot parse ResourceLocation: illegal common name index: {}", ordinal);
                return new ResourceLocation("minecraft:air");
            }

            return new ResourceLocation(name.mod.ID + ":" + name.name);
        } else if (primitive.isString()) {
            String id = primitive.getAsString();

            return new ResourceLocation(id.contains(":") ? id : "minecraft:" + id);
        } else {
            MMMod.LOG.error("cannot parse ResourceLocation: expected number or string, but got {}", json);
            return new ResourceLocation("minecraft:air");
        }
    }

    private enum CommonName {

        AIR(Mods.Minecraft, "air"),
        GT_BLOCKMACHINES(Mods.GregTech, "gt.blockmachines"),
        AE_ITEMPART(Mods.AppliedEnergistics2, "item.ItemMultiPart"),
        ;

        public final Mods mod;
        public final String name;

        CommonName(Mods mod, String name) {
            this.mod = mod;
            this.name = name;
        }
    }
}
