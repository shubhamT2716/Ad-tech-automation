package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;


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

    public ReadContext parseJson(String json) {
        return com.jayway.jsonpath.JsonPath.parse(json);
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

    public Object readValueFromJson(String data, String jsonPath) throws PathNotFoundException {
    	
        return com.jayway.jsonpath.JsonPath.read(data, jsonPath);
    }

    public String convertObjectToJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    public boolean compareValueInJson(String json, String jsonPath, Object expectedValue) {
        try {
        	

            	Object actualValue = readValueFromJson(json, jsonPath);
                
//              log.info("Path = "+jsonPath+"   " +" expectedValue = "+  expectedValue +"   " +"Actual Value = "+actualValue);
             
              String pathValue="Path = "+jsonPath+"   " +" expectedValue = "+  expectedValue +"   " +"Actual Value = "+actualValue;
              
              log.info(pathValue);
              
              ArrayList<String> al=new ArrayList<>();
              al.add(pathValue);
              
              
              if(expectedValue.toString().equals(actualValue.toString()))
              {
            	  return true;
              }
              else {
            	  
            	  return false;
              }
              
//              return Objects.equals(actualValue.toString(), expectedValue.toString());
//            }else {
//            	throw new PathNotFoundException();
//            }
            
//         	Object actualValue = readValueFromJson(json, jsonPath);
        		
// //            log.info("Path = "+jsonPath+"   " +" expectedValue = "+  expectedValue +"   " +"Actual Value = "+actualValue);
           
//             String pathValue="Path = "+jsonPath+"   " +" expectedValue = "+  expectedValue +"   " +"Actual Value = "+actualValue;
            
//             ArrayList<String> al=new ArrayList<>();
//             al.add(pathValue);
            
//            log.info(pathValue);
            
            
            
        } catch (PathNotFoundException | IllegalArgumentException | NullPointerException e) {
            return false;
        }
        
    }




}
