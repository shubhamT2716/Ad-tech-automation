package net.media.emma.cop.testsuites.pagetests.bidders;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

@SuperBuilder @Slf4j
public class YHBUtil  extends AbstractBidder {

        private Set<String> sizes;
        String adUnitPath;

        private Set<String> getSizeYHB(){

            if(sizes == null){

                sizes = new HashSet<>();

                JsonNode listOfSizes = yhbPostData.at("/imp/0/banner/0");

                if(!listOfSizes.isMissingNode()) {

                    for (JsonNode jsonNode : listOfSizes) {

                        if(listOfSizes !=null ){
                            sizes.add(jsonNode.get("w").asText() + "x" + jsonNode.get("h").asText());
                            log.info("sizes added: " + sizes);
                            log.info("List of Sizes " + listOfSizes.toString());
                        }

                    }
                }
            }

            return sizes;
        }

        // @Override
        // public String getAdUnitPath() {
            
        //     if(this.adUnitPath == null){

        //        JsonNode adUnitPath = getYhbPostData().at("/imp/0/ext/dfp_ad_unit_code");

        //        if(!adUnitPath.isMissingNode()) {
        //            log.info("Adunit Path found: " + adUnitPath.asText());
        //            this.adUnitPath = adUnitPath.asText();
        //         }else{
        //              this.adUnitPath = "";
        //         }
        //     }

        //     return this.adUnitPath;
        // }


        @Override
        public Set<String> getSizes() {
           if(null == sizes){
            sizes = getSizeYHB();
           }

           return sizes;
        }

        @Override
        public String getAdUnitPath() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'getAdUnitPath'");
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


