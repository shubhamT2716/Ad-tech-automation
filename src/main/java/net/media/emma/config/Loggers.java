package net.media.emma.config;

import net.media.emma.coptests.AssertNetworkCalls;
import net.media.emma.coptests.SoftAssertion;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.media.emma.service.BrowserRemote;

@Configuration
public class Loggers {

    @Bean
    public Logger standardLogger() {
        return org.slf4j.LoggerFactory.getLogger("standardLogger");
    }

    @Bean
    public BrowserRemote browserRemote() {
        return new net.media.emma.impl.playwright.PlayWrightBrowserRemote();
    }

    @Bean
    public AssertNetworkCalls assertAllCalls() {
        return new AssertNetworkCalls();
    }

    /*@Bean
    public ConsoleError consoleError() {
        return new ConsoleError();
    }*/

    @Bean public SoftAssertion softAssertion(){return new SoftAssertion();}


}
