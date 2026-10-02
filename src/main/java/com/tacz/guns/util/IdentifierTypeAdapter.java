package com.tacz.guns.util;

import com.google.gson.*;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;

/**
 * Identifier.Serializer is gone, same behaviour for the gun pack Gson instances.
 */
public class IdentifierTypeAdapter implements JsonDeserializer<Identifier>, JsonSerializer<Identifier> {
    @Override
    public Identifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Identifier.parse(GsonHelperCompat.asString(json, "location"));
    }

    @Override
    public JsonElement serialize(Identifier src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    private static final class GsonHelperCompat {
        static String asString(JsonElement json, String name) {
            if (json.isJsonPrimitive()) {
                return json.getAsString();
            }
            throw new JsonSyntaxException("Expected " + name + " to be a string, was " + json);
        }
    }
}
