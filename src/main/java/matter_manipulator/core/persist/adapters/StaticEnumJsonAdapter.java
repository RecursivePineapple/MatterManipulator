package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class StaticEnumJsonAdapter<T extends Enum<T>> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final Class<T> clazz;
    private final T[] values;

    public StaticEnumJsonAdapter(Class<T> clazz) {
        this.clazz = clazz;
        values = clazz.getEnumConstants();
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (!(json instanceof JsonPrimitive primitive)) throw new JsonParseException("expected number: " + json);

        if (primitive.isNumber()) {
            int index = primitive.getAsInt();

            if (index < 0 || index >= values.length) throw new JsonParseException("illegal enum index: " + index);

            return values[index];
        } else {
            try {
                return Enum.valueOf(clazz, primitive.getAsString());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("illegal enum variant: '" + primitive.getAsString() + "'", e);
            }
        }
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.ordinal());
    }
}
