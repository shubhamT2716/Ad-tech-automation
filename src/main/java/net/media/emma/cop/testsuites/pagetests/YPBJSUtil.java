package net.media.emma.cop.testsuites.pagetests;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Builder @Slf4j
public class YPBJSUtil {

    JsonNode ypbjsPostData;
    private Set<String> sizes;
    private String adUnitPath;

    public Set<String> getSizes(){

        if(sizes == null){

            sizes = new HashSet<>();

            JsonNode listOfSizes = ypbjsPostData.at("/imp/0/banner/format");

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

    public String getAdUnitPath(){

        if(adUnitPath == null){

            JsonNode adUnitPath = ypbjsPostData.at("/imp/0/ext/dfp_ad_unit_code");

            if(!adUnitPath.isMissingNode()) {
                log.info("Adunit Path found: " + adUnitPath.asText());
                this.adUnitPath = adUnitPath.asText();
            }else{
                this.adUnitPath = "";
                log.info("Adunit Path json path not found");
                log.info(ypbjsPostData.toPrettyString());
            }
        }

        return adUnitPath;
    }
}
