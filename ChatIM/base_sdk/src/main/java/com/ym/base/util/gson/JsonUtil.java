package com.ym.base.util.gson;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.Reader;
import java.lang.reflect.Type;

public class JsonUtil {
    private static final String ARRAY_FLAG = "[";
    private static final String OBJECT_FLAG = "{";

    public static JsonObject fromJson(String str) {
        try {
            return new JsonParser().parse(str).getAsJsonObject();
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(JsonElement jsonElement, Class<T> cls) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(jsonElement, (Class) cls) : (T) NBSGsonInstrumentation.fromJson(gson, jsonElement, (Class) cls);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(JsonElement jsonElement, Type type) {
        if (jsonElement == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(jsonElement, type) : (T) NBSGsonInstrumentation.fromJson(gson, jsonElement, type);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(JsonReader jsonReader, Class<T> cls) {
        if (jsonReader == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(jsonReader, cls) : (T) NBSGsonInstrumentation.fromJson(gson, jsonReader, cls);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(JsonReader jsonReader, Type type) {
        if (jsonReader == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(jsonReader, type) : (T) NBSGsonInstrumentation.fromJson(gson, jsonReader, type);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(Reader reader, Class<T> cls) {
        if (reader == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(reader, (Class) cls) : (T) NBSGsonInstrumentation.fromJson(gson, reader, (Class) cls);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(Reader reader, Type type) {
        if (reader == null) {
            return null;
        }
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(reader, type) : (T) NBSGsonInstrumentation.fromJson(gson, reader, type);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static <T> T fromJson(String str, Class<T> cls) {
        if (!(str == null || !str.startsWith("{") || cls == null)) {
            try {
                Gson gson = Json.GSON;
                return !(gson instanceof Gson) ? (T) gson.fromJson(str, (Class) cls) : (T) NBSGsonInstrumentation.fromJson(gson, str, (Class) cls);
            } catch (Exception e) {
                LogUtils.e(e);
            }
        }
        return null;
    }

    public static <T> T fromJson(String str, Type type) {
        try {
            Gson gson = Json.GSON;
            return !(gson instanceof Gson) ? (T) gson.fromJson(str, type) : (T) NBSGsonInstrumentation.fromJson(gson, str, type);
        } catch (Exception e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static Gson getGson() {
        return Json.GSON;
    }

    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        Gson gson = Json.GSON;
        return !(gson instanceof Gson) ? gson.toJson(obj) : NBSGsonInstrumentation.toJson(gson, obj);
    }

    public static JsonObject toJsonTree(Object obj) {
        return Json.GSON.toJsonTree(obj).getAsJsonObject();
    }

    public String toJson(JsonElement jsonElement) {
        Gson gson = Json.O_TO_JSON;
        return !(gson instanceof Gson) ? gson.toJson(jsonElement) : NBSGsonInstrumentation.toJson(gson, jsonElement);
    }

    public String toJson(Object obj, Type type) {
        Gson gson = Json.O_TO_JSON;
        return !(gson instanceof Gson) ? gson.toJson(obj, type) : NBSGsonInstrumentation.toJson(gson, obj, type);
    }

    /* access modifiers changed from: package-private */
    public static class Json {
        static final Gson GSON = new Gson();
        static final Gson O_TO_JSON = new GsonBuilder().disableHtmlEscaping().create();

        Json() {
        }
    }
}
