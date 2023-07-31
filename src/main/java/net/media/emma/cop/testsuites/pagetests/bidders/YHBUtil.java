package net.media.emma.cop.testsuites.pagetests.bidders;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import java.util.HashSet;
import java.util.Set;

@SuperBuilder @Slf4j
public class YHBUtil extends AbstractBidder {

   private Set < String > sizes;
   String adUnitPath;

   private Set < String > getSizeYHB() {

      if (sizes == null) {

         sizes = new HashSet < > ();

         JsonNode listOfSizes = yhbPostData.at("/imp/0/banner");
         for (JsonNode getWandH: listOfSizes) {
            if (getWandH.get("w") != null && getWandH.get("h") != null) {
               JsonNode widthDirectOutside = getWandH.get("w");
               JsonNode heightDirectOutside = getWandH.get("h");
               sizes.add(widthDirectOutside.toString() + "x" + heightDirectOutside.toString());
            }
         }

         // if(!listOfSizes.isMissingNode()) {
         //     for(JsonNode child : listOfSizes){
         //         log.info("YHB call data " + yhbPostData.toPrettyString());
         //         log.info("YHB call Data at imp" + listOfSizes.toPrettyString());
         //         log.info("Child is " + child.toPrettyString());
         //         if(child.get("w")!=null || child.has("w")){
         //             log.info("W mil gaya");
         //             sizes.add(child.get("w").toString());
         //         }
         //         if(child.get("h")!=null || child.has("h")){
         //             log.info("H mil gaya");
         //             sizes.add(child.get("h").toString());
         //         }

         //     }

         // }
      }

      return sizes;
   }

   @Override
   public String getAdUnitPath() {

      if (this.adUnitPath == null) {

         JsonNode adUnitPath = getYhbPostData().at("/imp/0/ext/dfp_ad_unit_code");
        
         if (!adUnitPath.isMissingNode()) {
            log.info("Adunit Path found: " + adUnitPath.asText());
            this.adUnitPath = adUnitPath.asText();
         } else {
            this.adUnitPath = "";
         }
      }

      return this.adUnitPath;
   }

   @Override
   public Set < String > getSizes() {
      if (null == sizes) {
         sizes = getSizeYHB();
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