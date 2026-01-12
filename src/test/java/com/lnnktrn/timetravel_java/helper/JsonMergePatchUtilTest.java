package com.lnnktrn.timetravel_java.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonMergePatchUtilTest {

    private ObjectMapper objectMapper;
    private JsonMergePatchUtil util;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        util = new JsonMergePatchUtil(objectMapper);
    }

    @Test
    void applyMergePatch_patchIsNull_returnsNull() {
        ObjectNode target = objectMapper.createObjectNode().put("a", 1);

        JsonNode result = util.applyMergePatch(target, null);

        assertNull(result);
    }

    @Test
    void applyMergePatch_patchIsJsonNull_returnsJsonNull() {
        ObjectNode target = objectMapper.createObjectNode().put("a", 1);

        JsonNode result = util.applyMergePatch(target, objectMapper.nullNode());

        assertTrue(result.isNull());
    }

    @Test
    void applyMergePatch_patchIsScalar_replacesTarget() {
        ObjectNode target = objectMapper.createObjectNode().put("a", 1);
        JsonNode patch = objectMapper.createObjectNode().textNode("hello");

        JsonNode result = util.applyMergePatch(target, patch);

        assertTrue(result.isTextual());
        assertEquals("hello", result.asText());
    }

    @Test
    void applyMergePatch_patchIsArray_replacesTarget() {
        ObjectNode target = objectMapper.createObjectNode().put("a", 1);
        ArrayNode patch = objectMapper.createArrayNode().add(1).add(2);

        JsonNode result = util.applyMergePatch(target, patch);

        assertTrue(result.isArray());
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).asInt());
        assertEquals(2, result.get(1).asInt());
    }

    @Test
    void applyMergePatch_targetNull_patchObject_createsNewObjectAndApplies() {
        ObjectNode patch = objectMapper.createObjectNode()
                .put("a", 1)
                .put("b", "x");

        JsonNode result = util.applyMergePatch(null, patch);

        assertTrue(result.isObject());
        assertEquals(1, result.get("a").asInt());
        assertEquals("x", result.get("b").asText());
    }

    @Test
    void applyMergePatch_targetNotObject_patchObject_createsNewObjectAndApplies() {
        JsonNode target = objectMapper.createObjectNode().textNode("not-an-object");
        ObjectNode patch = objectMapper.createObjectNode().put("a", 1);

        JsonNode result = util.applyMergePatch(target, patch);

        assertTrue(result.isObject());
        assertEquals(1, result.get("a").asInt());
    }

    @Test
    void applyMergePatch_nullValueRemovesField() {
        ObjectNode target = objectMapper.createObjectNode()
                .put("a", 1)
                .put("b", 2);

        ObjectNode patch = objectMapper.createObjectNode()
                .set("b", objectMapper.nullNode());

        JsonNode result = util.applyMergePatch(target, patch);

        assertTrue(result.isObject());
        assertTrue(result.has("a"));
        assertFalse(result.has("b"));
        assertEquals(1, result.get("a").asInt());
    }

    @Test
    void applyMergePatch_deepMergeNestedObjects() {
        ObjectNode target = objectMapper.createObjectNode();
        target.set("obj", objectMapper.createObjectNode()
                .put("x", 1)
                .put("y", 2));

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("obj", objectMapper.createObjectNode()
                .put("y", 99)
                .put("z", 3));

        JsonNode result = util.applyMergePatch(target, patch);

        assertTrue(result.isObject());
        JsonNode obj = result.get("obj");
        assertTrue(obj.isObject());
        assertEquals(1, obj.get("x").asInt());   // from target
        assertEquals(99, obj.get("y").asInt());  // overwritten
        assertEquals(3, obj.get("z").asInt());   // added
    }

    @Test
    void applyMergePatch_nestedNullRemovesNestedField() {
        ObjectNode target = objectMapper.createObjectNode();
        target.set("obj", objectMapper.createObjectNode()
                .put("x", 1)
                .put("y", 2));

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("obj", objectMapper.createObjectNode()
                .set("y", objectMapper.nullNode()));

        JsonNode result = util.applyMergePatch(target, patch);

        JsonNode obj = result.get("obj");
        assertTrue(obj.isObject());
        assertTrue(obj.has("x"));
        assertFalse(obj.has("y"));
        assertEquals(1, obj.get("x").asInt());
    }

    @Test
    void applyMergePatch_arrayReplacesField() {
        ObjectNode target = objectMapper.createObjectNode();
        target.set("arr", objectMapper.createArrayNode().add(1).add(2));

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("arr", objectMapper.createArrayNode().add(9));

        JsonNode result = util.applyMergePatch(target, patch);

        JsonNode arr = result.get("arr");
        assertTrue(arr.isArray());
        assertEquals(1, arr.size());
        assertEquals(9, arr.get(0).asInt());
    }

    @Test
    void applyMergePatch_doesNotMutateOriginalTarget() {
        ObjectNode target = objectMapper.createObjectNode();
        target.set("obj", objectMapper.createObjectNode().put("x", 1));

        ObjectNode patch = objectMapper.createObjectNode();
        patch.set("obj", objectMapper.createObjectNode().put("x", 2));

        JsonNode result = util.applyMergePatch(target, patch);

        // result has updated value
        assertEquals(2, result.get("obj").get("x").asInt());
        // original target unchanged (because deepCopy)
        assertEquals(1, target.get("obj").get("x").asInt());
    }
}
