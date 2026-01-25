package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.core.persist.DataStorage;

public class DataStorageJsonAdapter implements JsonSerializer<DataStorage>, JsonDeserializer<DataStorage> {

    @Override
    public DataStorage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new DataStorage(context.deserialize(json, JsonObject.class), true);
    }

    @Override
    public JsonElement serialize(DataStorage src, Type typeOfSrc, JsonSerializationContext context) {
        return src.state;
    }
}
