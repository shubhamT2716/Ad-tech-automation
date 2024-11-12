package net.media.emma.cop.testsuites.pagetests;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import net.media.emma.cop.testsuites.yaml.TestReportModel.ControllerData;
import net.media.emma.cop.testsuites.yaml.TestReportModel.TestStatus;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    Map<String, Map<String, Map<String, String>>> cookies;
    Map<String, URLChecksReportModel> urlChecksReport;
    Map<String, Map<String, Map<List<String>, Boolean>>> slotsReport;
    List<FeatureReport> featureReport;

    //	BusinessFeaturesDetected  businessFeaturesDetected;
//	EnvironmentDetails environmentDetails;
//	SlotsDetected slotsDetected; 
//	AllFeatureData allFeatureData;
//	
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestMetaDataBidex {
        String testName;
        TestStatus testStatus;
        String dateTime;
        String publisherUrl;
        String environment;
        String userAgent;
        String harFileDownloadLink;
        Controller bidexController;

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TestDataResult {
        TestMetaDataBidex bidex;
        Map<String, Boolean> businessFeaturesDetected;
//		Map<String , Object> environmentDetails;

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Controller {
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
        ControllerData bidexData;
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
