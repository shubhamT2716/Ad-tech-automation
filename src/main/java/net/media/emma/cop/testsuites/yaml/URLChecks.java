package net.media.emma.cop.testsuites.yaml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class URLChecks {
    
    @Builder.Default
    Map<String, PositiveURLChecks> positiveUrlChecks = new HashMap<String, PositiveURLChecks>();
    @Builder.Default
    Map<String, NegativeURLChecks> negativeUrlChecks = new HashMap<String, NegativeURLChecks>();

    @Getter @Setter @Builder
    public static class PositiveURLChecks{
        String label;
        String urlPattern;

        @Builder.Default
        List<String> matchingUrls = new ArrayList<>();
        
        @Builder.Default
        Map<String, List<String>> positiveKeyValChecks = new HashMap<String, List<String>>();
        
        @Builder.Default
        Map<String, List<String>> negativeKeyValChecks = new HashMap<String, List<String>>();
        
        @Builder.Default
        Map<String, List<String>> anyOneOfKeyValChecks = new HashMap<String, List<String>>();
    }

    @Getter @Setter @Builder
    public static class NegativeURLChecks{
        String label;
        String urlPattern;

        @Builder.Default
        List<String>  matchingUrls = new ArrayList<>();
    }
}