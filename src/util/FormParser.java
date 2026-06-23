package util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class FormParser {

    private FormParser() {
    }

    public static Map<String, String> parse(
            String body) {

        Map<String, String> values =
                new HashMap<>();

        if (body == null || body.isBlank()) {
            return values;
        }

        String[] pairs = body.split("&");

        for (String pair : pairs) {

            String[] keyValue =
                    pair.split("=", 2);

            String key = URLDecoder.decode(
                    keyValue[0],
                    StandardCharsets.UTF_8
            );

            String value = keyValue.length > 1
                    ? URLDecoder.decode(
                    keyValue[1],
                    StandardCharsets.UTF_8
            )
                    : "";

            values.put(key, value);
        }

        return values;
    }
}