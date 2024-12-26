package net.media.emma.cop.components;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO: to be used in future

@Getter
@Setter
public final class TestSuitesList {

    Map<TestSuiteNames, TestSuiteData> testSuites = new ConcurrentHashMap<>() {{
        put(TestSuiteNames.YAML_BASED_SANITY, TestSuiteData.builder()
                .testSuiteName("YAML based Sanity - Test Suite")
                .testSuiteDescription("Sanity Tests based on customizable YAML config")
                .testSuiteUrl("/yamlTestSuite/")
                .testSuiteYamlFileLocation("src/main/resources/yamlTestCriteriaFiles/")
                .build());

        put(TestSuiteNames.LOGGING_PIXEL, TestSuiteData.builder()
                .testSuiteName("Logging Pixel Data Validations - Test Suite")
                .testSuiteDescription("Logging Pixel Data Validation Tests")
                .testSuiteUrl("/testSuite3")
                .testSuiteYamlFileLocation("classpath:loggingPixelTestSuite.yaml")
                .build());

        put(TestSuiteNames.COMPLIANCE, TestSuiteData.builder()
                .testSuiteName("Compliance - GDPR/TCF, CCPA, USPA etc - Test Suite")
                .testSuiteDescription("Compliance - GDPR/TCF, CCPA, USPA etc - Tests")
                .testSuiteUrl("/privacyComplianceTestSuite/")
                .testSuiteYamlFileLocation("src/main/resources/complianceYamlFiles/")
                .build());

        put(TestSuiteNames.PAGE_LEVEL_CHECKS, TestSuiteData.builder()
                .testSuiteName("Page Level Checks - Test Suite")
                .testSuiteDescription("Page Level Checks - Test Suite")
                .testSuiteUrl("/pageLevelTests/")
                .testSuiteYamlFileLocation("classpath:pageLevelTests.yaml")
                .build());
    }};

    enum TestSuiteNames {
        YAML_BASED_SANITY,
        LOGGING_PIXEL,
        COMPLIANCE,
        PAGE_LEVEL_CHECKS,
    }

    @Getter
    @Setter
    @Builder
    public static class TestSuiteData {
        String testSuiteName;
        String testSuiteDescription;
        String testSuiteUrl;
        String testSuiteYamlFileLocation;
    }
}
