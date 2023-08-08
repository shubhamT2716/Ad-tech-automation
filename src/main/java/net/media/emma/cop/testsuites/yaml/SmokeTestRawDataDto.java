package net.media.emma.cop.testsuites.yaml;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmokeTestRawDataDto {

	private SmokeTestSummeryDto smokeReport;

	private Map<String, TestReportModel> rawData;

}
