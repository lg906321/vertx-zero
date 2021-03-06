package io.vertx.zero.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.up.func.Fn;
import io.vertx.up.log.Annal;
import io.vertx.zero.eon.Values;
import io.vertx.zero.tool.mirror.Types;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Lookup the json tree data
 */
@SuppressWarnings({"unchecked"})
public final class Jackson {

    private static final Annal LOGGER = Annal.get(Jackson.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonObject visitJObject(
            final JsonObject item,
            final String... keys
    ) {

        Ensurer.gtLength(Jackson.class, 0, (Object[]) keys);
        return searchData(item, JsonObject.class, keys);
    }

    public static JsonArray visitJArray(
            final JsonObject item,
            final String... keys
    ) {
        Ensurer.gtLength(Jackson.class, 0, (Object[]) keys);
        return searchData(item, JsonArray.class, keys);
    }

    public static Integer visitInt(
            final JsonObject item,
            final String... keys
    ) {
        Ensurer.gtLength(Jackson.class, 0, (Object[]) keys);
        return searchData(item, Integer.class, keys);
    }

    public static String visitString(
            final JsonObject item,
            final String... keys
    ) {
        Ensurer.gtLength(Jackson.class, 0, (Object[]) keys);
        return searchData(item, String.class, keys);
    }

    private static <T> T searchData(final JsonObject data,
                                    final Class<T> clazz,
                                    final String... pathes) {
        if (null == data || Values.ZERO == pathes.length) {
            return null;
        }/** 1. Get current node  **/
        final JsonObject current = data;
        /** 2. Extract current input key **/
        final String path = pathes[Values.IDX];
        /** 3. Continue searching if key existing, otherwise terminal. **/
        return Fn.getSemi(current.containsKey(path) && null != current.getValue(path),
                LOGGER,
                () -> {
                    final Object curVal = current.getValue(path);
                    T result = null;
                    if (Values.ONE == pathes.length) {
                        /** 3.1. Get the end node. **/
                        if (clazz == curVal.getClass()) {
                            result = (T) curVal;
                        }
                    } else {
                        /** 3.2. Address the middle search **/
                        if (Types.isJObject(curVal)) {
                            final JsonObject continueNode = current.getJsonObject(path);
                            /** 4.Extract new key **/
                            final String[] continueKeys =
                                    Arrays.copyOfRange(pathes,
                                            Values.ONE,
                                            pathes.length);
                            result = searchData(continueNode,
                                    clazz,
                                    continueKeys);
                        }
                    }
                    return result;
                },
                () -> null);
    }

    public static JsonObject validJObject(final Supplier<JsonObject> supplier) {
        JsonObject result;
        try {
            result = supplier.get();
        } catch (final DecodeException ex) {
            result = new JsonObject();
        }
        return result;
    }

    public static JsonArray validJArray(final Supplier<JsonArray> supplier) {
        JsonArray result;
        try {
            result = supplier.get();
        } catch (final DecodeException ex) {
            result = new JsonArray();
        }
        return result;
    }

    public static JsonArray toJArray(final Object value) {
        final JsonArray result = new JsonArray();
        Fn.safeNull(() -> {
            if (Types.isJArray(value)) {
                result.addAll((JsonArray) value);
            } else {
                result.add(value.toString());
            }
        }, value);
        return result;
    }

    public static <T> String serialize(final T t) {
        return Fn.get(null, () ->
                Fn.getJvm(() -> MAPPER.writeValueAsString(t), t), t);
    }

    public static <T> T deserialize(final String value, final Class<T> type) {
        return Fn.get(null,
                () -> Fn.getJvm(() -> MAPPER.readValue(value, type)), value);
    }

    private Jackson() {
    }
}
