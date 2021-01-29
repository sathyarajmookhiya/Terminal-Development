package uk.co.transaxiom.android.txandroidlib;


import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import android.util.Base64;
import android.util.Log;


public class GsonHelper {
    public static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
            new ByteArrayToBase64TypeAdapter()).create();
 
    // Using Android's base64 libraries. This can be replaced with any base64 library.
    private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decode(json.getAsString(), Base64.DEFAULT);
        }
 
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        
        	String tmp = Base64.encodeToString(src, Base64.CRLF);

        	JsonPrimitive json = new JsonPrimitive(tmp.trim());
        	Log.d("GSONHELPER", "JsonPrimitive to string : "+json.getAsString());
            return json;
        }
    }
}
