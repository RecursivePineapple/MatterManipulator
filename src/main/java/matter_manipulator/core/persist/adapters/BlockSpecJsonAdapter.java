package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.MMMod;
import matter_manipulator.common.interop.MMRegistriesInternal;
import matter_manipulator.core.block_spec.IBlockSpec;
import matter_manipulator.core.block_spec.IBlockSpecLoader;

public class BlockSpecJsonAdapter implements JsonSerializer<IBlockSpec>, JsonDeserializer<IBlockSpec> {

    @Override
    public IBlockSpec deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (!(json instanceof JsonObject obj)) {
            MMMod.LOG.error("Expected JsonObject, got {}", json, new Exception());
            return null;
        }

        for (var e : obj.entrySet()) {
            IBlockSpecLoader loader = MMRegistriesInternal.LOADERS.get(e.getKey());

            if (loader == null) {
                MMMod.LOG.error(
                    "Could not load spec {}: spec loader {} was not found",
                    e.getValue(),
                    e.getKey(),
                    new Exception());
                break;
            }

            return loader.load(e.getValue());
        }

        return null;
    }

    @Override
    public JsonElement serialize(IBlockSpec src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();

        obj.add(
            src.getLoader()
                .getKey(),
            src.getLoader()
                .save(src));

        return obj;
    }
}
