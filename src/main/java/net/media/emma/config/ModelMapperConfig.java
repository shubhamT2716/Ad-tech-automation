package net.media.emma.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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
    public ObjectWriter jsonWriter() {
        return new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Bean
    public ObjectMapper jsonReader() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public ObjectMapper genericObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ObjectMapper yamlMapper() {
        return new YAMLMapper();
    }

}