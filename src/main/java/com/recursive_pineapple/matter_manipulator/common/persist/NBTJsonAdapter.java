package com.recursive_pineapple.matter_manipulator.common.persist;

import java.lang.reflect.Type;

import net.minecraft.nbt.NBTTagCompound;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.recursive_pineapple.matter_manipulator.common.utils.MMUtils;

public class NBTJsonAdapter implements JsonSerializer<NBTTagCompound>, JsonDeserializer<NBTTagCompound> {

    @Override
    public NBTTagCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        if (!(json instanceof JsonObject)) throw new JsonParseException("expected object");

        return (NBTTagCompound) MMUtils.toNbtExact(json);
    }

    @Override
    public JsonElement serialize(NBTTagCompound src, Type typeOfSrc, JsonSerializationContext context) {
        return MMUtils.toJsonObjectExact(src);
    }
}
