package net.media.emma.cop.testsuites.pagetests;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;

@Builder
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
                this.adUnitPath = adUnitPath.asText();
            }else{
                this.adUnitPath = "";
            }
        }

        return adUnitPath;
    }
}
