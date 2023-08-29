package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class JsonPath {

    @Autowired
    Configuration jsonModelMapper;

    @Autowired
    ObjectMapper objectMapper;

    public JsonNode getJsonNodes(String json) {
        try {
            return objectMapper.readTree(json); // Parse JSON using readTree
        } catch (IOException e) {
            return null;
        }
    }


    public Boolean verifyJsonNodeValue(JsonNode jsonNode, String value) {
        if (jsonNode.isTextual() && value.equals(jsonNode.textValue())) {
            return true;
        } else {
            return false;
        }
    }

    public String toJsonPointer(String dotNotation) {
        return "/" + dotNotation.replace(".", "/").replace("[", "/").replace("]", "");
    }

    public boolean verifyDataAssertion(String jsonPathString, String expectedValue, JsonNode parsedJson) {
        JsonNode node = parsedJson.at(JsonPointer.valueOf(toJsonPointer(jsonPathString)));
        return verifyJsonNodeValue(node, expectedValue);
    }
}
