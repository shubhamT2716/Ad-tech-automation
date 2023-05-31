package net.media.emma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.media.emma.utils.DomJsonUtils;

@Configuration
public class UtilsConfig {
    
    @Bean
    public DomJsonUtils domJsonUtils() {
        return new DomJsonUtils();
    }
}
