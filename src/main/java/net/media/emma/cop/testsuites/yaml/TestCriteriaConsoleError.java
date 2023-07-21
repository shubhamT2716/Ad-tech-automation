package net.media.emma.cop.testsuites.yaml;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter @Setter
public class TestCriteriaConsoleError {

    String browserType;
    String userAgent;
    String publisherUrl;
    String environment;
    List<Map<String, String>> cookies;
    String bidExchangeUrl;
}