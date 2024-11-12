package net.media.emma.cop.testsuites.yaml;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestReportModel.Controller;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmokeTestRawDataDto {

    private SmokeTestSummeryDto smokeReport;

    private Map<String, TestReportModel> rawData;

    private Map<String, Controller> controllerReport;

    private Map<String, Map<String, List<String>>> errorMessage;

    private Map<String, String> testResult;

    private String fullReport;


}
