package matter_manipulator.common.persist;

import java.lang.reflect.Type;
import java.util.Base64;
import java.util.BitSet;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BitSetJsonAdapter implements JsonSerializer<BitSet>, JsonDeserializer<BitSet> {

    @Override
    public BitSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String str = context.deserialize(json, String.class);

        byte[] data = Base64.getDecoder().decode(str);

        return BitSet.valueOf(data);
    }

    @Override
    public JsonElement serialize(BitSet src, Type typeOfSrc, JsonSerializationContext context) {
        byte[] data = src.toByteArray();

        String str = Base64.getEncoder().encodeToString(data);

        return context.serialize(str);
    }
}
