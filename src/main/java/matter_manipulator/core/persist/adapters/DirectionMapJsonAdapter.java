package matter_manipulator.core.persist.adapters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.common.utils.DataUtils;
import matter_manipulator.core.util.DirectionMap;

public class DirectionMapJsonAdapter implements JsonSerializer<DirectionMap<?>>, JsonDeserializer<DirectionMap<?>> {

    @Override
    public DirectionMap<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (!(json instanceof JsonObject obj)) throw new JsonParseException("Expected object: " + json);

        @SuppressWarnings("rawtypes")
        DirectionMap map = new DirectionMap();

        Type t;

        if (typeOfT instanceof ParameterizedType param) {
            t = DataUtils.getIndexSafe(param.getActualTypeArguments(), 0);
        } else {
            t = null;
        }

        obj.entrySet().forEach(e -> {
            try {
                int i = Integer.parseInt(e.getKey());

                Object value = t != null ? context.deserialize(e.getValue(), t) : e.getValue();

                //noinspection unchecked
                map.put(DirectionMap.key(i), value);
            } catch (NumberFormatException ex) {
                throw new JsonParseException("Expected integer key: " + e.getKey(), ex);
            }
        });

        return map;
    }

    @Override
    public JsonElement serialize(DirectionMap<?> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        Type t;

        if (typeOfSrc instanceof ParameterizedType param) {
            t = DataUtils.getIndexSafe(param.getActualTypeArguments(), 0);
        } else {
            t = null;
        }

        src.forEach((key, value) -> {
            obj.add(Integer.toString(DirectionMap.index(key)), t == null ? context.serialize(value) : context.serialize(value, t));
        });

        return obj;
    }
}
