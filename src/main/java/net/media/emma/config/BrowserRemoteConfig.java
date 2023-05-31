package net.media.emma.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.media.emma.components.BrowserFactory;

@Configuration
public class BrowserRemoteConfig {

    @Bean
    public BrowserFactory browserFactory() {
        return new BrowserFactory();
    }
}