package net.media.emma.config;

import net.media.emma.utils.DomJsonUtils;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilBeanConfig {

    @Bean("urlUtil")
    public URLDataProcessing getUrlUtil() {

        return new URLDataProcessing();
    }

    @Bean
    public DomJsonUtils domUtils() {
        return new DomJsonUtils();
    }

}
