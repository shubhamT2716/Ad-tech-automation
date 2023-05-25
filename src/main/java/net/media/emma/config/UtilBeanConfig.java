package net.media.emma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.media.emma.utils.URLDataProcessing;

@Configuration
public class UtilBeanConfig {

    @Bean("urlUtil")
    public URLDataProcessing getUrlUtil(){

        return new URLDataProcessing();
    }
    
}
