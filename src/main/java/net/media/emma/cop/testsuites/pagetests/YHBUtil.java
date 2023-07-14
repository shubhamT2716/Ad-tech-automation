package net.media.emma.cop.testsuites.pagetests;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
@Builder @Slf4j
public class YHBUtil {

        JsonNode yhbPostData;
        private Set<String> sizes;
        String adUnitPath;


        public Set<String> getSizeYHB(){

            if(sizes == null){

                sizes = new HashSet<>();

                JsonNode listOfSizes = yhbPostData.at("/imp/0/banner/0");

                if(!listOfSizes.isMissingNode()) {

                    for (JsonNode jsonNode : listOfSizes) {

                        if(jsonNode.get("w") != null && jsonNode.get("h") != null){
                            sizes.add(jsonNode.get("w").asText() + "x" + jsonNode.get("h").asText());
                        }

                    }
                }
            }

            return sizes;
        }

//    public String getSamplePath(){
//
//        if(adUnitPath == null){
//
//            JsonNode adUnitPath = yhbPostData.at("/imp/0/ext/dfp_ad_unit_code");
//
//            if(!adUnitPath.isMissingNode()) {
//                log.info("Adunit Path found: " + adUnitPath.asText());
//                this.adUnitPath = adUnitPath.asText();
//            }else{
//                this.adUnitPath = "";
//                log.info("Adunit Path json path not found");
//                log.info(yhbPostData.toPrettyString());
//            }
//        }
//
//        return adUnitPath;
//    }
    }


