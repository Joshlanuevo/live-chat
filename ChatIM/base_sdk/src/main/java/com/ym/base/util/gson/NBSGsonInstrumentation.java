package com.ym.base.util.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.Reader;
import java.lang.reflect.Type;

public class NBSGsonInstrumentation {

    public static <T> T fromJson(Gson gson, JsonElement jsonElement, Class<T> cls) throws JsonSyntaxException {
        T t = (T) gson.fromJson(jsonElement, (Class) cls);
        return t;
    }

    public static <T> T fromJson(Gson gson, JsonElement jsonElement, Type type) throws JsonSyntaxException {
        T t = gson.fromJson(jsonElement, type);
        return t;
    }

    public static <T> T fromJson(Gson gson, JsonReader jsonReader, Type type) throws JsonIOException, JsonSyntaxException {
        T t = gson.fromJson(jsonReader, type);
        return t;
    }

    public static <T> T fromJson(Gson gson, Reader reader, Class<T> cls) throws JsonSyntaxException, JsonIOException {
        T t = (T) gson.fromJson(reader, (Class) cls);
        return t;
    }

    public static <T> T fromJson(Gson gson, Reader reader, Type type) throws JsonIOException, JsonSyntaxException {
        T t = gson.fromJson(reader, type);
        return t;
    }

    public static <T> T fromJson(Gson gson, String str, Class<T> cls) throws JsonSyntaxException {
        T t = (T) gson.fromJson(str, (Class) cls);
        return t;
    }

    public static <T> T fromJson(Gson gson, String str, Type type) throws JsonSyntaxException {
        T t = gson.fromJson(str, type);
        return t;
    }

    public static String toJson(Gson gson, JsonElement jsonElement) {
        String json = gson.toJson(jsonElement);
        return json;
    }

    public static String toJson(Gson gson, Object obj) {
        String json = gson.toJson(obj);
        return json;
    }

    public static String toJson(Gson gson, Object obj, Type type) {
        String json = gson.toJson(obj, type);
        return json;
    }

    public static void toJson(Gson gson, JsonElement jsonElement, JsonWriter jsonWriter) throws JsonIOException {
        gson.toJson(jsonElement, jsonWriter);
    }

    public static void toJson(Gson gson, JsonElement jsonElement, Appendable appendable) throws JsonIOException {
        gson.toJson(jsonElement, appendable);
    }

    public static void toJson(Gson gson, Object obj, Appendable appendable) throws JsonIOException {
        gson.toJson(obj, appendable);
    }

    public static void toJson(Gson gson, Object obj, Type type, JsonWriter jsonWriter) throws JsonIOException {
        gson.toJson(obj, type, jsonWriter);
    }

    public static void toJson(Gson gson, Object obj, Type type, Appendable appendable) throws JsonIOException {
        gson.toJson(obj, type, appendable);
    }

}
