package co.com.bancolombia.api.util;

import co.com.bancolombia.api.dto.ErrorResponse;

public final class Utils {
    public static String toJson(ErrorResponse errorResponse) {
        return "{" +
                "\"timestamp\":\"" + errorResponse.timestamp() + "\"," +
                "\"status\":" + errorResponse.status() + "," +
                "\"error\":\"" + errorResponse.error() + "\"," +
                "\"message\":\"" + errorResponse.message() + "\"," +
                "\"path\":\"" + errorResponse.path() + "\"," +
                "\"details\":\"" + errorResponse.details() + "\"" +
                "}";
    }

    private Utils() {
    }
}
