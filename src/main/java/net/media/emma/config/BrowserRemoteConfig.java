package net.media.emma.config;

import net.media.emma.cop.components.BrowserFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BrowserRemoteConfig {

    @Bean
    public BrowserFactory browserFactory() {
        return new BrowserFactory();
    }
}