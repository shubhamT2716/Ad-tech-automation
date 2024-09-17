package net.media.emma.cop.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import com.microsoft.playwright.Request;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.Timing;

import lombok.extern.slf4j.Slf4j;
//import net.media.emma.cop.components.BidExConfig.SlotsData;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
import net.media.emma.cop.testsuites.pagetests.FeatureReport;

import net.media.emma.cop.testsuites.pagetests.FeatureTestServiceIn;
import net.media.emma.cop.testsuites.pagetests.TestCriteriaBo;
import net.media.emma.cop.testsuites.pagetests.bidexAndFeatureTests;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.BidexController;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.TestMetaDataBidex;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;

import net.media.emma.cop.testsuites.yaml.TestReportModel.BidexData;
import net.media.emma.utils.CommonBrowserUtil;

import net.media.emma.utils.URLDataProcessing;


@Service
@Slf4j
public class AutoTesterSuite implements AutoTesterSreviceIn {
	
	
	
	@Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlDataProcessing;

    @Autowired
    ObjectWriter jsonWriter;

    @Autowired
    ObjectMapper jsonReader;
    
    @Autowired
	private FeatureTestServiceIn criteria;

    
//    @Builder.Default
//    public List < SlotsData > slotsData = new ArrayList < SlotsData > ();
    
    
  


//    @Builder.Default
    ObjectMapper mapper = new ObjectMapper();
    
    
    @Autowired
    CommonBrowserUtil browserUtil;
    
  
    
//    @Autowired
//    private FileOperations fileOperations;
//    
//    @Autowired
//	private YamlUtils yamlUtils;
    
    @Autowired
    private bidexAndFeatureTests bidexAndFeatureTests;
    
    BidexData bd=new  BidexData();
 
    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
            "hbx-stage.media.net","hbx-stage-2.media.net","hbx-stage-3.media.net","hbx-stage-4.media.net","hbx-stage-1.media.net","ssp-stage-hb.media.net","ssp-stage-hb-1.media.net","ssp-stage-hb-2.media.net","ssp-stage-hb-3.media.net","ssp-stage-hb-4.media.net");
    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js","ss/nes/bidexl2.js");
    

    @Override
	public BidexFeatureReport loadBidExConfig(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteriaBo yaml, String harFile, Map<String, List<Cookie>> cookies, String yamlTc, List<String> urls,  List<Request> requests, List<String> consoleError,JsonNode slots) 
	{
		
		
		
		
		 Map<String ,Object> featureResult=new HashMap<>();
	       
	       Map<String ,Boolean> featureTestResult=new HashMap<>();
	  	 
	  	 for(String feature : listOfFeatures)
	       {
	         
	       	JsonNode basedOnFeature = bidexContent.get(feature);
	      	
	       	String featureName=getFeatureName(feature);
	       	
	       
	       	
	       	if(basedOnFeature ==null)
	       	{
	       		
	       		featureTestResult.put(featureName, false);
	       	}
	       	else {
	       		
//	       		 basedOnFeature.fields().forEachRemaining(entry->{
//	             		
//	       			System.err.println("in else   condition"+ entry.getKey()+ "  "+entry.getValue());
//	       			 
//	             		if(entry.getKey()==null )
//	             		{
//	             			System.err.println("in else if  condition"+ entry.getKey()+ "  "+entry.getValue());
//	             			featureTestResult.put(featureName, false);
//	             		}
//	             		else {
//	             			
//	             			System.out.println("In else condition");
//	             			
//	             			featureTestResult.put(featureName, true);
//	             		}
//	             		
//	             	});
	       		
	       		
	       		
	       		featureTestResult.put(featureName, true);
	       		
	       		
	       	}
	       	
	           Object featureMap=mapper.convertValue(basedOnFeature, Object.class);

	          featureResult.put(feature, featureMap);
	       }


	    	
//	    	Map<String, Object> featureResult=getAllFeatureDetails(listOfFeatures, bidexContent);
//	    	
//	    	Map<String, Boolean> featureTestResult=getFeatureDetected(listOfFeatures, bidexContent);
	    	
	    	Map<String, URLChecksReportModel> urlChecks=new HashMap<>();
	    	

	    	
	    	List<FeatureReport> listOfFeatureReport=new ArrayList<>();
	    	
	    	
	    	featureTestResult.entrySet().forEach(entry->{
	    		
	    		if(entry.getValue()==true)
	    		{
	    			String feature=entry.getKey();
	    			
	    			
	    			
	    			TestCriteriaBo tesCriteria=bidexAndFeatureTests.buildTestCriteriaOnFeature( feature, yamlTc);
	    			
	    			
	    			URLChecksReportModel reportModel=bidexAndFeatureTests.getUrlAssertionsData(urls, requests, tesCriteria);
	    			
	    			
//	    			System.err.println("Report Model: "+reportModel);
	    			
	    			FeatureReport featureReport=this.featureChecks(urls, requests, tesCriteria, reportModel,feature);
	    			
	    			urlChecks.put(feature, reportModel);
	    			
	    			listOfFeatureReport.add(featureReport);
	    			
	    			
	    			
	    			
	    		}
//	    		else {
//	    			
//	    			
//	    			
//	    			URLChecksReportModel reportModel=bidexAndFeatureTests.getUrlAssertionsData(urls, requests, tesCriteria);
//	    			
//	    		}
	    		
	    	});
	    	
	       
//	       Map<String, List<SlotsData>> solts=new HashMap<>();
//	        
//	        for(SlotsData data: slotsData)
//	        {
//	        	solts.computeIfAbsent(data.getCrid(), k -> new ArrayList<>()).add(data);
//	        }
	        
	    	
	    	BidexController bidexController=getBidExData(requests, yaml);
	    	bidexController.setConsoleMessages(consoleError);
	        
	        BidexFeatureReport bidexFeatureReport=BidexFeatureReport.builder().testMetaDataBidex(TestMetaDataBidex.builder().dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
	        		.testName("Auto Tester Report").environment(yaml.getEnvironment()).publisherUrl(yaml.getPublisherUrl()).harFileDownloadLink("harDownload/"+harFile ).userAgent(yaml.getUserAgent())
	        		.bidexController(bidexController).build()).build();
	        
	        
	        bidexFeatureReport.setAllFeatureData(featureResult);
	        
	        bidexFeatureReport.setBusinessFeaturesDetected(featureTestResult);
//	        bidexFeatureReport.setEnvironmentDetails(getEnvironment(bidexContent));
	        bidexFeatureReport.setSlotsData(slots);
	        bidexFeatureReport.setCookies(displayCookies(cookies));
	        bidexFeatureReport.setUrlChecksReport(urlChecks);
//	        bidexFeatureReport.setSlotsReport(getSlotsReport(reportModel, bidexContent));    
	        bidexFeatureReport.setFeatureReport(listOfFeatureReport);        

	        
	        return bidexFeatureReport;
		
		
	}
	
	
	private BidexController getBidExData(List<Request> listOfPwRequestObjects, TestCriteriaBo testData) {

		// get bidex url's Request object from listOfPwRequestObjects
		// assumer the pattern like this
		// /**hbx.media.net|warp.media.net|hb.yahoo.net|contextual.media.net**bidexchange.js|clienttag.js**
		// from pw req object will have actual url filed, size, time taken to load
		// js errors arising from this controller

		BidexController bidexReport = BidexController.builder().build();
		List<Request> bidExRequestObjects = getBidExRequestObjects(listOfPwRequestObjects, testData.getEnvironment());

		// get matching urls
		for (Request request : bidExRequestObjects) {
			bidexReport.getMatchingUrls().add(request.url());

			if (null != request.failure()) {
				bidexReport.getConsoleMessages().add("Request has failed. " + request.failure().toString());
				continue;
			}

			Timing timing = request.timing();

			if (null != timing) {
				Date date = new Date((long) (timing.responseEnd - timing.responseStart));
				bidexReport.setTotalResponseTime(date.getTime() + "ms");
			} else {
				bidexReport.setTotalResponseTime("Not Available");
			}

			bidexReport.setTotalSizeDownloaded(request.sizes().responseBodySize + "b");
			
			bidexReport.setBidexData(getBidExData(request));
			
			
			
		}

		if (bidExRequestObjects.size() == 0) {
			bidexReport.getConsoleMessages().add("No bidex request found");
		}

		if (bidExRequestObjects.size() > 1) {
			bidexReport.getConsoleMessages().add("Multiple bidex request found");
		}

		return bidexReport;

	}
	
	
	
public FeatureReport featureChecks(List<String> requestList,List<Request> requests, TestCriteriaBo criteriaBo, URLChecksReportModel model, String feature) {
    	
    	
    	for(String requestUrl: requestList )
    	{
    		
    		Map.Entry<String, String> urlData=positiveURLPatternExistsForFeature(requestUrl, criteriaBo);
    		
    		if(urlData!=null)
    		{
    			
    			FeatureReport featureReport=this.criteria.featureTestCasePositvie(urlData, requests,criteriaBo,model,feature);
    			
    			return featureReport;
    		}
    	
    	}
    	
    	return null;
    	
    }


private Map.Entry<String, String> positiveURLPatternExistsForFeature(String currentUrl, TestCriteriaBo testData) {

	for (Map.Entry<String, String> urlData : testData.getFeatureCallsPositive().entrySet()) {

		if (currentUrl.startsWith(urlData.getValue())) {
			
			urlData.setValue(currentUrl);
			

			return urlData;
		}
	}

	return null;
}
	
//	for (Map.Entry<String, String> urlData : testData.getFeatureCallsPositive().entrySet()) {
//
//		if (currentUrl.startsWith(urlData.getValue())) {
//
//			return urlData;
//		}
//	}
	
	
	

	
	private BidexData getBidExData(Request request) {

		BidexData bidexData = BidexData.builder().build();
		
		ObjectMapper mapper=new ObjectMapper();

		//get data from request
		// convert into request into json
		String response=request.response().text();
		
	
		
		try {
			
			List<String> clientagController=Arrays.asList("js/tags/clientag.js","clientag.js");	
			String regexForMp= ".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", clientagController) + ").*";
			
			List<String> bibex1=Arrays.asList("bidexchange.js");
			String regexForBidex1=".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", bibex1) + ").*";
			
			List<String> bidex2=Arrays.asList("ss/nes/bidexl2.js?");
			String regexForBidex2=".*/(" + String.join("|", bidExHosts) + ")/("
	                + String.join("|", bidex2) + ").*";
		
			
//			if(request.url().startsWith("https://warp.media.net/js/tags/clientag.js")|| request.url().startsWith("**//**/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-2.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-3.media.net/js/tags/clientag.js")|| request.url().startsWith("https://hbx-stage-4.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-1.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-2.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-3.media.net/js/tags/clientag.js")|| request.url().startsWith("https://ssp-stage-hb-4.media.net/js/tags/clientag.js"))
		if(request.url().matches(regexForMp))
			{
				
				
				String responseMP=request.response().text();
				
//				String target="new s.a({";
				String target="new r.a({";
				
		
				if(responseMP.contains(target))
				{
					
		
					int in=responseMP.indexOf(target);
			
					responseMP=responseMP.substring(in);
					
					Pattern pattern=Pattern.compile("new\\s+r\\.a\\((.*?)\\)");
					
					Matcher matcher=pattern.matcher(responseMP);
					
					if(matcher.find())
					{
						
						
						String extractedData=matcher.group(1);
						
						String svr = null;
						
						if(responseMP.contains("const i"))
						{
							int svrIndex=responseMP.indexOf("const i");
							
							responseMP=responseMP.substring(svrIndex);
							
//							System.out.println(responseMP);
							
							Pattern pattern2=Pattern.compile("const i=\"([^\"]+)\"");
							
							Matcher matcher2=pattern2.matcher(responseMP);
							
							if(matcher2.find())
							{
								
								System.out.println("match found");
								
								 svr = matcher2.group(1);
								
								log.info(svr);
								
							}
							
						}
						
						JsonNode bidData=mapper.readValue(extractedData, JsonNode.class);
						
						bidexData.setCid(bidData.get("cid").asText());
						
						if(svr==null)
						{
							bidexData.setSvr(bidData.get("sver").asText());	
						}else {
							bidexData.setSvr(svr);
						}
						
//						bidexData.setSvr(bidData.get("sver").asText());
						bidexData.setSugd(bidData.get("sugd").asText());
						bidexData.setCt(bidData.get("ip2ct").asText());
						
//						bidexData.setItype(bidData.get("itype").asText());
						
						
						return bidexData;
					}
		
				}
					
			}
//			else if(request.url().contains("**/bidexchange.js?")|| request.url().startsWith("https://hbx-stage-2.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-3.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage-4.media.net//bidexchange.js")|| request.url().startsWith("https://hbx-stage.media.net//bidexchange.js")|| request.url().startsWith("https://hbx.media.net//bidexchange.js?")|| request.url().startsWith("https://hbx.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb.media.net/bidexchange.js?")|| request.url().startsWith("https://ssp-stage-hb-1.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-2.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-3.media.net//bidexchange.js")|| request.url().startsWith("https://ssp-stage-hb-4.media.net//bidexchange.js")){
		else if(request.url().matches(regexForBidex1))
		{
		
			 int startIndex=0;


			 String startTarget = "{\"AaxDpCeiling\"";
			if (response.contains(startTarget)) {
				startIndex = response.indexOf(startTarget);
				startIndex += 12;

			}

			//control in Bidexconfig.java
			String finalString = response.substring(startIndex-12);
			 JsonNode bidexJson= mapper.readValue(finalString, JsonNode.class);

			
			JsonNode envData=bidexJson.get("Environment");
			
			bidexData.setCid(envData.get("cid").asText());
			bidexData.setSvr(envData.get("sver").asText());
			bidexData.setSugd(envData.get("sugd").asText());
			bidexData.setItype(envData.get("itype").asText());
			bidexData.setCt(envData.get("ip2ct").asText())	;	
			
			bd.setCid(envData.get("cid").asText());
			bd.setCt(envData.get("ip2ct").asText());
			bd.setSvr(envData.get("sver").asText());	
			bd.setSugd(envData.get("sugd").asText());
			bd.setItype(envData.get("itype").asText());
		}
			
//			if(request.url().startsWith("https://hbx-stage-1.media.net/ss/nes/bidexl2.js?")|| request.url().startsWith("https://hbx-stage-2.media.net/ss/nes/bidexl2.js?"))
			
		if(request.url().matches(regexForBidex2))
		{
				
				
				
				bidexData.setCid(bd.getCid());
				bidexData.setSvr(bd.getSvr());
				bidexData.setCt(bd.getCt());
				bidexData.setItype(bd.getItype());
				bidexData.setSugd(bd.getSugd());	
				
//				bd.setCid();
//				bd.setCt(envData.get("ip2ct").asText());
//				bd.setSvr(envData.get("sver").asText());	
//				bd.setSugd(envData.get("sugd").asText());
//				bd.setItype(envData.get("itype").asText());
			}
		
		
			
		}
		catch (Exception e) {
			
			log.error("Error in fetch Svr: "+e.getMessage());
			
			e.printStackTrace();
			
		}
		

		return bidexData;
	}
	
	
	
	   private List<Request> getBidExRequestObjects(List<Request> listOfPwRequestObjects, String environmentHost) {

	        // get bidex url's Request object from listOfPwRequestObjects
	        // assumer the pattern like this
	        // /**hbx.media.net|warp.media.net|hb.yahoo.net|contextual.media.net**bidexchange.js|clienttag.js**
	        // from pw req object will have actual url filed, size, time taken to load
	        // js errors arising from this controller

	        String regEx = ".*/(" + String.join("|", bidExHosts) + "|" + environmentHost + ")/("
	                + String.join("|", bidExControlers) + ").*";

	        List<Request> bidExRequestObjects = new ArrayList<>();

	        for (Request pwReq : listOfPwRequestObjects) {

	            // URL GLOBAL MATCHING
	            if (pwReq.url().matches(regEx)) {

	                bidExRequestObjects.add(pwReq);
	            }

	        }

	        return bidExRequestObjects;
	    }
	
	
	 public String getFeatureName(String name)
	   {
	   	Map<String, String> featureMap=new HashMap<>();

	   	featureMap.put("osrm","OutStream");
//	   	featureMap.put("InStreamVideoDfpPlacements","Instream Video");
//	   	featureMap.put("InsertSlotInLargeSlot", "AdStacking 1.0 - insertslots_in_largeslots");
//	   	featureMap.put("CridSizeMapForMultiAd", "AdStacking 1.0 - crid_size_map_for_multi_ad");
//	   	featureMap.put("hb_ad_stacking_features", "AdStacking 1.0 - hb_ad_stacking_features");
//	   	featureMap.put("MultiAdConfig", "AdStacking 2.0 - multi_ad_config");
//	   	featureMap.put("MultiAdSzComb", "AdStacking 2.0 - multi_ad_size_combinations");
//	   	featureMap.put("FloorPrice", "Floor Data");
//		featureMap.put("KeyValuePairAssociations", "Key Value Detected");
//		featureMap.put("DfpPlacements", "DfpPlacements Value");
//		featureMap.put("PlacementProviderRules", "Placement Provider Rules Value Detected");
//		featureMap.put("fpd", "fpd Value Detected ");
//		featureMap.put("cwrs", "cwrs Value Detected ");
//	    featureMap.put("sizes", "Sizes Value Detected ");
//	    featureMap.put("Mediatypes", "MediaTypes Value Detected ");

	    String value=(String) featureMap.get(name);
	    
	    if(value ==null)
	    {
	    	featureMap.put(name, name);
	    	
	    	value=featureMap.get(name);
	    	
	    	return value;
	    }
	    return value;
	    
	   }
	 
	 
	  
	  
//	  private Map<String, Object> getEnvironment(JsonNode bidExContent) {
//	    	
//	    	
//	    	List<String> clientagController=Arrays.asList("js/tags/clientag.js","clientag.js");	
//			String regexForMp= ".*/(" + String.join("|", bidExHosts) + ")/("
//	                + String.join("|", clientagController) + ").*";
//			
//			
//			
//			
//	    	
//	    	JsonNode envConfig = bidExContent.get("Environment");
//	        
//	        List<String> envDetails= Arrays.asList("ip2ct","app","ip2sc","ip2c","ip");
//	    
//	        Map<String , Object> feature =new HashMap<>();
//	        
//	        for(String env : envDetails)
//	        {
//	        	JsonNode envResult=envConfig.get(env);
//	   
//	        	feature.put(getEnvironmentName(env), envResult);
//	        
//	        }
//	        
//	       return feature;
//
//		}
	  
	  
//	  public String getEnvironmentName(String envdata)
//	    {
//	        Map<String , Object> map =new HashMap<>();
//	        map.put("ip2ct", "City");
//	        map.put("app", "Server");
//	        map.put("ip2sc", "State");
//	        map.put("ip2c", "Country");
//	        map.put("ip", "ip");
//
//	       
//	        String value=(String) map.get(envdata);
//
//	        return value;
//	    }
	  
	  
	  public Map<String, Map<String, Map<String,String>>> displayCookies(Map<String, List<Cookie>> cookies)
	    {
	    	Map<String, Map<String, Map<String,String>>> listOfCookies=new HashMap<>();

	    	cookies.entrySet().forEach(entry->{

	    		Map<String, Map<String,String>> allcookiesData = new HashMap<>();
	            
	    		for(Cookie cookie : entry.getValue()){

	                allcookiesData.put(cookie.name, new HashMap<>());
	                allcookiesData.get(cookie.name).put("value", cookie.value);
	                allcookiesData.get(cookie.name).put("domain", cookie.domain);
	                allcookiesData.get(cookie.name).put("path", cookie.path);
	                allcookiesData.get(cookie.name).put("expires", cookie.expires.toString());
	                allcookiesData.get(cookie.name).put("httpOnly", cookie.httpOnly.toString());
	                allcookiesData.get(cookie.name).put("secure", cookie.secure.toString());
	                allcookiesData.get(cookie.name).put("sameSite", cookie.sameSite.toString());
	                //log.info("Cookie Name : "+cookie.name+" Cookie Domain : "+cookie.domain);

	            }

	    		listOfCookies.put(entry.getKey(), allcookiesData);

	    	});



	    	return listOfCookies;
	    }

	
	
}
