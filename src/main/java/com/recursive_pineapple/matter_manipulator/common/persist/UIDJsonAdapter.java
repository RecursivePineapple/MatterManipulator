package com.recursive_pineapple.matter_manipulator.common.persist;

import java.lang.reflect.Type;

import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.recursive_pineapple.matter_manipulator.MMMod;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;
import com.recursive_pineapple.matter_manipulator.common.utils.Mods;

public class UIDJsonAdapter implements JsonSerializer<UniqueIdentifier>, JsonDeserializer<UniqueIdentifier> {

    @Override
    public JsonElement serialize(UniqueIdentifier src, Type typeOfSrc, JsonSerializationContext context) {
        CommonName common = null;

        for (CommonName name : CommonName.values()) {
            if (name.mod.ID.equals(src.modId) && name.name.equals(src.name)) {
                common = name;
                break;
            }
        }

        if (common != null) {
            return new JsonPrimitive(common.ordinal());
        } else {
            if ("minecraft".equals(src.modId)) {
                return new JsonPrimitive(src.name);
            } else {
                return new JsonPrimitive(src.toString());
            }
        }
    }

    @Override
    public UniqueIdentifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive primitive)) {
            MMMod.LOG.error("cannot parse UniqueIdentifier: expected number or string, but got " + json);
            return new UniqueIdentifier("minecraft:air");
        }

        if (primitive.isNumber()) {
            int ordinal = primitive.getAsInt();

            CommonName name = MMUtils.getIndexSafe(CommonName.values(), ordinal);

            if (name == null) {
                MMMod.LOG.error("cannot parse UniqueIdentifier: illegal common name index: " + ordinal);
                return new UniqueIdentifier("minecraft:air");
            }

            return new UniqueIdentifier(name.mod.ID + ":" + name.name);
        } else if (primitive.isString()) {
            String id = primitive.getAsString();

            return new UniqueIdentifier(id.contains(":") ? id : "minecraft:" + id);
        } else {
            MMMod.LOG.error("cannot parse UniqueIdentifier: expected number or string, but got " + json);
            return new UniqueIdentifier("minecraft:air");
        }
    }

    public static enum CommonName {

        AIR(Mods.Minecraft, "air"),
        GT_BLOCKMACHINES(Mods.GregTech, "gt.blockmachines"),
        AE_ITEMPART(Mods.AppliedEnergistics2, "item.ItemMultiPart"),
        ARCH_SHAPE(Mods.ArchitectureCraft, "shape"),
        ARCH_SHAPE_GLOW(Mods.ArchitectureCraft, "shapeSE"),
        ;

        public final Mods mod;
        public final String name;

        private CommonName(Mods mod, String name) {
            this.mod = mod;
            this.name = name;
        }
    }
}
