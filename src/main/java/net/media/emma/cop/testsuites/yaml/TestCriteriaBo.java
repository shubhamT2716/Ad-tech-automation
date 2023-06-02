package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestCriteriaBo {

    List<Map<String, String>> cookies;
    String userAgent;
    String publisherUrl;
    String environment;
    List<Map<String, String>> networkCallsPositive;
    List<Map<String, String>> networkCallsNegative;

    List<Map<String, Map<String, List<String>>>> keysToVerifyPositive;
    List<Map<String, Map<String, List<String>>>> keysToVerifyNegative;
    List<Map<String, Map<String, List<String>>>> keysToVerifyAnyOneOf;

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