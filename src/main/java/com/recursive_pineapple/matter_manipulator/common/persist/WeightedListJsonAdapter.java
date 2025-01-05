package com.recursive_pineapple.matter_manipulator.common.persist;

import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.recursive_pineapple.matter_manipulator.common.building.BlockSpec;
import com.recursive_pineapple.matter_manipulator.common.data.WeightedSpecList;

import it.unimi.dsi.fastutil.objects.ObjectIntMutablePair;

public class WeightedListJsonAdapter implements JsonSerializer<WeightedSpecList>, JsonDeserializer<WeightedSpecList> {

    private static final Type BLOCK_SPEC = new TypeToken<BlockSpec>() {}.getType();

    @Override
    public WeightedSpecList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (!(json instanceof JsonArray array)) return new WeightedSpecList();

        WeightedSpecList list = new WeightedSpecList();

        for (var x : array) {
            if (!(x instanceof JsonObject obj)) continue;
            if (!(obj.get("s") instanceof JsonObject spec)) continue;
            if (!(obj.get("w") instanceof JsonPrimitive weight) || !weight.isNumber()) continue;

            list.specs.add(ObjectIntMutablePair.of(context.deserialize(spec, BLOCK_SPEC), weight.getAsInt()));
        }

        return list;
    }

    @Override
    public JsonElement serialize(WeightedSpecList src, Type typeOfSrc, JsonSerializationContext context) {
        if (src.specs.size() == 1) {
            BlockSpec spec = src.specs.get(0)
                .left();

            if (spec == null || spec.isAir()) return JsonNull.INSTANCE;
        }

        JsonArray array = new JsonArray();

        for (var p : src.specs) {
            JsonObject pair = new JsonObject();

            pair.add("s", context.serialize(p.left()));
            pair.addProperty("w", p.rightInt());

            array.add(pair);
        }

        return array;
    }
}
