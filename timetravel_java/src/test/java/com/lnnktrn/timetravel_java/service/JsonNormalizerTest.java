package com.lnnktrn.timetravel_java.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonNormalizerTest {

    private JsonNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new JsonNormalizer(new ObjectMapper());
    }

    @Test
    void normalize_removesNullAndEmptyAndWhitespaceValues() {
        String input = """
                {
                  "name": "Kate",
                  "empty": "",
                  "spaces": "   ",
                  "nullValue": null,
                  "ok": "value"
                }
                """;

        String out = normalizer.normalizeObjectStringValues(input);

        // order should be preserved: name then ok
        assertEquals("{\"name\":\"Kate\",\"ok\":\"value\"}", out);
    }

    @Test
    void normalize_keepsNumbersAsInvalidBecauseContractIsStringOrNull() {
        String input = """
                { "a": 123 }
                """;

        assertThrows(IllegalArgumentException.class,
                () -> normalizer.normalizeObjectStringValues(input));
    }

    @Test
    void normalize_throwsOnNonObjectJson() {
        String inputArray = """
                [ {"a":"b"} ]
                """;

        assertThrows(IllegalArgumentException.class,
                () -> normalizer.normalizeObjectStringValues(inputArray));
    }

    @Test
    void normalize_throwsOnInvalidJson() {
        String bad = "{ not-json }";

        assertThrows(IllegalArgumentException.class,
                () -> normalizer.normalizeObjectStringValues(bad));
    }

    @Test
    void normalize_returnsEmptyObject_whenAllKeysRemoved() {
        String input = """
                { "a": "", "b": "   ", "c": null }
                """;

        String out = normalizer.normalizeObjectStringValues(input);

        assertEquals("{}", out);
    }

    @Test
    void normalize_preservesKeyOrder() {
        String input = """
                { "b": "2", "a": "", "c": "3" }
                """;

        String out = normalizer.normalizeObjectStringValues(input);

        // "a" removed, remaining order b then c
        assertEquals("{\"b\":\"2\",\"c\":\"3\"}", out);
    }

}