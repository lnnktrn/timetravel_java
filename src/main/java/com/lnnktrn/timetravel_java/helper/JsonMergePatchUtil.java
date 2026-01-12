package com.lnnktrn.timetravel_java.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class JsonMergePatchUtil {

    private final ObjectMapper objectMapper;

    public JsonMergePatchUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Applies "merge patch" semantics:
     * - null => remove field
     * - object => deep merge
     * - scalar/array => replace
     */
    public JsonNode applyMergePatch(JsonNode target, JsonNode patch) {
        if (patch == null || patch.isNull()) {
            return patch;
        }

        if (!patch.isObject()) {
            return patch;
        }

        ObjectNode targetObj = (target != null && target.isObject())
                ? (ObjectNode) target.deepCopy()
                : objectMapper.createObjectNode();

        Iterator<Map.Entry<String, JsonNode>> fields = patch.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode patchValue = entry.getValue();

            if (patchValue == null || patchValue.isNull()) {
                targetObj.remove(fieldName);
                continue;
            }

            JsonNode targetValue = targetObj.get(fieldName);

            if (patchValue.isObject()) {
                JsonNode mergedChild = applyMergePatch(targetValue, patchValue);
                targetObj.set(fieldName, mergedChild);
            } else {
                // scalar or array: replace
                targetObj.set(fieldName, patchValue);
            }
        }

        return targetObj;
    }
}
