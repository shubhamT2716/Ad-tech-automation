package net.media.emma.cop.components;


import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.utils.FileOperations;

@Builder @Getter @Setter @Slf4j
public class BidExConfig {

    @Autowired FileOperations fileUtil;
    @Autowired ObjectMapper jsonReader;


    private JsonNode bidExConfigAsJsonNode;
    public List<SlotsData> slotsData;
    
    //Data format
    // cridDetails = { "crid" : { "dcn" : "dcnValue", "adunitPath" : "adunitPathValue" } }
    // cridDetails = { "164457376" : { "dcn" : "8a969d570187874cae488063d27b00ab", "adunitPath" : "/22888152279/us/ysports/main/dt/us_ysports_main_dt_mid_right" } }
    // private Map<String, Map<String, String>> cridDetails = new HashMap<String, Map<String, String>>();

    public void loadBidExConfig(String bidExContent){

        if(bidExContent != null){
            bidExContent = fileUtil.readFileAsString("src/main/java/net/media/emma/cop/components/sample/bidExConfigSample.json");
        }

        loadBidExConfigAsJsonNode(bidExContent);
        setCridAdunitPathMapping();
        setDcnIntoCrid();
       
    }



    private void loadBidExConfigAsJsonNode(String bidExContent){

        //sampleBidExConfig into JsonNode
        try {
            bidExConfigAsJsonNode = jsonReader.readTree(bidExContent);
        } catch (Exception  e) {
            log.error("BidEx Configload Error", e);
        }

    }

    public SlotsData getSlotById(String crid){

        for (SlotsData slot : slotsData) {
            if(slot.crid.equals(crid)){
                return slot;
            }
        }

        slotsData.add(SlotsData.builder().crid(crid).build());
        
        for (SlotsData slot : slotsData) {
            if(slot.crid.equals(crid)){
                return slot;
            }
        }

        return null;
    
    }

    private void setDcnIntoCrid(){
        // /x.BidderParamsConfig.25014.placementParams

        JsonNode crids =  bidExConfigAsJsonNode.get("BidderParamsConfig").get("25014").get("placementParams");
        crids.fields().forEachRemaining(crid -> {
            // addElementToCridData(crid.getKey(), "dcn", crid.getValue().get("dcn").asText());
            getSlotById(crid.getKey()).setDcn(crid.getValue().get("dcn").asText());
        });
      
    }


    public void setCridAdunitPathMapping(){

        JsonNode crids = bidExConfigAsJsonNode.get("DfpPlacements");

        crids.fields().forEachRemaining((crid) -> {
            // addElementToCridData(crid.asText(), "adunitPath", crid.get("targ").get(0).get("value").asText());
            getSlotById(crid.getKey()).setAdunitPath(crid.getValue().get("targ").get(0).get("value").asText());
        });
    }

    public void setSizes(){

    }


    @Getter @Setter @Builder
    public static class SlotsData{

        public String crid;
        public String dcn;
        public String adunitPath;

        
        @Builder.Default
        public List<ProvidersData> providersData = new ArrayList<ProvidersData>();

        public ProvidersData getProviderById(int id){
            for (ProvidersData provider : providersData) {
                if(provider.pvid == id){
                    return provider;
                }
            }
            return null;
        }

        public List<String> getSizes(){
            List<String> sizes = new ArrayList<String>();
            for (ProvidersData provider : providersData) {
                for (ProvidersData.Sizes size : provider.sizes) {
                    sizes.add(size.sz);
                }
            }
            return sizes;
        }

    }

    @Getter @Setter @Builder
    public static class ProvidersData{

        int pvid;
        String  br;
        String  ecc;
        String  hb;
        String  prf;
        String  id;
        List<Sizes> sizes;

        @Getter @Setter @Builder
        public static class Sizes{
            String epc;
            String sz;
            String mediaType;
        }
       
    }


}