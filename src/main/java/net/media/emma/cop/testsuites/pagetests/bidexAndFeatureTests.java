package net.media.emma.cop.testsuites.pagetests;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Set;import java.util.stream.Collector;


import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.Timing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Route;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BidExConfig;
import net.media.emma.cop.components.BrowserFactory;

import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.BidexController;
import net.media.emma.cop.testsuites.yaml.PlAssestion;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.FileOperations;
import net.media.emma.utils.URLDataProcessing;
import net.media.emma.utils.YamlUtils;



@Service
@Slf4j
public class bidexAndFeatureTests {

	BidExConfig bidExConfig = BidExConfig.builder().build();
	
	
	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
			"hbx-stage.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clienttag.js");// , "catchpoint_content.js",
		

	String finalConfig = "";
	String bidexResp = "";
	ObjectMapper mapper = new ObjectMapper(); 
	JsonNode replaced;

	@Autowired
	BrowserFactory browserFactory;
	@Autowired
	URLDataProcessing urlDataProcessing;

	@Autowired
	ObjectWriter jsonWriter;

	@Autowired
	ObjectMapper jsonReader;

	@Value("${harFileBasePath}")
	private String harFileBasePath;
	
	@Value("#{'${listOfFeature}'.split(',')}")
    private List<String> listOfFeatures;
	
	@Autowired
	private CommonBrowserUtil browserUtil;
	
	
	@Autowired
	private FileOperations fileOperations;
	
	@Value("${commonYamlFilePath}")
	private String commonYamlFile;
	
	@Value("${jsonFile}")
    private String bidexConfigSampleFilePath;
	
	@Autowired
	private YamlUtils yamlUtils;
	
	@Autowired
	private PlAssestion payloadAsserstions;

    public BidexFeatureReport bidexTests(TestCriteriaBo yaml) throws JsonMappingException, JsonProcessingException{

    	
    	List<String> urlFromNetworkTab=new ArrayList<>();
		List<Request> listOfReqest=new ArrayList<>();
    	
	

    	String harFile=browserUtil.getFileNameForHar();
    	
		
		Page page = BrowserFactory.getPageWithNewContext(yaml.getUserAgent(), null, harFileBasePath+harFile);
		try {
			

			page=replaceBidExUrl(page, yaml);
			
			
			page.onRequest(request ->{
				urlFromNetworkTab.add(request.url());
				listOfReqest.add(request);
			});
			
		page.onResponse(response -> {
			if(response.url().contains("bidexchange.js?")){
			try {
				if(response.status()==200){
				bidexResp = response.text();
				}
			} catch (Exception e) {
				log.info("Response Json failed");
			}
			}
        });
        page.navigate(yaml.getPublisherUrl());
        Thread.sleep(4000);
	
		} catch (Exception e) {
			log.info("Bidex not found");
		}

		JsonNode pageSlots = slotsDataFromPage(page);
		 int startIndex=0;


		 String startTarget = "{\"AaxDpCeiling\"";
		if (bidexResp.contains(startTarget)) {
			startIndex = bidexResp.indexOf(startTarget);
			startIndex += 12;

		}

		//control in Bidexconfig.java
		finalConfig = bidexResp.substring(startIndex-12);
		 JsonNode bidexJson= mapper.readValue(finalConfig, JsonNode.class);

		 featureChecks(urlFromNetworkTab, yaml);
		
		page.waitForTimeout(5000);
		List<Cookie> cookies = page.context().cookies();
		page.close();
		return bidExConfig.loadBidExConfig(bidexJson,listOfFeatures,yaml, harFile,bidexConfigSampleFilePath, cookies, getUrlAssertionsData(urlFromNetworkTab, listOfReqest, yaml),getBidexData(listOfReqest, yaml));
    }


	private JsonNode slotsDataFromPage(Page page){
		try {
			Object pubSlots = page.evaluate(adSlotScraping());
			//log.info("Slots data from page " + pubSlots.toString());
			JsonNode slotsExctracted = jsonReader.readTree(pubSlots.toString());
			return slotsExctracted;
		}catch (Exception e){
			log.info("Slots data from page failed");
		}

		return null;
	}

    
    private Page replaceBidExUrl(Page browserContext, TestCriteriaBo testData) {

		for (String currentHost : bidExHosts) {
			for (String currentController : bidExControlers) {

				browserContext.route("**/" + currentHost + "/" + currentController + "*", route -> {

					String replacedURL = "";
					String originalURl = route.request().url();

					for (String currentHostInner : bidExHosts) {
						if (originalURl.contains(currentHostInner)) {
							replacedURL = originalURl.replace(currentHost, testData.getEnvironment());
							break;
						}
					}

					if (testData.getEnvironment().matches(".*.hbistage.internal.ad.tech.*")) {

						String splitUrl[] = testData.getEnvironment().split("\\.");
						String username = splitUrl[0];
						String addToUrl = "&ad_username=" + username;
						replacedURL = replacedURL.concat(addToUrl);
					}

//					log.info("replace Url  =  " + replacedURL);
					route.resume(new Route.ResumeOptions().setUrl(replacedURL));
				});
			}
		}

		return browserContext;
	}
    
    
    public TestCriteriaBo meargeYaml(String yaml)
    {
    	Set<String> listofCommonYaml=fileOperations.listYamlFiles(commonYamlFile);
      	
    	net.media.emma.cop.testsuites.pagetests.TestCriteriaBo criteria=null;
    	
    	for(String commonFile: listofCommonYaml)
    	{
    		String yamlcontent=this.fileOperations.getCommonFile(commonFile);
    	
    		String mearge[]= {yaml,yamlcontent};
    		
    		
    		 criteria=yamlUtils.buildAutoTesterYaml(mearge);
       	}
    	
    	return criteria;
    }
    
    private URLChecksReportModel getUrlAssertionsData(List<String> requestList,List<Request> requests, TestCriteriaBo testData) {

		URLChecksReportModel urlChecks = URLChecksReportModel.builder().build();

		for (String requestUrl : requestList) {
			
//			if(!URLDataProcessing.isValidUrl(requestUrl))
//			{
//				System.err.println();
//				
//				continue;
//			}
			
			Map.Entry<String, String> urlData = positiveURLPatternExists(requestUrl, testData);

			if (urlData != null) {

				PositiveURLChecks labelExists = urlChecks.getPositiveUrlChecks().get(urlData.getKey());

				if (labelExists == null) {

					PositiveURLChecks puc = PositiveURLChecks.builder().label(urlData.getKey())
							.urlPattern(urlData.getValue())
							.matchingUrls(new ArrayList<String>(Arrays.asList(requestUrl)))
							.queryStringMap(new ArrayList<HashMap<String, String>>(
									Arrays.asList(urlDataProcessing.getQueryStringMapFromUrl(requestUrl))))
							.negativeKeyValChecks(testData.getKeysToVerifyNegative().get(urlData.getKey()))
							.positiveKeyValChecks(testData.getKeysToVerifyPositive().get(urlData.getKey()))
							.anyOneOfKeyValChecks(testData.getKeysToVerifyAnyOneOf().get(urlData.getKey())).build();

					urlChecks.getPositiveUrlChecks().put(urlData.getKey(), puc);
				} else {
					labelExists.getMatchingUrls().add(requestUrl);
					labelExists.getQueryStringMap().add(urlDataProcessing.getQueryStringMapFromUrl(requestUrl));
				}

				continue;
			}

			urlData = negativeURLPatternExists(requestUrl, testData);

			if (urlData != null) {
				NegativeURLChecks nuc = urlChecks.getNegativeUrlChecks().get(urlData.getKey());
				if (nuc == null) {
					urlChecks.getNegativeUrlChecks().put(urlData.getKey(),
							NegativeURLChecks.builder().label(urlData.getKey()).urlPattern(urlData.getValue())
									.matchingUrls(new ArrayList<String>((Arrays.asList(requestUrl)))).build());
				} else {
					nuc.getMatchingUrls().add(requestUrl);
				}
			}
		}

		urlChecks.updateMatchingUrlsCount();
		urlChecks.updateNetworkCallErrors(testData);
		urlChecks.verifyPositiveKeyValChecks();
//		if(testData.getBidderDataAssertions()!=null && testData.getBidderDataUrls()!=null)
//		{
//			payloadAsserstions.verifyBidderDataAssertions(urlChecks,requests,testData);
//		}
		
		return urlChecks;
	}
    
    
    public void featureChecks(List<String> requestList, TestCriteriaBo criteriaBo) {
    	
    	
    	for(String requestUrl: requestList )
    	{
    		
    		String urlData=positiveURLPatternExistsForFeature(requestUrl, criteriaBo);
    		
    		if(urlData!=null)
    		{
    			criteriaBo.getFeatureKeysPositive().entrySet().forEach(entry->{
    				
    				String key=entry.getKey();
    				
    			Map<String, Map<String, String>> value =entry.getValue();
    			
    			System.err.println("Key :  "+key);
    			
    			System.err.println("Value :  "+value);
    			
    			value.entrySet().forEach(valueEntry->{
    				
    				String vKey=valueEntry.getKey();
    				
    				Map<String, String> vValue= valueEntry.getValue();   
    				
    				if(urlData.contains(vKey))
    				{
    					System.out.println("vKey: "+ vKey);
    					System.out.println("vValue: "+ vValue);
    					
    					vValue.entrySet().forEach(r->{
    						
    						String fKey=r.getKey();
    						
    						String fValue=r.getValue();
    						
    						
    					int index=urlData.indexOf(vKey);
    					
    					String url=urlData.substring(index);
    					
    					int sIndex=url.indexOf(vKey);
    					int eIndex=url.indexOf("&");
    					
    					url=url.substring(sIndex, eIndex);
    					
    					
    					System.out.println("url : "+url);
    					
    					
    					if(url.contains(fKey)) {
    						
    						
    						Map<String, Object> map=new HashMap<>();
    						
    						 String[] keyValue = url.split("=");

    						 
    				            
    				            try {
									String  s= URLDecoder.decode(keyValue[0], "UTF-8");
									
									String v = URLDecoder.decode(keyValue[1], "UTF-8");
									
									map.put(s, v);
									
									Object vl=map.get(vKey);
									
									System.out.println(vl);
									
								} catch (UnsupportedEncodingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
    				        
    					}
    					
    					});
    					
    				}
    				
    			});
    			
    				
    				
    			});
    		}
    	
    	}
    	
    }
    
    private String positiveURLPatternExistsForFeature(String currentUrl, TestCriteriaBo testData) {

		for (Map.Entry<String, String> urlData : testData.getFeatureCallsPositive().entrySet()) {

			if (currentUrl.startsWith(urlData.getValue())) {

				return currentUrl;
			}
		}

		return null;
	}
    
    
    private Map.Entry<String, String> positiveURLPatternExists(String currentUrl, TestCriteriaBo testData) {

		for (Map.Entry<String, String> urlData : testData.getNetworkCallsPositive().entrySet()) {

			if (currentUrl.startsWith(urlData.getValue())) {

				return urlData;
			}
		}

		return null;
	}

	private Map.Entry<String, String> negativeURLPatternExists(String currentUrl, TestCriteriaBo testData) {

		for (Map.Entry<String, String> urlData : testData.getNetworkCallsNegative().entrySet()) {

			if (urlData.getValue().startsWith("pattern: ")) { // pattern: .*\.media\.net.*
				if (currentUrl.matches(urlData.getValue().substring(9))) { // substring
					return urlData;
				}
			}

			else if (currentUrl.startsWith(urlData.getValue())) {

				return urlData;
			}
		}

		return null;
	}
	
	
	private BidexController getBidexData(List<Request> listOfRequest, TestCriteriaBo criteriaBo)
	{
		BidexFeatureReport report=new BidexFeatureReport();
		
		BidexFeatureReport.BidexController bidexController=new BidexController();
		
		
		
		String regEx = ".*/(" + String.join("|", bidExHosts) + "|" + criteriaBo.getEnvironment() + ")/("
				+ String.join("|", bidExControlers) + ").*";

		List<Request> bidexRequest=new ArrayList<>();
		
		for(Request request:listOfRequest)
		{
			if(request.url().matches(regEx))
			{
				bidexRequest.add(request);
			}
		}

		for(Request request: bidexRequest)
		{
			bidexController.getListOfMatchingUrls().add(request.url());

			if(null != request.failure())
			{
				bidexController.getConsoleMessage().add(request.failure().toString());
				continue;
			}

			Timing timing = request.timing();


			if (null != timing) {
				Date date = new Date((long) (timing.responseEnd - timing.responseStart));
				bidexController.setTotalResponsetime(date.getTime() + "ms");
			} else {
				bidexController.setTotalResponsetime("Not Available");
			}

			bidexController.setTotalSize(request.sizes().responseBodySize + "b");
		}

		 if (bidexRequest.size() == 0) {
			 bidexController.getConsoleMessage().add("No bidex request found");
		 }

		 if (bidexRequest.size() > 1) {
			 bidexController.getConsoleMessage().add("Multiple bidex request found");
		}

		return bidexController;



		}
		
		
		
	
	

	private String adSlotScraping(){
		return"""
				let slotsData = [];

				function getAdUnitJs(){
				         return JSON.stringify(slotsData);
				     }

				if(googletag.pubads()){
				    googletag.pubads().getSlots().map(s => {
				        // console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
				        slotsData.push(s.getAdUnitPath(), s.getSizes(), s.getTargetingMap());

				    });

				}

				//console.log(JSON.stringify(slotsData, null, 4));

				getAdUnitJs();
				""";
	}
    
    
    
}
