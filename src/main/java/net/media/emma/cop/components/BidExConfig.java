package net.media.emma.cop.components;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport;
import net.media.emma.cop.testsuites.pagetests.BidexFeatureReport.TestMetaDataBidex;
import net.media.emma.cop.testsuites.pagetests.TestCriteria;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;

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
    


    @Builder.Default
    ObjectMapper mapper = new ObjectMapper();
    
    
    @Autowired
    CommonBrowserUtil browserUtil;

  
    

    //Data format
    // cridDetails = { "crid" : { "dcn" : "dcnValue", "adunitPath" : "adunitPathValue" } }
    // cridDetails = { "164457376" : { "dcn" : "8a969d570187874cae488063d27b00ab", "adunitPath" : "/22888152279/us/ysports/main/dt/us_ysports_main_dt_mid_right" } }
    // private Map<String, Map<String, String>> cridDetails = new HashMap<String, Map<String, String>>();

    public BidexFeatureReport loadBidExConfig(JsonNode bidExContent, List<String> feature, TestCriteria yaml ) {

        if (bidExContent == null) {
            File sample = new File("src/main/java/net/media/emma/cop/components/sample/bidExConfigSample.json");
            try {
                bidExContent = mapper.readTree(sample);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                log.info("Bidex null");

            }
        }
        
      
//         loadbidExContent(bidExContent);
        setCridAdunitPathMapping(bidExContent);
        setDcnIntoCrid(bidExContent);
        getInstreamDfpPlacements(bidExContent);
        getEnvironment(bidExContent);
        
        
//        JsonNode featureResult = mapper.convertValue(getFeaturesData(bidExContent, feature), JsonNode.class);
        
        return getFeaturesData(bidExContent, feature, yaml);
        
    }
    
    public String getEnvironmentName(String envdata)
    {

        // List < String > name = Arrays.asList("city","server","state","Country","ip");
        
        // for(String replace: name )
        // {
        // 	envdata.replace(envdata, replace);
        // }

        Map<String , Object> map =new HashMap<>();
        map.put("ip2ct", "City");
        map.put("app", "Server");
        map.put("ip2sc", "State");
        map.put("ip2c", "Country");
        map.put("ip", "ip");

        //get the value from the map
        String value=(String) map.get(envdata);

        return value;




    }
    
   public String getFeatureName(String name)
   {
   	Map<String, String> featureMap=new HashMap<>();

   	featureMap.put("InStreamVideoDfpPlacements", "Instream Video ");
   	featureMap.put("osrm", "Outstream Video");
   	featureMap.put("CridSizeMapForMultiAd","AdStacking 1.0");
   	featureMap.put("InsertSlotInLargeSlot","AdStacking 1.0");
   	featureMap.put("MultiAdSzComb","AdStacking 2.0");
   	featureMap.put("aucmods", "Ad-Stacking 2.0.0 ");
   	featureMap.put("FloorPrice", "Floor Data");
   	featureMap.put("KeyValuePairAssociations", "Key Value detected");
   	featureMap.put("DfpPlacements", "DfpPlacements Value");
    featureMap.put("PlacementProviderRules", "Placement-Provider-Rules -Value");
    featureMap.put("fpd", "fpd Value ");
    featureMap.put("cwrs", "cwrs Value ");
    featureMap.put("MultiBidTemplateConfig", "MultiBidConfig Value ");

    String value=(String) featureMap.get(name);
    
    if(value ==null)
    {
    	featureMap.put(name, name);
    	
    	value=featureMap.get(name);
    	
    	return value;
    }
    
//    log.info(name  +" :  "+value);

    return value;

   	
   }
    

    

    private Map<String, Object> getEnvironment(JsonNode bidExContent) {
    	
//    	log.info("bid :" +bidExContent);
		
    	JsonNode envConfig = bidExContent.get("Environment");
        
        List<String> envDetails=Arrays.asList("ip2ct","app","ip2sc","ip2c","ip");
    
//        log.info("Environment " + envConfig.toString());

        Map<String , Object> feature =new HashMap<>();
        
        for(String env : envDetails)
        {
        	JsonNode envResult=envConfig.get(env);
   
        	feature.put(getEnvironmentName(env), envResult);
        
        }
        
       return feature;

	}

    
    
    

	public SlotsData getSlotById(String cridIn) {

        if (slotsData.size() > 0) {
            for (SlotsData slot: slotsData) {
                if (slot.crid.equals(cridIn)) {
//                    log.info("Slots data crid " + slotsData);
                    
                    return slot;
                   
                }
            }
        }

        slotsData.add(SlotsData.builder().crid(cridIn).build());
//        log.info("Slots data crid " + slotsData);
        
        

        for (SlotsData slot: slotsData) {
            if (slot.crid.equals(cridIn)) {
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
            SlotsData data=getSlotById(cridList.toString());
            data.setAdunitPath(adUnitPathTrimmed);
            data.setFeatureName("DfpPlacements");
            

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
                 log.info("cridNode " + cridNode.toString());

                if (cridNode != null & cridNode.isObject()) {
                    try {
                        getDcn = cridNode.get("25014").get("pp").get("dcn");
                        log.info("Crid " + cridKey + " " + "DCN" + getDcn.toString());

                    } catch (Exception e) {
                        // TODO: handle exception
                        log.error("DCN does not exist for crid " + cridKey);
                    	
                    }
                    try {
                        if (getDcn.toString() != null) {
                            SlotsData data=getSlotById(cridKey);
                            data.setDcn(getDcn.toString());
                            data.setFeatureName("PlacementProviderRules");
                            
                            
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                        log.error("DCN build failed");
                    }
                    
                    
                    
                }
            });
        }
        
    }




    // This is to get intream video ad units. In bidex config we get instream video ad units separatley in the key "InStreamVideoDfpPlacements". 
    // This can be merged in the above method setCridAdunitPathMapping.

    private void getInstreamDfpPlacements(JsonNode bidExContent) {
        // TODO Auto-generated method stub
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
            SlotsData data=getSlotById(cridList.toString());
            data.setAdunitPath(adUnitPathTrimmed);
            data.setFeatureName("InStreamVideoDfpPlacements");
           
            
            

        }        
        
    }
    
    
    private BidexFeatureReport getFeaturesData(JsonNode bidexContent,List<String> listOfFeatures ,TestCriteria yaml){
//        List < String > features = new ArrayList < String > ();
//        //add multiple features to the list
//        
//        Collections.addAll(features, "CridSizeMapForMultiAd","DfpPlacements","PlacementProviderRules","InStreamVideoDfpPlacements", "InsertSlotInLargeSlot", "MultiBidTemplateConfig", "fpd", "cwrs", "MultiAdSzComb", "osrm");
  	
    	
    	//DefaultTargets.--> cc,Ugd
    	//env deatail above ;FloorPrice
    	
    	// report if feature pass then return boolean
    	

    	String harFile=browserUtil.getFileNameForHar();
    
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
        	solts.put(data.getCrid(), data);
        }
        
        
        BidexFeatureReport bidexFeatureReport=BidexFeatureReport.builder().testMetaDataBidex(TestMetaDataBidex.builder().dateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
        		.testName("Bidex And Feature Report ").environment(yaml.getEnvironment()).publisherUrl(yaml.getPublisherUrl()).harFileDownloadLink(harFile).userAgent(yaml.getUserAgent()).build()).build();
        
        
        bidexFeatureReport.setAllFeatureData(featureResult);
        bidexFeatureReport.setBusinessFeaturesDetected(featureTestResult);
        bidexFeatureReport.setEnvironmentDetails(getEnvironment(bidexContent));
        bidexFeatureReport.setSlotsDetected(solts);
        
//        //Getting AB test Config Data
//        JsonNode abTestConifgData;
////for each ab config get the ad unit, id, name and start date
//        abTestConifgData = bidexContent.get("AbtestRawConfig").get("adUnits");
//        try {
//            JsonNode rootNode = mapper.readTree(abTestConifgData.toString());
//            rootNode.fields().forEachRemaining(entry -> {
//                String adUnit = entry.getKey();
//                JsonNode adUnitNode = entry.getValue();
//                JsonNode rootValueNode = adUnitNode.get(0);
//                JsonNode name = rootValueNode.get("name");
//                JsonNode id = rootValueNode.get("id");
//                JsonNode startDate = rootValueNode.get("startDate");
//                JsonNode endDate = rootValueNode.get("endDate");
////                log.info("AdUnit " + adUnit + " " + "ID " + id + " " + "Experiment Name " + name + " " + "Start Date " + startDate + " " + "End Date " + endDate);
//
//                String abTest="AdUnit " + adUnit + " " + "ID " + id + " " + "Experiment Name " + name + " " + "Start Date " + startDate + " " + "End Date " + endDate;
//
//                
//                map.put("Abtesting", abTest);
//
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        log.info("List of Features to check " + features);

        
        return bidexFeatureReport;
        

    }

   

    @Getter @Setter @Builder @ToString
    public static class SlotsData {

    	public String featureName;
        public String crid;
        public String dcn;
        public String adunitPath;

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

        public List < String > getSizes() {
            List < String > sizes = new ArrayList < String > ();
            for (ProvidersData provider: providersData) {
                for (ProvidersData.Sizes size: provider.sizes) {
                    sizes.add(size.sz);
                }
            }
            return sizes;
        }

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