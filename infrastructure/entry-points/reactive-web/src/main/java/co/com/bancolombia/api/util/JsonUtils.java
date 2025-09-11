package co.com.bancolombia.api.util;

import co.com.bancolombia.api.exception.JsonProcessingRuntimeException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {
        // evitar instanciación
    }

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new JsonProcessingRuntimeException("Error serializando a JSON", e);
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new JsonProcessingRuntimeException(
                    "Error deserializando JSON a " + clazz.getSimpleName(), e
            );
        }
    }
}