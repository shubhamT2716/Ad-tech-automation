package net.media.emma.cop.testsuites.pagetests.bidders;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@SuperBuilder @Slf4j
public class YPBJSUtil extends AbstractBidder {

    private Set<String> sizes;
    private String adUnitPath;

    public Set<String> getSizes(){

        if(sizes == null){

            sizes = new HashSet<>();

            JsonNode listOfSizes = getYhbPostData().at("/imp/0/banner/format");

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

            JsonNode adUnitPath = getYhbPostData().at("/imp/0/ext/dfp_ad_unit_code");

            if(!adUnitPath.isMissingNode()) {
                log.info("Adunit Path found: " + adUnitPath.asText());
                this.adUnitPath = adUnitPath.asText();
            }else{
                this.adUnitPath = "";
                log.info("Adunit Path json path not found");
                log.info(getYhbPostData().toPrettyString());
            }
        }

        return adUnitPath;
    }
}
