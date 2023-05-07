package net.media.emma.config;

import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Loggers {

    @Bean
    public Logger standardLogger() {
        return org.slf4j.LoggerFactory.getLogger("standardLogger");
    }
    
}
