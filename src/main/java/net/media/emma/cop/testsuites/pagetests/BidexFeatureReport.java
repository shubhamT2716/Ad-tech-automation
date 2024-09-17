package net.media.emma.cop.testsuites.pagetests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.options.Cookie;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;
import net.media.emma.cop.testsuites.yaml.TestReportModel.TestStatus;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BidexFeatureReport {

	TestMetaDataBidex testMetaDataBidex;
	Map<String, Boolean> businessFeaturesDetected;
//	Map<String , Object> environmentDetails;
//	Map<String, List<SlotsData>> slotsDetected;
	JsonNode slotsData;
	Map<String, Object> allFeatureData;
	Map<String, Map<String, Map<String,String>>> cookies;
	Map<String, URLChecksReportModel> urlChecksReport;
	Map<String, Map<String, Map<List<String>, Boolean>>> slotsReport;
	List<FeatureReport> featureReport;
	
//	BusinessFeaturesDetected  businessFeaturesDetected;
//	EnvironmentDetails environmentDetails;
//	SlotsDetected slotsDetected; 
//	AllFeatureData allFeatureData;
//	
	@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
	public static class TestMetaDataBidex {
		String testName;
		TestStatus testStatus;
		String dateTime;
		String publisherUrl;
		String environment;
		String userAgent;
		String harFileDownloadLink;
		BidexController bidexController;
		
	}
	
	@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
	public static class TestDataResult{
		TestMetaDataBidex bidex;
		Map<String, Boolean> businessFeaturesDetected;
//		Map<String , Object> environmentDetails;
		
	}
	
	@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
	public static class BidexController{
//		@Builder.Default
//		List<String> listOfMatchingUrls=new ArrayList<>();
//		String totalSize;
//		String totalResponsetime;
//		@Builder.Default
//		List<String> consoleMessage=new ArrayList<>();
		
		@Builder.Default
		List<String> matchingUrls = new ArrayList<>();
		String totalSizeDownloaded;
		String totalResponseTime;
		@Builder.Default
		List<String> consoleMessages = new ArrayList<>();
		BidexData bidexData;
	}
	
//	
//	@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
//	public static class BusinessFeaturesDetected{
//		
//		Map<String, Object> featureDetails;
//	}
//	
//	@Getter @Setter @Builder @NoArgsConstructor
//	@AllArgsConstructor
//	public static class EnvironmentDetails{
//		Map<String, Object> envDetails; 
//	}
//	
//	@Getter @Setter @Builder @NoArgsConstructor
//	@AllArgsConstructor
//	public static class SlotsDetected{
//		Map<String, Object> slotsData;
//	}
//	
//	
//	@Getter @Setter @Builder @NoArgsConstructor
//	@AllArgsConstructor
//	public static class AllFeatureData{
//		Map<String,Object> featureData;
//	}
	
	
	
	
	
	
	
}
