package net.media.emma.cop.testsuites.pagetests;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.media.emma.cop.testsuites.yaml.TestReportModel.TestStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidexFeatureReport {

 	TestMetaDataBidex testMetaDataBidex;
	BusinessFeaturesDetected  businessFeaturesDetected;
	SlotsDetected slotsDetected; 
	AllFeatureData allFeatureData;
	
	@Getter @Setter @Builder
	public static class TestMetaDataBidex {
		String testName;
		TestStatus testStatus;
		String dateTime;
		String publisherUrl;
		String environment;
		String userAgent;
		String harFileDownloadLink;
	}
	
	@Getter @Setter @Builder
	public static class BusinessFeaturesDetected{
		
		Map<String, Object> featureDetails;
	}
	
	@Getter @Setter @Builder
	public static class Environment{
		Map<String, Object> envDetails; 
	}
	
	@Getter @Setter @Builder
	public static class SlotsDetected{
		Map<String, Object> slotsData;
	}
	
	
	@Getter @Setter @Builder
	public static class AllFeatureData{
		Map<String,Object> featureData;
	}
	
	
	
}
