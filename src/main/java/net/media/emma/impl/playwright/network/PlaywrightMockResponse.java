package net.media.emma.impl.playwright.network;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;

import java.nio.charset.StandardCharsets;

public class PlaywrightMockResponse {

    private static final Configuration configuration = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();
    static String responseBody;
    Page page;

    public PlaywrightMockResponse(Page page) {
        this.page = page;
    }

    public void mockResponse(String urlPattern, String jsonPath, String newValue) {
        System.out.println("Mocking ---- ");
        page.route(urlPattern, route -> {
            APIResponse apiResponse = route.fetch();
            responseBody = new String(apiResponse.body(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = JsonPath.using(configuration).parse(responseBody).set(jsonPath, newValue).json();
            try {
                responseBody = mapper.writeValueAsString(jsonNode);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            route.fulfill(new Route.FulfillOptions().setBody(responseBody));
        });
    }
}
