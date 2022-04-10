package com.snoopy.grpc.base.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Gson工具
 *
 * @author :   kehanjiang
 * @date :   2021/11/8  10:51
 */
public class GsonUtil {


    private static final Gson gson = new GsonBuilder().create();


    public static Gson getNewGson(boolean pretty, boolean serializeNulls) {
        GsonBuilder temp = new GsonBuilder();
        if (pretty) {
            temp.setPrettyPrinting();
        }
        if (serializeNulls) {
            temp.serializeNulls();
        }
        return temp.create();
    }

    public static Gson getGson() {
        return gson;
    }

    public static <T> T getJSONValue(JsonObject data, String name, Class<T> clazz, T defaultValue) {
        JsonElement element = data.get(name);
        if (element == null) {
            return defaultValue;
        } else {
            return gson.fromJson(element, clazz);
        }
    }
}
