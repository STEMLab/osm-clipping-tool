package io.github.stemlab.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Created by Azamat on 6/2/2017.
 */
public class JsonUtils {
    public static final Gson json = new Gson();

    public static String toJson(Object object) {
        return json.toJson(object);
    }

    public static <T> T fromJson(String gson, Class<T> classOfT) {
        return json.fromJson(gson, classOfT);
    }

    public static <T> T fromJson(String gson, Type type) {
        return json.fromJson(gson, type);
    }
}
