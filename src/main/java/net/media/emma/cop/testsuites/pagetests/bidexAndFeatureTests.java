package net.media.emma.cop.testsuites.pagetests;
import java.util.ArrayList;
import java.util.HashMap;
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
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.utils.URLDataProcessing;
import java.util.regex.Pattern;



@Service
@Slf4j
public class bidexAndFeatureTests {

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

    public JsonNode bidexTests(TestCriteria yaml) throws JsonMappingException, JsonProcessingException{

		Map<String,String> getCrids;
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


		String startTarget  =  ".loadConfig";
		String endTarget = "})}abtest(e,t)";
		int startIndex=0;
		int endIndex=0;

		if(bidexResp.contains(startTarget)){
			 startIndex  = bidexResp.indexOf(startTarget);
			startIndex+=12;
			log.info("Start Index : " + startIndex);
		}
		if(bidexResp.contains(endTarget)){
			endIndex  = bidexResp.indexOf(endTarget);
			endIndex+=3;
			log.info("End Index : " + endIndex);
		}


		finalConfig = bidexResp.substring(startIndex, endIndex);

		// finalConfig = finalConfig.replaceAll("\""	, "");

		try {
					 replaced = mapper.readTree(finalConfig.toString());
					// log.info("Json :"+ replaced);

		} catch (Exception e) {
			log.info("Converting to json error");
		}

		getCrids = getCridsByXpath(replaced);
	
		// log.info("get Crids " + getCrids);

		
		page.close();
		return replaced;
    }


	private Map<String,String> getCridsByXpath(JsonNode replaced){
		Map<String, String> cridToDfpPath = new HashMap<String, String>();
		List<String> crids = new ArrayList<String>();
		Map<String, String> getDcn;
		log.info("Here");
		JsonNode parent = replaced.get("DfpPlacements");
		try {
            JsonNode rootNode = mapper.readTree(parent.toString());
            // log.info("rootNode: " + rootNode);
            rootNode.fields().forEachRemaining(entry -> {
                crids.add(entry.getKey());
                // log.info("Object ID: " + crids);
            });
			
        } catch (Exception e) {
            e.printStackTrace();
        }
		for (String cridList : crids) {
				// String id = cridList;
				JsonNode cridNode = parent.get(cridList).get("targ").get(0).get("value");
				String adUnitPathTrimmed = cridNode.toString().substring(cridNode.toString().lastIndexOf("/") + 1).replace('"', ' ').trim();
				cridToDfpPath.put(cridList, adUnitPathTrimmed);
				// log.info("Crid is " + id + " and Dfp Path is" + cridNode);
			}
			log.info("Crid and DfpPath " + cridToDfpPath);

			getDcn = getDcnForCrid(replaced, crids);


		return cridToDfpPath;
	}



	private Map<String, String> getDcnForCrid(JsonNode replaced, List<String> crids){
		Map<String, String> dcnToCridMap = new HashMap<String, String>();
				
				try {
		
					JsonNode bidderParamsConfigNode = replaced.get("BidderParamsConfig");
					if (bidderParamsConfigNode != null && bidderParamsConfigNode.isObject()) {

						bidderParamsConfigNode.fields().forEachRemaining(entry -> {
							String fieldKey = entry.getKey();
							JsonNode fieldNode = entry.getValue();
		

							JsonNode placementParamsNode = fieldNode.get("placementParams");
							if (placementParamsNode != null && placementParamsNode.isObject()) {

								placementParamsNode.fields().forEachRemaining(ppEntry -> {
									String dcn = ppEntry.getKey();

									String dcnVal = placementParamsNode.get(dcn).get("dcn").toString();
									dcnToCridMap.put(dcn, dcnVal);

									
								});
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				
				// log.info("Crid is " + id + " and Dfp Path is" + cridNode);
				

			}
			log.info("DCN mapped for crids " + dcnToCridMap);

			return dcnToCridMap;
	}
}
