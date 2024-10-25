package net.media.emma.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo.DomInjectParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
@Slf4j
public class DomJsonUtils {

    @Autowired
    Configuration jsonModelMapper;

    public String replaceInJson(String jsonBody, String jsonPath, String overrideValue) {

        ObjectMapper mapper = new ObjectMapper();
        String newResponseBody = null;

        try {
            JsonNode jsonNode = JsonPath.using(jsonModelMapper).parse(jsonBody)
                    .set(jsonPath, overrideValue).json();
            newResponseBody = mapper.writeValueAsString(jsonNode);
            return newResponseBody;

        } catch (PathNotFoundException | JsonProcessingException e) {

            log.error("Respoinse Body Mocking Error", e);
        }

        return jsonBody;
    }

    public String domManipulate(String pageContent, DomInjectParams domInject) {

        try {
            Document document = Jsoup.parse(pageContent);
            String fileContent = new String(Files.readAllBytes(Paths.get(domInject.getContent())));
            Elements domElement = document.select(domInject.getDomSelector());

            if (domInject.getAppend()) {
                domElement.append(fileContent);
            } else {
                domElement.html(fileContent);
            }

            return document.html();

        } catch (IOException e) {
            log.error("File Read Error... skipping domInject loop", e);
            return pageContent;
        }
    }
}
