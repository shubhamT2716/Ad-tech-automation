package net.media.emma.cop.testsuites.yaml;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidExController;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmokeTestRawDataDto {

	private SmokeTestSummeryDto smokeReport;

	private Map<String, TestReportModel> rawData;

	private Map<String, BidExController> bidExReport;

	private Map<String, Map<String, List<String>>> errorMessage;

}
