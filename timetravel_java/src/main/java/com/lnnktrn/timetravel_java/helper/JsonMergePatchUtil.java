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
            // по merge-patch это значит "заменить целиком на null",
            // но для твоего кейса обычно patch root null не используют.
            return patch;
        }

        // если patch не объект - заменить target целиком
        if (!patch.isObject()) {
            return patch;
        }

        // target должен быть объектом, иначе делаем новый объект
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
                // scalar или array: replace
                targetObj.set(fieldName, patchValue);
            }
        }

        return targetObj;
    }
}
