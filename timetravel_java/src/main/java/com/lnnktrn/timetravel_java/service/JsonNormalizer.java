package com.lnnktrn.timetravel_java.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonNormalizer {
    private final ObjectMapper mapper;

    public JsonNormalizer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Accepts JSON object where values are string or null.
     * Removes keys with null / empty / whitespace-only values.
     * Returns normalized JSON string (object).
     */
    public String normalizeObjectStringValues(String json) {
        if (json == null) {
            throw new IllegalArgumentException("data cannot be null");
        }

        final Map<String, String> map;
        try {
            map = mapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {   // Not a JSON object or values are not strings/null
            throw new IllegalArgumentException("data must be a JSON object with string values (or null)", e);
        }

        Map<String, String> cleaned = new LinkedHashMap<>();
        for (var entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // null -> delete
            if (value == null) continue;
// "" or "   " -> delete
            if (value.trim().isEmpty()) continue;

            cleaned.put(key, value);
        }

        try {
            return mapper.writeValueAsString(cleaned);
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to serialize normalized JSON", e);
        }
    }
}
