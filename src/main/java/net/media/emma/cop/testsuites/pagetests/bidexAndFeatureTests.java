package net.media.emma.cop.testsuites.pagetests;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.Page;


import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BidExConfig;
import net.media.emma.cop.components.BrowserFactory;

import net.media.emma.utils.URLDataProcessing;



@Service
@Slf4j
public class bidexAndFeatureTests {

	BidExConfig bidExConfig = BidExConfig.builder().build();

	String finalConfig = "";
	String bidexResp = "";
	ObjectMapper mapper = new ObjectMapper(); 
	JsonNode replaced;

    
	@Autowired
	BrowserFactory browserFactory;
	@Autowired
	URLDataProcessing urlDataProcessing;

	@Autowired
	ObjectWriter jsonWriter;

	@Autowired
	ObjectMapper jsonReader;

	@Value("${harFileBasePath}")
	private String harFileBasePath;
	
	@Value("#{'${listOfFeature}'.split(',')}")
    private List<String> listOfFeatures;
	
	
	

    public BidexFeatureReport bidexTests(TestCriteria yaml) throws JsonMappingException, JsonProcessingException{

		// bidExConfig.getSlotById("164457376").getProvidersData().add(
		// 						ProvidersData.builder()
		// 							.pvid(123123)
		// 							.ecc("asdf")
		// 							.sizes(Arrays.asList( Sizes.builder().epc("asdfa").sz("300x250").mediaType("0").build()))
		// 							.build());

		// bidExConfig.getSlotById("164457376").getProvidersData().get(0).getSizes()
		// bidExConfig.getSlotById("asdf").getProviderById(233).getEcc();
		// bidExConfig.getSlotById("asdf").getSizes();

		// bidExConfig.getSlotById("164457376").getSizes();


		// finalConfig = finalConfig.replaceAll("\""	, "")

		
		Page page = BrowserFactory.getPageWithNewContext(yaml.getUserAgent(), null, harFileBasePath);
		try {
		page.onResponse(response -> {
			if(response.url().contains("https://hb.yahoo.net/bidexchange.js?cid")){
			log.info("Response Status: " + response.status() + ", Response Headers: " + response.headers());
			// log.info("Response Body: " + response.text());	
			
			try {
				
				if(response.status()==200){
				bidexResp = response.text();
				
				
				
				}

				// log.info("Bidex Response: " + bidexJsonNode.toPrettyString());
			} catch (Exception e) {
				log.info("Response Json failed");
				// log.info("Json Node value " + bidexJsonNode.toString());

			}
			
			}
        });
 
        page.navigate(yaml.getPublisherUrl());
	
		} catch (Exception e) {
			log.info("Bidex not found");
		}


		String startTarget = "this.configRegistry.loadConfig";
		String endTarget = "})}abtest(e,t)";
		int startIndex=0;
		int endIndex=0;

//		if(bidexResp.contains(startTarget)){
//			 startIndex  = bidexResp.indexOf(startTarget);
//			startIndex+=12;
//			log.info("Start Index : " + startIndex);
//		}
//		if(bidexResp.contains(endTarget)){
//			endIndex  = bidexResp.indexOf(endTarget);
//			endIndex+=3;
//			log.info("End Index : " + endIndex);
//		}
		
		startIndex=bidexResp.indexOf(startTarget);
		endIndex=bidexResp.indexOf(endTarget);
		
		

		//control in Bidexconfig.java
		finalConfig = bidexResp.substring(startIndex, endIndex);
//		log.info("finalString = "+ finalConfig);
		
//		System.err.println(finalConfig);
		

		try {
		 replaced = mapper.readTree(finalConfig.toString());
//				 log.info("Json :"+ replaced);
		 
		 

		} catch (Exception e) {
//			log.info("Converting to json error");
		}
		
		page.close();
		return bidExConfig.loadBidExConfig(replaced,listOfFeatures);
    }




	// ----------------OLD LOGIC--------------

	// private Map<String,String> getCridsByXpath(JsonNode replaced){
	// 	Map<String, String> cridToDfpPath = new HashMap<String, String>();
	// 	List<String> crids = new ArrayList<String>();
	// 	Map<String, String> getDcn;
		
	// 	JsonNode parent = replaced.get("DfpPlacements");
	// 	try {
    //         JsonNode rootNode = mapper.readTree(parent.toString());
    //         // log.info("rootNode: " + rootNode);
    //         rootNode.fields().forEachRemaining(entry -> {
    //             crids.add(entry.getKey());
    //             // log.info("Object ID: " + crids);
	// 			// log.info("Printed crids and dfp outside");
    //         });
			
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
	// 	for (String cridList : crids) {
	// 			// String id = cridList;
	// 			JsonNode cridNode = parent.get(cridList).get("targ").get(0).get("value");
	// 			String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
	// 			cridToDfpPath.put(cridList, adUnitPathTrimmed);
	// 			// log.info("Crid is " + id + " and Dfp Path is" + cridNode);
	// 		}
	// 		log.info("Crid and DfpPath outside " + cridToDfpPath);

	// 		getDcn = getDcnForCrid(replaced, crids);


	// 	return cridToDfpPath;
	// }



	// private Map<String, String> getDcnForCrid(JsonNode replaced, List<String> crids){
	// 	Map<String, String> dcnToCridMap = new HashMap<String, String>();
				
	// 			try {
		
	// 				JsonNode bidderParamsConfigNode = replaced.get("BidderParamsConfig");
	// 				if (bidderParamsConfigNode != null && bidderParamsConfigNode.isObject()) {

	// 					bidderParamsConfigNode.fields().forEachRemaining(entry -> {
	// 						String fieldKey = entry.getKey();
	// 						JsonNode fieldNode = entry.getValue();
		
	// 						JsonNode placementParamsNode = fieldNode.get("placementParams");
	// 						if (placementParamsNode != null && placementParamsNode.isObject()) {

	// 							placementParamsNode.fields().forEachRemaining(ppEntry -> {
	// 								String dcn = ppEntry.getKey();

	// 								String dcnVal = placementParamsNode.get(dcn).get("dcn").toString();
	// 								dcnToCridMap.put(dcn, dcnVal);

									
	// 							});
	// 						}
	// 					});
	// 				}
	// 			} catch (Exception e) {
	// 				e.printStackTrace();
				
	// 			// log.info("Crid is " + id + " and Dfp Path is" + cridNode);
				

	// 		}
	// 		log.info("DCN mapped for crids outside" + dcnToCridMap);

	// 		return dcnToCridMap;
	// }
}
