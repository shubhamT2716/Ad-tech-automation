package net.media.emma.config;

import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

@org.springframework.context.annotation.Configuration
public class ModelMapperConfig {
    
    @Bean
    public Configuration jsonModelMapper() {
        return Configuration.builder()
        .jsonProvider(new JacksonJsonNodeJsonProvider())
        .mappingProvider(new JacksonMappingProvider())
        .build();
    }

    @Bean
    public ObjectWriter jsonWriter(){
        return new ObjectMapper().writer().withDefaultPrettyPrinter();
    }
}