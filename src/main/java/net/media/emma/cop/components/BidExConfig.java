//package net.media.emma.cop.components;
//
////import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
//
//
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import com.microsoft.playwright.Request;
//import com.microsoft.playwright.options.Cookie;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.ObjectWriter;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.ToString;
//
//import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
//import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.BidexController;
//import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.TestMetaDataBidex;
//import net.media.emma.cop.testsuites.pagetests.FeatureReport;
//import net.media.emma.cop.testsuites.pagetests.TestCriteriaBo;
//import net.media.emma.cop.testsuites.pagetests.bidexAndFeatureTests;
//import net.media.emma.utils.CommonBrowserUtil;
//import net.media.emma.utils.FileOperations;
//import net.media.emma.utils.URLDataProcessing;
//import net.media.emma.utils.YamlUtils;
//import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
//
//
//
//
//@Component
//
//public class BidExConfig {
//
//    @Autowired
//    BrowserFactory browserFactory;
//    @Autowired
//    URLDataProcessing urlDataProcessing;
//
//    @Autowired
//    ObjectWriter jsonWriter;
//
//    @Autowired
//    ObjectMapper jsonReader;
//
//    private JsonNode bidExContent;
//    @Builder.Default
//    public List < SlotsData > slotsData = new ArrayList < SlotsData > ();
//    
//    
//  
//
//
//    @Builder.Default
//    ObjectMapper mapper = new ObjectMapper();
//    
//    
//    @Autowired
//    CommonBrowserUtil browserUtil;
//    
//  
//    
//    @Autowired
//    private FileOperations fileOperations;
//    
//    @Autowired
//	private YamlUtils yamlUtils;
//    
//    @Autowired
//    private bidexAndFeatureTests bidexAndFeatureTests;
//    
// 
//    List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
//            "hbx-stage.media.net","hbx-stage-2.media.net","hbx-stage-3.media.net","hbx-stage-4.media.net","hbx-stage-1.media.net","ssp-stage-hb.media.net","ssp-stage-hb-1.media.net","ssp-stage-hb-2.media.net","ssp-stage-hb-3.media.net","ssp-stage-hb-4.media.net");
//    List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js","ss/nes/bidexl2.js");
//    
//    
////    public BidexFeatureReport loadBidExConfig(JsonNode bidExContent, List<String> feature, TestCriteriaBo yaml, String harFile, String jsonPathFile, Map<String, List<Cookie>> cookies, URLChecksReportModel model,BidexController bidexController, FeatureReport featureReport) {
//
//    	public BidexFeatureReport loadBidExConfig(JsonNode bidExContent, List<String> feature, TestCriteriaBo yaml, String harFile,Map<String, List<Cookie>> cookies,BidexController bidexController, String yamlTc, List<String> urlNetworkTab, List<Request>  requests) {
//
////        if (bidExContent == null) {
////           
//////        File sample = new File("src/main/java/net/media/emma/cop/components/sample/bidExConfigSample.json");
////          
////        	ClassPathResource classPathResource=new ClassPathResource(jsonPathFile+"/"+"bidExConfigSample.json");
////        	
////        	
////        	
////        	try {
//    		
////        		
////        		InputStream sample=classPathResource.getInputStream();
////        		
////                bidExContent = mapper.readTree(sample);
////            } catch (IOException e) {
////                // TODO Auto-generated catch block
////                e.printStackTrace();
////                log.info("Bidex null");
////
////            }
////        }
//        
//
////        setCridAdunitPathMapping(bidExContent);
////        setDcnIntoCrid(bidExContent);
////
////        getInstreamDfpPlacements(bidExContent);
////        getEnvironment(bidExContent);
//        
//        
//
//        
////        return getFeaturesData(bidExContent, feature, yaml, harFile, cookies, model, bidexController, featureReport);
//        return getFeaturesData(bidExContent, feature, yaml, harFile, cookies, bidexController,yamlTc, urlNetworkTab,requests);
//        
//    }
//
//
//    public Map<String, Map<String, Map<String,String>>> displayCookies(Map<String, List<Cookie>> cookies)
//    {
//    	Map<String, Map<String, Map<String,String>>> listOfCookies=new HashMap<>();
//
//    	cookies.entrySet().forEach(entry->{
//
//    		Map<String, Map<String,String>> allcookiesData = new HashMap<>();
//            
//    		for(Cookie cookie : entry.getValue()){
//
//                allcookiesData.put(cookie.name, new HashMap<>());
//                allcookiesData.get(cookie.name).put("value", cookie.value);
//                allcookiesData.get(cookie.name).put("domain", cookie.domain);
//                allcookiesData.get(cookie.name).put("path", cookie.path);
//                allcookiesData.get(cookie.name).put("expires", cookie.expires.toString());
//                allcookiesData.get(cookie.name).put("httpOnly", cookie.httpOnly.toString());
//                allcookiesData.get(cookie.name).put("secure", cookie.secure.toString());
//                allcookiesData.get(cookie.name).put("sameSite", cookie.sameSite.toString());
//                //log.info("Cookie Name : "+cookie.name+" Cookie Domain : "+cookie.domain);
//
//            }
//
//    		listOfCookies.put(entry.getKey(), allcookiesData);
//
//    	});
//
//
//
//    	return listOfCookies;
//    }
//
//
//
//
//    public String getEnvironmentName(String envdata)
//    {
//        Map<String , Object> map =new HashMap<>();
//        map.put("ip2ct", "City");
//        map.put("app", "Server");
//        map.put("ip2sc", "State");
//        map.put("ip2c", "Country");
//        map.put("ip", "ip");
//
//       
//        String value=(String) map.get(envdata);
//
//        return value;
//    }
//    
//   public String getFeatureName(String name)
//   {
//   	Map<String, String> featureMap=new HashMap<>();
//
//   	featureMap.put("osrm","OutStream");
////   	featureMap.put("InStreamVideoDfpPlacements","Instream Video");
////   	featureMap.put("InsertSlotInLargeSlot", "AdStacking 1.0 - insertslots_in_largeslots");
////   	featureMap.put("CridSizeMapForMultiAd", "AdStacking 1.0 - crid_size_map_for_multi_ad");
////   	featureMap.put("hb_ad_stacking_features", "AdStacking 1.0 - hb_ad_stacking_features");
////   	featureMap.put("MultiAdConfig", "AdStacking 2.0 - multi_ad_config");
////   	featureMap.put("MultiAdSzComb", "AdStacking 2.0 - multi_ad_size_combinations");
////   	featureMap.put("FloorPrice", "Floor Data");
////	featureMap.put("KeyValuePairAssociations", "Key Value Detected");
////	featureMap.put("DfpPlacements", "DfpPlacements Value");
////	featureMap.put("PlacementProviderRules", "Placement Provider Rules Value Detected");
////	featureMap.put("fpd", "fpd Value Detected ");
////	featureMap.put("cwrs", "cwrs Value Detected ");
////    featureMap.put("sizes", "Sizes Value Detected ");
////    featureMap.put("Mediatypes", "MediaTypes Value Detected ");
//
//    String value=(String) featureMap.get(name);
//    
//    if(value ==null)
//    {
//    	featureMap.put(name, name);
//    	
//    	value=featureMap.get(name);
//    	
//    	return value;
//    }
//    return value;
//
//   	
//   }
//    
//
//    
//
//    private Map<String, Object> getEnvironment(JsonNode bidExContent) {
//    	
//    	
//    	List<String> clientagController=Arrays.asList("js/tags/clientag.js","clientag.js");	
//		String regexForMp= ".*/(" + String.join("|", bidExHosts) + ")/("
//                + String.join("|", clientagController) + ").*";
//		
//		
//		
//		
//    	
//    	JsonNode envConfig = bidExContent.get("Environment");
//        
//        List<String> envDetails= Arrays.asList("ip2ct","app","ip2sc","ip2c","ip");
//    
//        Map<String , Object> feature =new HashMap<>();
//        
//        for(String env : envDetails)
//        {
//        	JsonNode envResult=envConfig.get(env);
//   
//        	feature.put(getEnvironmentName(env), envResult);
//        
//        }
//        
//       return feature;
//
//	}
//  
//
////	public SlotsData getSlotById(String cridIn, String feature) {
////
////		
////		
////        if (slotsData.size() > 0) {
////            for (SlotsData slot: slotsData) {
////                if (slot.crid.equals(cridIn)&& slot.getFeatureName().equals(feature)) {
//////                   log.info("Slots data crid " + slotsData);
////                    
////                    return slot;
////                   
////                }
////            }
////        }
////
////        slotsData.add(SlotsData.builder().crid(cridIn).featureName(feature).build());
//////       log.info("Slots data crid " + slotsData);
////       
////        for (SlotsData slot: slotsData) {
////            if (slot.crid.equals(cridIn)&& slot.getFeatureName().equals(feature)) {
//////           	System.err.println(slotsData);
////                return slot;
////            }
////        }
////        
////        
//////       solts.put(cridIn, slotsData);
////               return null;
////
////    }
//
////    public void setCridAdunitPathMapping(JsonNode bidExContent) {
////        List < String > crids = new ArrayList < String > ();
////
////
////        JsonNode cridConfig = bidExContent.get("DfpPlacements");
////        try {
////            JsonNode rootNode = mapper.readTree(cridConfig.toString());
////            rootNode.fields().forEachRemaining(entry -> {
////                crids.add(entry.getKey());
////            });
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        for (String cridList: crids) {
////            JsonNode cridNode;
////          try{
////            cridNode = cridConfig.get(cridList).get("targ").get(0).get("value");
////            String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
////            SlotsData data=getSlotById(cridList.toString(),"DfpPlacements");
////            data.setAdunitPath(adUnitPathTrimmed);
////            
////            
//////            data.setFeatureName("DfpPlacements");
////            }catch(Exception e){
////                cridNode = cridConfig.get(cridList).get("div");
////                String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
////                SlotsData data=getSlotById(cridList.toString(),"DfpPlacements");
////                data.setAdunitPath(adUnitPathTrimmed);
////               
////            }
////
////        }
////    }
//
////    public void setDcnIntoCrid(JsonNode bidExContent) {
////       
////    	
//////    	Map<String , Object> map=new HashMap<>();
////        JsonNode dcnConfig = bidExContent.get("PlacementProviderRules");
////        if (dcnConfig != null && dcnConfig.isObject()) {
////            dcnConfig.fields().forEachRemaining(entry -> {
////                String cridKey = entry.getKey();
////                JsonNode cridNode = entry.getValue();
////                JsonNode getDcn = null;
//////                JsonNode getSize=null;
////                
//////                 log.info("cridNode " + cridNode.toString());
////     
////               
////         
////                if (cridNode != null & cridNode.isObject()) {
////                    try {
////                      Map<String, Object> getSize= mapper.convertValue(cridNode, Map.class);
////                      
////                      Map<String, List<Map<String, Object>>> sizes=new HashMap<>();
////                    	
////                        getDcn = cridNode.get("25014").get("pp").get("dcn");
////                        
////                        getSize.entrySet().forEach(s->{
////                        	String k=s.getKey();
////                        	Object v=s.getValue();
////                        	
////                        	Map<String, Object> vl=mapper.convertValue(v, Map.class);
////                        	
////                        	Object object=vl.get("szs");
////                        	
////                        	List<Map<String, Object>>  getEpcSize=mapper.convertValue(object, List.class);
////                        	
//////                        	List<Map<String, Object>> listOfSize=new ArrayList<>();
//////                        	
//////                        	for(Map<String, Object> fm:getEpcSize)
//////                        	{
//////                        		Map<String, Object> getEpcAndSize=new HashMap<>();
//////                        		
//////                        		Object epc=fm.get("epcMap");
//////                        		Object siz=fm.get("sz");
//////                        		Object mediaType=fm.get("mediatype");
//////                        		
//////                        		
//////                        		
//////                        		
//////                        		System.err.println(epc+" "+siz+" "+mediaType);
//////                        		
//////                        		getEpcAndSize.put("sz", siz);
//////                        		
//////                        		getEpcAndSize.put("mediaType", mediaType);
//////                        		
//////                        		listOfSize.add(getEpcAndSize);
//////                        		listOfSize.add((Map<String, Object>) epc);
//////                        		
//////                        	}
//////                        	
////                        	sizes.put(k, getEpcSize);
////                        	
////                        	
////                        });
////                        
////                        if (getDcn.toString() != null) {
////                        	 SlotsData  data=new SlotsData();
////                          data=getSlotById(cridKey,"PlacementProviderRules");
////                          data.setDcn(getDcn.toString());
////                          data.setSizes(sizes);
////                          //   data.setFeatureName("PlacementProviderRules");
////                          
////                        
////                        }
////
////                    } catch (Exception e) {
////                        // TODO: handle exception
////                          log.error("DCN does not exist for crid " + cridKey);
////                          String nullDcn = "null";
////                        SlotsData  data=new SlotsData();
////                        data=getSlotById(cridKey,"PlacementProviderRules");
////                        data.setDcn(nullDcn);
////                        
////                       
////                        
////                    }
////                }
////            });
////        }
////        
////    }
//
////    private  Map<String, Map<List<String>, Map<String, String>>> setSizesIntoCrid(JsonNode bidExContent) {
////
////
////        Map<String, Map<List<String>, Map<String , String>>> sizesData = new HashMap<>();
////        JsonNode sizeConfig = bidExContent.get("PlacementProviderRules");
////        if (sizeConfig != null && sizeConfig.isObject()) {
////            sizeConfig.fields().forEachRemaining(entry -> {
////                String cridKey = entry.getKey();
////                sizesData.put(cridKey, new HashMap<>());
////                JsonNode cridNode = entry.getValue();
////                if (cridNode != null & cridNode.isObject()) {
////                    try {
////                        cridNode.fields().forEachRemaining(entries->{
////                            List<String> provider = Collections.singletonList(entries.getKey());
////                            sizesData.get(cridKey).put(provider, new HashMap<>());
////                            JsonNode sizesvalue = entries.getValue();
////                            if(sizesvalue!=null && sizesvalue.isObject()) {
////                                JsonNode szs = sizesvalue.get("szs");
////                                if(szs !=null) {
////                                    szs.forEach(sizeNode -> {
////                                         String sz = null;
////                                         String mediaType = null;
////                                        try{
////                                         sz = sizeNode.get("sz").toString();
////                                         mediaType = sizeNode.get("mediatype").toString();
////                                        sizesData.get(cridKey).get(provider).put(sz, mediaType);
////                                        }catch(Exception e){
////                                        sz = "null";
////                                        mediaType = "null";
////                                        sizesData.get(cridKey).get(provider).put(sz, mediaType);
////
////                                        }
////                                    });
////                                }
////                            }
////                        });
////
////                        
////                        
////                    } catch (Exception e) {
////                        // TODO: handle exception
////                        log.error("Sizes extraction failed for crid " + cridKey);
////                    
////
////                    }
////                }
////            });
////           
////        }
////		return sizesData;
////
////
////
////
////
////        //Display sizesData
//////         for (Map.Entry<String, Map<List<String>, Map<String, String>>> entry1 : sizesData.entrySet()) {
//////             String key = entry1.getKey();
//////             Map<List<String>, Map<String, String>> value = entry1.getValue();
//////             for (Map.Entry<List<String>, Map < String, String>> entry2 : value.entrySet()) {
//////                 List<String> key1 = entry2.getKey();
//////                 Map<String, String> value1 = entry2.getValue();
//////                 for (Map.Entry <String, String> entry3 : value1.entrySet()) {
//////                     String key2 = entry3.getKey();
//////                     String value2 = entry3.getValue();
//////                                                     log.info("Crid " + key);
//////                                                     log.info("Provider " + key1);
////// //                                                            log.info("value1 "+ value1);
//////                                                     log.info("Size " + key2);
//////                                                     log.info("Mediatype " + value2);
////
//////                 }
//////             }
//////         }
////
////
////
////
////
////    }
//   
////    private void getInstreamDfpPlacements(JsonNode bidExContent) {
////        
//// List < String > videoCrids = new ArrayList < String > ();
////
////        JsonNode vCridConfig = bidExContent.get("InStreamVideoDfpPlacements");
////        try {
////            JsonNode rootNode = mapper.readTree(vCridConfig.toString());
////            rootNode.fields().forEachRemaining(entry -> {
////                videoCrids.add(entry.getKey());
////            });
////
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
////        for (String cridList: videoCrids) {
////            JsonNode cridNode = vCridConfig.get(cridList).get("targ").get(0).get("value");
////            String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
////            SlotsData data=getSlotById(cridList.toString(),"InStreamVideoDfpPlacements" );
////            data.setAdunitPath(adUnitPathTrimmed);
//////            data.setFeatureName("InStreamVideoDfpPlacements");
////            
////            System.err.println("In InStreamVideoDfpPlacements "+ data.getSizes());
////        }        
////        
////    }
//    
//    
////    private BidexFeatureReport getFeaturesData(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteriaBo yaml, String harFile, Map<String, List<Cookie>> cookies, URLChecksReportModel reportModel, BidexController bidexController , FeatureReport featureReport){
// 
//    	 private BidexFeatureReport getFeaturesData(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteriaBo yaml, String harFile, Map<String, List<Cookie>> cookies, BidexController bidexController, String yamlTc, List<String> urls, List<Request> requests){
//    		 
//    	
//    	
//       Map<String ,Object> featureResult=new HashMap<>();
//       
//       Map<String ,Boolean> featureTestResult=new HashMap<>();
//  	 
//  	 for(String feature : listOfFeatures)
//       {
//         
//       	JsonNode basedOnFeature = bidexContent.get(feature);
//      	
//       	String featureName=getFeatureName(feature);
//       	
//       
//       	
//       	if(basedOnFeature ==null)
//       	{
//       		
//       		featureTestResult.put(featureName, false);
//       	}
//       	else {
//       		
////       		 basedOnFeature.fields().forEachRemaining(entry->{
////             		
////       			System.err.println("in else   condition"+ entry.getKey()+ "  "+entry.getValue());
////       			 
////             		if(entry.getKey()==null )
////             		{
////             			System.err.println("in else if  condition"+ entry.getKey()+ "  "+entry.getValue());
////             			featureTestResult.put(featureName, false);
////             		}
////             		else {
////             			
////             			System.out.println("In else condition");
////             			
////             			featureTestResult.put(featureName, true);
////             		}
////             		
////             	});
//       		
//       		
//       		
//       		featureTestResult.put(featureName, true);
//       		
//       		
//       	}
//       	
//           Object featureMap=mapper.convertValue(basedOnFeature, Object.class);
//
//          featureResult.put(feature, featureMap);
//       }
//
//
//    	
////    	Map<String, Object> featureResult=getAllFeatureDetails(listOfFeatures, bidexContent);
////    	
////    	Map<String, Boolean> featureTestResult=getFeatureDetected(listOfFeatures, bidexContent);
//    	
//    	Map<String, URLChecksReportModel> urlChecks=new HashMap<>();
//    	
//
//    	
//    	List<FeatureReport> listOfFeatureReport=new ArrayList<>();
//    	
//    	
//    	featureTestResult.entrySet().forEach(entry->{
//    		
//    		if(entry.getValue()==true)
//    		{
//    			String feature=entry.getKey();
//    			
//    			
//    			
//    			TestCriteriaBo tesCriteria=bidexAndFeatureTests.buildTestCriteriaOnFeature( feature, yamlTc);
//    			
//    			
//    			URLChecksReportModel reportModel=bidexAndFeatureTests.getUrlAssertionsData(urls, requests, tesCriteria);
//    			
//    			
////    			System.err.println("Report Model: "+reportModel);
//    			
//    			FeatureReport featureReport=this.bidexAndFeatureTests.featureChecks(urls, requests, tesCriteria, reportModel,feature);
//    			
//    			urlChecks.put(feature, reportModel);
//    			
//    			listOfFeatureReport.add(featureReport);
//    			
//    			
//    			
//    			
//    		}
////    		else {
////    			
////    			
////    			
////    			URLChecksReportModel reportModel=bidexAndFeatureTests.getUrlAssertionsData(urls, requests, tesCriteria);
////    			
////    		}
//    		
//    	});
//    	
//       
////       Map<String, List<SlotsData>> solts=new HashMap<>();
////        
////        for(SlotsData data: slotsData)
////        {
////        	solts.computeIfAbsent(data.getCrid(), k -> new ArrayList<>()).add(data);
////        }
//        
//        
//        BidexFeatureReport bidexFeatureReport=BidexFeatureReport.builder().testMetaDataBidex(TestMetaDataBidex.builder().dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
//        		.testName("Auto Tester Report").environment(yaml.getEnvironment()).publisherUrl(yaml.getPublisherUrl()).harFileDownloadLink("harDownload/"+harFile ).userAgent(yaml.getUserAgent())
//        		.svr(getSvrFromBidex(bidexContent)).bidexController(bidexController).build()).build();
//        
//        
//        bidexFeatureReport.setAllFeatureData(featureResult);
//        
//        bidexFeatureReport.setBusinessFeaturesDetected(featureTestResult);
//        bidexFeatureReport.setEnvironmentDetails(getEnvironment(bidexContent));
////        bidexFeatureReport.setSlotsDetected(solts);
//        bidexFeatureReport.setCookies(displayCookies(cookies));
//        bidexFeatureReport.setUrlChecksReport(urlChecks);
////        bidexFeatureReport.setSlotsReport(getSlotsReport(reportModel, bidexContent));    
//        bidexFeatureReport.setFeatureReport(listOfFeatureReport);        
//
//        
//        return bidexFeatureReport;
//        
//
//    }
//    
//    
//   
//    
//    
//    
//    private Map<String , Object> getAllFeatureDetails(List<String> listOfFeaures, JsonNode bidexContent)
//    {
//    	Map<String ,Object> featureResult=new HashMap<>();
//    	
//    	
//    	for(String feature : listOfFeaures)
//    	{
//    		JsonNode basedOnFeature=bidexContent.get(feature);
//    		
//    		 Object featureMap=mapper.convertValue(basedOnFeature, Object.class);
//
//          featureResult.put(feature, featureMap);
//
//    		
//    	}
//    	
//    	return featureResult;
//    }
//    
//    
//    
//    
//    private Map<String , Boolean> getFeatureDetected(List<String> listOfFeatures,JsonNode bidexContent)
//    {
//    	 Map<String ,Boolean> featureTestResult=new HashMap<>();
//    	 
//    	 for(String feature : listOfFeatures)
//         {
//           
//         	JsonNode basedOnFeature = bidexContent.get(feature);
//        	
//         	String featureName=getFeatureName(feature);
//         	
//         	if(basedOnFeature ==null)
//         	{
//         		featureTestResult.put(featureName, false);
//         	}
//         	else {
//         		
//         		 basedOnFeature.fields().forEachRemaining(entry->{
//               		
//               		if(entry.getKey()==null )
//               		{
//               			featureTestResult.put(featureName, false);
//               		}
//               		else {
//               			featureTestResult.put(featureName, true);
//               		}
//               		
//               	});
//         	}
//         	
////             Object featureMap=mapper.convertValue(basedOnFeature, Object.class);
//
////            featureResult.put(feature, featureMap);
//         }
//    	 
//    	 
//    	 return featureTestResult;
//    	 
//    	 
//    }
//    
//    
//    public String getSvrFromBidex(JsonNode bidex)
//	{
//		JsonNode svr=bidex.get("SOURCE_CODE_VERSION");
//
//		return svr.textValue();
//		
//	}
//
//
//
//    @Getter @Setter @Builder @ToString @NoArgsConstructor @AllArgsConstructor
//    public static class SlotsData {
//
//    	public String featureName;
//        public String crid;
//        public String dcn;
//        public String adunitPath;
//        public Map<String, List<Map<String, Object>>> sizes;
//        public String mediaTypes;
//        public String providerId;
////        Map<String, Object> providerData=new HashMap<>();
//
//        @Builder.Default
//        public List < ProvidersData > providersData = new ArrayList < ProvidersData > ();
//
//        public ProvidersData getProviderById(int id) {
//            for (ProvidersData provider: providersData) {
//                if (provider.pvid == id) {
//                    return provider;
//                }
//            }
//            return null;
//        }
//
////        public List < String > getSizes() {
////            List < String > sizes = new ArrayList < String > ();
////            for (ProvidersData provider: providersData) {
////                for (ProvidersData.Sizes size: provider.sizes) {
////                    sizes.add(size.sz);
////                }
////            }
////            return sizes;
////        }
//
//    }
//
//    @Getter @Setter @Builder
//    public static class ProvidersData {
//
//        int pvid;
//        String br;
//        String ecc;
//        String hb;
//        String prf;
//        String id;
//        List < Sizes > sizes;
//
//        @Getter @Setter @Builder
//        public static class Sizes {
//            String epc;
//            String sz;
//            String mediaType;
//        }
//
//    }
//
//   
////    String urlLog = null ;
////    
////    public Map<String, Map<String, Map<List<String>, Boolean>>> getSlotsReport(URLChecksReportModel reportModel, JsonNode bidexContent)
////    {
////    	 
////    	try {
////
////            Map<String,Map<String, Map<List<String>, Boolean>>> slotsReport=new HashMap<>();
//////    		Map<String, Map<List<String>, Boolean>> slotsReport=new HashMap<>();
////     
////            Map<String, Map<List<String>, Map<String, String>>> sizesData = setSizesIntoCrid(bidexContent);
////            
////            urlLog="AWLog";
////            
////            PositiveURLChecks logData = reportModel.getPositiveUrlChecks().get("AWLog"); ;
////            
////            List<String> listOfMatchingUrl=logData.getMatchingUrls();
////            
////            if(listOfMatchingUrl.isEmpty()|| listOfMatchingUrl==null|| logData==null)
////            {
////            	urlLog="APLog";
////            	logData=reportModel.getPositiveUrlChecks().get("APLog");
////            	listOfMatchingUrl=logData.getMatchingUrls();
////            	
////            	if(listOfMatchingUrl.isEmpty()|| listOfMatchingUrl==null||logData==null)
////            	{
////            		urlLog="PRLog";
////                	logData=reportModel.getPositiveUrlChecks().get("PRLog");
////                	listOfMatchingUrl=logData.getMatchingUrls();
////            	}
////            	
////            }
////            
////            Set<String> set=new HashSet<>();
////            
////          
//////            Map<String, Map<List<String>, Map<String, String>>> getSlotsDataFromLog=new HashMap<>();
////            Map<String, Map<List<String>, Map<String, String>>> getSlotsDataFromLog=new HashMap<>();
////            
////            
////            for(String matchedUrl: listOfMatchingUrl)
////            {
////            	int index=matchedUrl.indexOf("&crid=");
////            	String crid=matchedUrl.substring(index+6, matchedUrl.indexOf("&", index+6));
////
////            	set.add(crid);
////            	     	
////            	
////            	int sizeIndex=matchedUrl.indexOf("&size=");
////            	int pvidIndex=matchedUrl.indexOf("&pvid=") ; 
////            	int getMediaType=matchedUrl.indexOf("res_mtype=");
////            	
////            	 String size=matchedUrl.substring(sizeIndex+6, matchedUrl.indexOf("&",sizeIndex+6));
////                 String pvid=matchedUrl.substring(pvidIndex+6, matchedUrl.indexOf("&",pvidIndex+6));
////                 List<String> listOfPvid=Collections.singletonList(pvid);
////                 String mediaType=matchedUrl.substring(getMediaType+10,matchedUrl.indexOf("&",getMediaType+10));
////            	
////                 getSlotsDataFromLog.computeIfAbsent(crid, k->new HashMap<>()).put(listOfPvid, Collections.singletonMap(size, mediaType));
////
////                 log.info("crid:"+crid+"  "+"size:"+size+"  "+"pvid:"+pvid+" "+"mediaType:"+mediaType);
////              
////            }
////
////            sizesData.entrySet().forEach(entry->{
////               			
////               			String crid=entry.getKey();
////               			
////               			getSlotsDataFromLog.entrySet().forEach(entryOfLogData->{
////               				
////               				if(crid.equals(entryOfLogData.getKey()))
////                   			{
////                   				Map<List<String>, Map<String, String>>  valueOfSizes=entry.getValue();
////                   				
////                   				Map<List<String>, Map<String, String>> responseValue=entryOfLogData.getValue();
////                   				
////
////                   				
////                   				responseValue.entrySet().forEach(response->{
////                   					
////                   					List<String> listOfPvid=response.getKey();
////                   					
////                   					valueOfSizes.entrySet().forEach(sizeResponse->{
////                   						
////                   						if(sizeResponse.getKey().equals(listOfPvid))
////                   						{
////                   							Map<String, String> data=sizeResponse.getValue();
////                   							Map<String, String> valueFromResponse=response.getValue();
////                   							
////                   							for(String key:data.keySet()) {
////                   								
////
////                   								String sz=key.replace("\"", "");
////                   								
////                   								String mType=data.get(key);
////                   								
////                   								String mediaTypes = mType.replace("\"", "");
////                   								
////                   								log.info("Key : "+sz+" Answer :"+mediaTypes);
////                   								
////                   								for(String sizeMediaType :valueFromResponse.keySet())
////                   								{
////                   									
////                   									
////                   									String mediaType=valueFromResponse.get(sizeMediaType);
////                   									
////                   									log.info("valueKey"+ sizeMediaType+ " valueAnswer :"+mediaType);
////
////                   									
////                   									if(sz.equals(sizeMediaType) && mediaTypes.equals(mediaType))
////                   									{
////	
////                                                        slotsReport.computeIfAbsent(urlLog, k->new HashMap<>()).put(crid, Collections.singletonMap(listOfPvid, true));
////                   									}
////                   									else {
////                                                        slotsReport.computeIfAbsent(urlLog, k->new HashMap<>()).put(crid, Collections.singletonMap(listOfPvid, false));
////                   									}
////                   									
////                   								}
////                   								
////                   							}
////                	
////                   						}
////                   						
////                   					});
////                   					
////                   				});	
////                   				
////                   			}
////               				
////               			});
////       
////               		});
////            
////           
////    	return slotsReport;
////    	}
////    	catch (Exception e) {
////    		log.info("Error "+e.getMessage());		
////    		return null;
////		}
////    	
////    }
//
//}