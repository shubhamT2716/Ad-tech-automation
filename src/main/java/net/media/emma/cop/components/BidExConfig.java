package net.media.emma.cop.components;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.utils.FileOperations;
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

    //Data format
    // cridDetails = { "crid" : { "dcn" : "dcnValue", "adunitPath" : "adunitPathValue" } }
    // cridDetails = { "164457376" : { "dcn" : "8a969d570187874cae488063d27b00ab", "adunitPath" : "/22888152279/us/ysports/main/dt/us_ysports_main_dt_mid_right" } }
    // private Map<String, Map<String, String>> cridDetails = new HashMap<String, Map<String, String>>();

    public void loadBidExConfig(JsonNode bidExContent) {

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

        // loadbidExContent(bidExContent);
        setCridAdunitPathMapping(bidExContent);
        setDcnIntoCrid(bidExContent);
        getInstreamDfpPlacements(bidExContent);
        getFeaturesData(bidExContent);
    }


    public SlotsData getSlotById(String cridIn) {

        if (slotsData.size() > 0) {
            for (SlotsData slot: slotsData) {
                if (slot.crid.equals(cridIn)) {
                    log.info("Slots data crid " + slotsData);
                    return slot;
                }
            }
        }

        slotsData.add(SlotsData.builder().crid(cridIn).build());
        log.info("Slots data crid " + slotsData);

        for (SlotsData slot: slotsData) {
            if (slot.crid.equals(cridIn)) {
                return slot;
            }
        }

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
            getSlotById(cridList.toString()).setAdunitPath(adUnitPathTrimmed);

        }
    }

    public void setDcnIntoCrid(JsonNode bidExContent) {
        //get dcn mapping from sample bidex config
        JsonNode dcnConfig = bidExContent.get("PlacementProviderRules");
        if (dcnConfig != null && dcnConfig.isObject()) {
            dcnConfig.fields().forEachRemaining(entry -> {
                String cridKey = entry.getKey();
                JsonNode cridNode = entry.getValue();
                JsonNode getDcn = null;
                // log.info("cridNode " + cridNode.toString());

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
                            getSlotById(cridKey).setDcn(getDcn.toString());
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
            getSlotById(cridList.toString()).setAdunitPath(adUnitPathTrimmed);

        }        
        
    }
    private void getFeaturesData(JsonNode bidexContent){
        List < String > features = new ArrayList < String > ();
        //add multiple features to the list
        Collections.addAll(features, "CridSizeMapForMultiAd", "fpd");

        Map<String , Map<String, Object>> map=new HashMap<>();

        for(String feature : features)
        {
            ObjectMapper mapper = new ObjectMapper();

        	JsonNode basedOnFeature = bidexContent.get(feature);

            Map<String, Object> featureMap=mapper.convertValue(basedOnFeature, new  TypeReference<Map<String, Object>>() {});

           map.put(feature, featureMap);

            System.err.println(featureMap);

            System.out.println("Feature data : "+map);

//        	basedOnFeature.fields().forEachRemaining(entry ->{
//        		String key=entry.getKey();
//        		
//        		System.err.println(key);
//        		JsonNode value=entry.getValue();
//        		map.put(key, value);
//        	});


        	
        }
        
        
        
        
        System.err.println("Feature data : "+map);
        
        //Getting AB test Config Data
        JsonNode abTestConifgData;
//for each ab config get the ad unit, id, name and start date
        abTestConifgData = bidexContent.get("AbtestRawConfig").get("adUnits");
        try {
            JsonNode rootNode = mapper.readTree(abTestConifgData.toString());
            rootNode.fields().forEachRemaining(entry -> {
                String adUnit = entry.getKey();
                JsonNode adUnitNode = entry.getValue();
                JsonNode rootValueNode = adUnitNode.get(0);
                JsonNode name = rootValueNode.get("name");
                JsonNode id = rootValueNode.get("id");
                JsonNode startDate = rootValueNode.get("startDate");
                JsonNode endDate = rootValueNode.get("endDate");
                log.info("AdUnit " + adUnit + " " + "ID " + id + " " + "Experiment Name " + name + " " + "Start Date " + startDate + " " + "End Date " + endDate);


            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("List of Features to check " + features);



    }

    public void setSizes() {

    }

    @Getter @Setter @Builder @ToString
    public static class SlotsData {

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