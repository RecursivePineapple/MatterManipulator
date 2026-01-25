package matter_manipulator.core.persist.adapters;

import java.lang.reflect.Type;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import matter_manipulator.core.persist.NBTPersist;

public class ItemStackJsonAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || !json.isJsonObject() || ((JsonObject) json).size() == 0) return ItemStack.EMPTY;

        return new ItemStack((NBTTagCompound) NBTPersist.toNbtExact(json));
    }

    @Override
    public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
        return NBTPersist.toJsonObjectExact(src.writeToNBT(new NBTTagCompound()));
    }
}
