package net.media.emma.cop.components;

//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


import java.text.SimpleDateFormat;
import java.util.*;

import com.microsoft.playwright.options.Cookie;
import org.springframework.beans.factory.annotation.Autowired;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.BidexController;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.TestMetaDataBidex;

import net.media.emma.cop.testsuites.pagetests.TestCriteriaBo;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;

@Builder @Getter @Setter @Slf4j @ToString
public class BidExConfig {

    @Autowired
    BrowserFactory browserFactory;
    @Autowired
    URLDataProcessing urlDataProcessing;

    @Autowired
    ObjectWriter jsonWriter;

    @Autowired
    ObjectMapper jsonReader;

    private JsonNode bidExContent;
    @Builder.Default
    public List < SlotsData > slotsData = new ArrayList < SlotsData > ();
    
    
    public List<Map<String, List<SlotsData>>> slots=new ArrayList<>();


    @Builder.Default
    ObjectMapper mapper = new ObjectMapper();
    
    
    @Autowired
    CommonBrowserUtil browserUtil;
    
    

  
    

    
    public BidexFeatureReport loadBidExConfig(JsonNode bidExContent, List<String> feature, TestCriteriaBo yaml, String harFile, String jsonPathFile, List<Cookie> cookies, URLChecksReportModel model,BidexController bidexController) {

//        if (bidExContent == null) {
//           
////        File sample = new File("src/main/java/net/media/emma/cop/components/sample/bidExConfigSample.json");
//          
//        	ClassPathResource classPathResource=new ClassPathResource(jsonPathFile+"/"+"bidExConfigSample.json");
//        	
//        	
//        	
//        	try {
//        		
//        		InputStream sample=classPathResource.getInputStream();
//        		
//                bidExContent = mapper.readTree(sample);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                log.info("Bidex null");
//
//            }
//        }
        

        setCridAdunitPathMapping(bidExContent);
        setDcnIntoCrid(bidExContent);
        setSizesIntoCrid(bidExContent);
        getInstreamDfpPlacements(bidExContent);
        getEnvironment(bidExContent);
        
        
//        JsonNode featureResult = mapper.convertValue(getFeaturesData(bidExContent, feature), JsonNode.class);
        
        return getFeaturesData(bidExContent, feature, yaml, harFile, cookies, model, bidexController);
        
    }

    //Getting list of cookies here, after waiting for 5 secs.
    public Map<String, String> displayCookies(List<Cookie> cookies)
    {
    	Map<String, String> allCookies=new HashMap<>();
    	for(Cookie cookie : cookies){
//            log.info("Cookie: " + cookie.name + " : " + cookie.value);
            
            allCookies.put(cookie.name, cookie.value);
            
        }
    	
    	return allCookies;
    }
    public String getEnvironmentName(String envdata)
    {
        Map<String , Object> map =new HashMap<>();
        map.put("ip2ct", "City");
        map.put("app", "Server");
        map.put("ip2sc", "State");
        map.put("ip2c", "Country");
        map.put("ip", "ip");

       
        String value=(String) map.get(envdata);

        return value;
    }
    
   public String getFeatureName(String name)
   {
   	Map<String, String> featureMap=new HashMap<>();

//   	featureMap.put("InStreamVideoDfpPlacements", "Instream Video ");
   	featureMap.put("osrm", "Outstream Video");
//   	featureMap.put("CridSizeMapForMultiAd","AdStacking 1.0");
//   	featureMap.put("InsertSlotInLargeSlot","AdStacking 1.0");
//   	featureMap.put("MultiAdSzComb","AdStacking 2.0");
//   	featureMap.put("aucmods", "Ad-Stacking 2.0.0 ");
//   	featureMap.put("FloorPrice", "Floor Data");
//   	featureMap.put("KeyValuePairAssociations", "Key Value detected");
//   	featureMap.put("DfpPlacements", "DfpPlacements Value");
//    featureMap.put("PlacementProviderRules", "Placement-Provider-Rules -Value");
//    featureMap.put("fpd", "fpd Value ");
//    featureMap.put("cwrs", "cwrs Value ");
//    featureMap.put("MultiBidTemplateConfig", "MultiBidConfig Value ");
   	

   	
   	featureMap.put("InStreamVideoDfpPlacements","Instream Video");
   	featureMap.put("InsertSlotInLargeSlot", "AdStacking 1.0 - insertslots_in_largeslots");
   	featureMap.put("CridSizeMapForMultiAd", "AdStacking 1.0 - crid_size_map_for_multi_ad");
   	featureMap.put("hb_ad_stacking_features", "AdStacking 1.0 - hb_ad_stacking_features");
   	featureMap.put("MultiAdConfig", "AdStacking 2.0 - multi_ad_config");
   	featureMap.put("MultiAdSzComb", "AdStacking 2.0 - multi_ad_size_combinations");
   	featureMap.put("FloorPrice", "Floor Data");
	featureMap.put("KeyValuePairAssociations", "Key Value Detected");
	featureMap.put("DfpPlacements", "DfpPlacements Value");
	featureMap.put("PlacementProviderRules", "Placement Provider Rules Value Detected");
	featureMap.put("fpd", "fpd Value Detected ");
	featureMap.put("cwrs", "cwrs Value Detected ");
    featureMap.put("sizes", "Sizes Value Detected ");
    featureMap.put("Mediatypes", "MediaTypes Value Detected ");

    String value=(String) featureMap.get(name);
    
    if(value ==null)
    {
    	featureMap.put(name, name);
    	
    	value=featureMap.get(name);
    	
    	return value;
    }
    return value;

   	
   }
    

    

    private Map<String, Object> getEnvironment(JsonNode bidExContent) {
    	
//    	log.info("bid :" +bidExContent);
		
    	JsonNode envConfig = bidExContent.get("Environment");
        
        List<String> envDetails= Arrays.asList("ip2ct","app","ip2sc","ip2c","ip");
    
//        log.info("Environment " + envConfig.toString());

        Map<String , Object> feature =new HashMap<>();
        
        for(String env : envDetails)
        {
        	JsonNode envResult=envConfig.get(env);
   
        	feature.put(getEnvironmentName(env), envResult);
        
        }
        
       return feature;

	}

    
    
    

	public SlotsData getSlotById(String cridIn, String feature) {

		
		
        if (slotsData.size() > 0) {
            for (SlotsData slot: slotsData) {
                if (slot.crid.equals(cridIn)&& slot.getFeatureName().equals(feature)) {
//                   log.info("Slots data crid " + slotsData);
                    
                    return slot;
                   
                }
            }
        }

        slotsData.add(SlotsData.builder().crid(cridIn).featureName(feature).build());
//       log.info("Slots data crid " + slotsData);
       
        for (SlotsData slot: slotsData) {
            if (slot.crid.equals(cridIn)&& slot.getFeatureName().equals(feature)) {
//           	System.err.println(slotsData);
                return slot;
            }
        }
        
        
//       solts.put(cridIn, slotsData);
               return null;

    }

    public void setCridAdunitPathMapping(JsonNode bidExContent) {
        List < String > crids = new ArrayList < String > ();

        JsonNode cridConfig = bidExContent.get("DfpPlacements");
        try {
            JsonNode rootNode = mapper.readTree(cridConfig.toString());
            rootNode.fields().forEachRemaining(entry -> {
                crids.add(entry.getKey());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String cridList: crids) {
            JsonNode cridNode = cridConfig.get(cridList).get("targ").get(0).get("value");
            String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
            SlotsData data=getSlotById(cridList.toString(),"DfpPlacements");
            data.setAdunitPath(adUnitPathTrimmed);
//            data.setFeatureName("DfpPlacements");
            

        }
    }

    public void setDcnIntoCrid(JsonNode bidExContent) {
        //get dcn mapping from sample bidex config
    	
    	Map<String , Object> map=new HashMap<>();
        JsonNode dcnConfig = bidExContent.get("PlacementProviderRules");
        if (dcnConfig != null && dcnConfig.isObject()) {
            dcnConfig.fields().forEachRemaining(entry -> {
                String cridKey = entry.getKey();
                JsonNode cridNode = entry.getValue();
                JsonNode getDcn = null;
//                 log.info("cridNode " + cridNode.toString());
     
               
         
                if (cridNode != null & cridNode.isObject()) {
                    try {
                      Map<String, Object> getSize= mapper.convertValue(cridNode, Map.class);
                    	
                        getDcn = cridNode.get("25014").get("pp").get("dcn");
                        
                        if (getDcn.toString() != null) {
                        	 SlotsData  data=new SlotsData();
                          data=getSlotById(cridKey,"PlacementProviderRules");
                          data.setDcn(getDcn.toString());
//                          data.setFeatureName("PlacementProviderRules");
                        }

                    } catch (Exception e) {
                        // TODO: handle exception
                          log.error("DCN does not exist for crid " + cridKey);
                    	
                    }
                }
            });
        }
        
    }


//Getting each crid-> {provider->{mediatype & sizes}}

    public void setSizesIntoCrid(JsonNode bidExContent) {


        Map<String, Map<List<String>, Map<String , String>>> sizesData = new HashMap<>();
        JsonNode sizeConfig = bidExContent.get("PlacementProviderRules");
        if (sizeConfig != null && sizeConfig.isObject()) {
            sizeConfig.fields().forEachRemaining(entry -> {
                String cridKey = entry.getKey();
                sizesData.put(cridKey, new HashMap<>());
                JsonNode cridNode = entry.getValue();
                if (cridNode != null & cridNode.isObject()) {
                    try {
                        cridNode.fields().forEachRemaining(entries->{
                            List<String> provider = Collections.singletonList(entries.getKey());
                            sizesData.get(cridKey).put(provider, new HashMap<>());
                            JsonNode sizesvalue = entries.getValue();
                            if(sizesvalue!=null && sizesvalue.isObject()) {
                                JsonNode szs = sizesvalue.get("szs");
                                if(szs !=null) {
                                    szs.forEach(sizeNode -> {
                                        String sz = sizeNode.get("sz").toString();
                                        String mediaType = sizeNode.get("mediatype").toString();
                                        sizesData.get(cridKey).get(provider).put(sz, mediaType);
                                    });
                                }
                            }
                        });

                    } catch (Exception e) {
                        // TODO: handle exception
                        log.error("Sizes extraction failed for crid " + cridKey);


                    }
                }
            });
        }





        //Display sizesData
//         for (Map.Entry<String, Map<List<String>, Map<String, String>>> entry1 : sizesData.entrySet()) {
//             String key = entry1.getKey();
//             Map<List<String>, Map<String, String>> value = entry1.getValue();
//             for (Map.Entry<List<String>, Map < String, String>> entry2 : value.entrySet()) {
//                 List<String> key1 = entry2.getKey();
//                 Map<String, String> value1 = entry2.getValue();
//                 for (Map.Entry <String, String> entry3 : value1.entrySet()) {
//                     String key2 = entry3.getKey();
//                     String value2 = entry3.getValue();
//                                                     log.info("Crid " + key);
//                                                     log.info("Provider " + key1);
// //                                                            log.info("value1 "+ value1);
//                                                     log.info("Size " + key2);
//                                                     log.info("Mediatype " + value2);

//                 }
//             }
//         }





    }
   
    private void getInstreamDfpPlacements(JsonNode bidExContent) {
        
 List < String > videoCrids = new ArrayList < String > ();

        JsonNode vCridConfig = bidExContent.get("InStreamVideoDfpPlacements");
        try {
            JsonNode rootNode = mapper.readTree(vCridConfig.toString());
            rootNode.fields().forEachRemaining(entry -> {
                videoCrids.add(entry.getKey());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        for (String cridList: videoCrids) {
            JsonNode cridNode = vCridConfig.get(cridList).get("targ").get(0).get("value");
            String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
            SlotsData data=getSlotById(cridList.toString(),"InStreamVideoDfpPlacements" );
            data.setAdunitPath(adUnitPathTrimmed);
//            data.setFeatureName("InStreamVideoDfpPlacements");
        }        
        
    }
    
    
    private BidexFeatureReport getFeaturesData(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteriaBo yaml, String harFile, List<Cookie> cookies, URLChecksReportModel reportModel, BidexController bidexController){
 
    	 Map<String ,Object> featureTestResult=new HashMap<>();
    	
       Map<String ,Object> featureResult=new HashMap<>();
       
      
       for(String feature : listOfFeatures)
        {
          
        	JsonNode basedOnFeature = bidexContent.get(feature);
       	
        	String featureName=getFeatureName(feature);
        	
        	if(basedOnFeature ==null)
        	{
        		featureTestResult.put(featureName, false);
        	}
        	else {
        		
        		 basedOnFeature.fields().forEachRemaining(entry->{
              		
              		if(entry.getKey()==null )
              		{
              			featureTestResult.put(featureName, false);
              		}
              		else {
              			featureTestResult.put(featureName, true);
              		}
              		
              	});
        	}
        	
            Object featureMap=mapper.convertValue(basedOnFeature, Object.class);

           featureResult.put(feature, featureMap);
        }
       
       Map<String, Object> solts=new HashMap<>();
        
        for(SlotsData data: slotsData)
        {
//        	log.info("slotsData : "+slotsData);
        	solts.put(data.getCrid(), data);
        }
        
        
        BidexFeatureReport bidexFeatureReport=BidexFeatureReport.builder().testMetaDataBidex(TestMetaDataBidex.builder().dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
        		.testName("Auto Tester Report").environment(yaml.getEnvironment()).publisherUrl(yaml.getPublisherUrl()).harFileDownloadLink("harDownload/"+harFile ).userAgent(yaml.getUserAgent())
        		.bidexController(bidexController).build()).build();
        
        
        bidexFeatureReport.setAllFeatureData(featureResult);
        bidexFeatureReport.setBusinessFeaturesDetected(featureTestResult);
        bidexFeatureReport.setEnvironmentDetails(getEnvironment(bidexContent));
        bidexFeatureReport.setSlotsDetected(solts);
        bidexFeatureReport.setCookies(displayCookies(cookies));
        bidexFeatureReport.setUrlChecksReport(reportModel);
       

        
        return bidexFeatureReport;
        

    }

   

    @Getter @Setter @Builder @ToString @NoArgsConstructor @AllArgsConstructor
    public static class SlotsData {

    	public String featureName;
        public String crid;
        public String dcn;
        public String adunitPath;
        public String sizes;
        public String mediaTypes;
        public String providerId;
//        Map<String, Object> providerData=new HashMap<>();

        @Builder.Default
        public List < ProvidersData > providersData = new ArrayList < ProvidersData > ();

        public ProvidersData getProviderById(int id) {
            for (ProvidersData provider: providersData) {
                if (provider.pvid == id) {
                    return provider;
                }
            }
            return null;
        }

//        public List < String > getSizes() {
//            List < String > sizes = new ArrayList < String > ();
//            for (ProvidersData provider: providersData) {
//                for (ProvidersData.Sizes size: provider.sizes) {
//                    sizes.add(size.sz);
//                }
//            }
//            return sizes;
//        }

    }

    @Getter @Setter @Builder
    public static class ProvidersData {

        int pvid;
        String br;
        String ecc;
        String hb;
        String prf;
        String id;
        List < Sizes > sizes;

        @Getter @Setter @Builder
        public static class Sizes {
            String epc;
            String sz;
            String mediaType;
        }

    }

}