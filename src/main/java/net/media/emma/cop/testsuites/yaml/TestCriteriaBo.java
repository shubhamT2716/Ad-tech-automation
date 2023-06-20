package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestCriteriaBo {

    List<Map<String, String>> cookies;
    String userAgent;
    String publisherUrl;
    String environment;
    Map<String, String> networkCallsPositive;
    Map<String, String> networkCallsNegative;

    Map<String, Map<String, Set<String>>> keysToVerifyPositive;
    Map<String, Map<String, Set<String>>> keysToVerifyNegative;
    Map<String, Map<String, Set<String>>> keysToVerifyAnyOneOf;

    List<ResponseSimulateParams> responseSimulate;
    List<DomInjectParams> domInject;

    @Getter @Setter
    public static class ResponseSimulateParams{
        String urlPattern;
        String jsonPath;
        String overrideValue;
    }

    @Getter @Setter
    public static class DomInjectParams{
        String domSelector;
        String content;
        Boolean append;
    }   
}