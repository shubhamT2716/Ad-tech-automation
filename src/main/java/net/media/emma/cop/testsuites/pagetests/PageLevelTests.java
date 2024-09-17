package net.media.emma.cop.testsuites.pagetests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;
import net.media.emma.cop.testsuites.pagetests.bidders.AbstractBidder;
import net.media.emma.cop.testsuites.pagetests.bidders.YHBUtil;
import net.media.emma.cop.testsuites.pagetests.bidders.YPBJSUtil;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.URLDataProcessing;

/*  ***************************************************************************************************
    // Open the URL using Playwright Page - Done
    // Scrape Content (as your doing right now) - Done
    // capture all network calls
    // find out bidder calls (keep a list of known bidder URL formats and filter
    // network calls against them) - In progress
    // bidder calls request data (QA Params / Request Body) should have expeted
    // values only. - In Progress
    // ???
    // build a map<string,string> bidder key-val data from the URL


 ********************************************************************************************* */

@Service
@Slf4j
public class PageLevelTests {

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

	List<String> jsExtraction = new ArrayList<>();
	// List of urls to scrape the data from, basically bidder calls
	HashMap<String, String> knownBidderCallPatterns = new HashMap<>() {
		{
//			put("YPBJS", "https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS");
//			put("RTBSCall", "https://xch.media.net/AdExchange/rtbsspub?");
			put("RTBSCall", "\n"
					+ "https://xch.media.net/AdExchange/rtbsspub?" );
//			put("YHBcall", "https://m.hb.yahoo.net/rtb/yhb?cid");
		}
	};

	// Map<String, AbstractBidder> bidderDataUtils = new HashMap<>(){
	// {
	// put("YPBJS",
	// YPBJSUtil.builder().bidderCallFormat("https://c2shb-oao.ssp.yahoo.com/admax/bid/partners/YPBJS")
	// .build());
	// put("YHBcall", YHBUtil.builder().build());
	// }
	// };

	public JsonNode getPageLevelTestsData(TestCriteria yaml) {

		String harFilePath = CommonBrowserUtil.getFileNameForHar();

		Page page = BrowserFactory.getPageWithNewContext(yaml.getUserAgent(), yaml.getCoockies(),
				harFileBasePath + harFilePath);

		// list of arrays initialization
		List<Request> urlsFromNetworkTab = new ArrayList<>();

		page.onRequest(request -> {

			urlsFromNetworkTab.add(request);
		});
		page.setDefaultNavigationTimeout(15000);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Slots> slotsData = extractSlotsDataFromPage2(page, yaml.getPublisherUrl());
		System.err.println("slots:  "+slotsData);
		
		
		page.close();
		page.context().close();

		Map<String, List<? extends AbstractBidder>> bidderData = new HashMap<>();

		// knownBidderCallPatterns.forEach((bidderName, urlPattern) -> {
		// bidderData.putAll(getBidderUtils(urlsFromNetworkTab, bidderName));
		// });

		// iterate through all the known bidder call patterns and get the data
		for (Map.Entry<String, String> bidderCallPattern : knownBidderCallPatterns.entrySet()) {

			List<? extends AbstractBidder> bidderDataasdfs = getBidderUtils(urlsFromNetworkTab,
					bidderCallPattern.getKey());

			log.info("Bidders List for " + bidderCallPattern.getKey() + " : " + bidderDataasdfs);

			if (bidderDataasdfs.size() > 0) {
				bidderData.put(bidderCallPattern.getKey(), bidderDataasdfs);
			}
		}

		log.info("All known bidders size:" + bidderData.size(), bidderData);

		TestReportV2 testReport = TestReportV2.builder()
				.testMetaData(TestReportV2.TestMetaData.builder().testName("Pagelevel Test Suite")
						.environment(yaml.getEnvironment()).publisherUrl(yaml.getPublisherUrl())
						.userAgent(yaml.getUserAgent()).harFileDownloadLink("harDownload/" + harFilePath).build())
				.build();

		
		if(slotsData!=null)
		{
			
			for (Slots slotData : slotsData) {

				TestReportV2.Slot slotReport = TestReportV2.Slot.builder().slotName(slotData.getPath()).build();

				for (Map.Entry<String, List<? extends AbstractBidder>> bidderList : bidderData.entrySet()) {

					log.info("Bidder Name: " + bidderList.getKey() + " Bidder Data: " + bidderList.getValue());

					TestReportV2.Bidder bidderReport = TestReportV2.Bidder.builder().bidderName(bidderList.getKey())
							.build();

					AbstractBidder bidder = getMatchingDataByAdUnit(slotData, bidderList.getValue());

					if (null == bidder) {
						bidderReport.getTestMessages().add("Bidder Call not found");
						slotReport.getBidders().add(bidderReport);
						continue;
					}

	                
					AbstractBidder bidderSizes = getMatchingDataByAdUnitYHB(slotData, bidderList.getValue());

					if(bidderSizes == null){
	                    log.info("Bidder size = " + bidder.getSizes());
						bidderReport.getTestMessages().add("Slot Sizes " + slotData.getSizes()
								+ " mismatched with bidder sizes " + bidder.getSizes() + "");
	                } else {
						bidderReport.getTestMessages().add("Sizes matched " + slotData.getSizes() + "");
					}

					slotReport.getBidders().add(bidderReport);
				}

				testReport.getSlots().add(slotReport);
			}
		}
		
		

		testReport.getTestRawData().put("Slots Raw Data", jsonReader.valueToTree(slotsData));
		testReport.getTestRawData().put("Bidder Calls Raw Data", jsonReader.valueToTree(bidderData));

		try {
			return jsonReader.readTree(jsonWriter.writeValueAsString(testReport));
		} catch (JsonProcessingException e) {
			log.error("Test Report Object marshalling failed", e);
			// Return JsonNode with error message
			return jsonReader.createObjectNode().put("error", "Test Report Object marshalling failed");
		}
	}

	private List<String> filterRequestsByTypeAndReturnPostData(String urlPattern, List<Request> urlsFromNetworkTab) {

		List<String> postData = new ArrayList<>();

		for (Request request : urlsFromNetworkTab) {

			if (request.url().startsWith(urlPattern)) {

				// post data of all ypbjs calls stored
				try {
					postData.add(request.postData());
				} catch (PlaywrightException e) {
					log.error("Error while getting post data from request: " + request.url());
				}
			}
		}

		return postData;
	}

	// private List<String> returnYhbPostData(String urlPattern, List<Request>
	// urlsFromNetworkTab) {
	// List<String> yhbPostData = new ArrayList<>();
	// for (Request request : urlsFromNetworkTab) {
	// if (request.url().startsWith(urlPattern)) {
	// try {
	// yhbPostData.add(request.postData());
	// log.info("YHB post Data :" + yhbPostData);
	// } catch (PlaywrightException e) {
	// log.error("couldnt get YHB post Data " + request.url());
	// }
	// }
	// }
	// return yhbPostData;
	// }

	private AbstractBidder getMatchingDataByAdUnit(Slots slotData, List<? extends AbstractBidder> bidderCallData) {
        

		for (AbstractBidder bidderUtil : bidderCallData) {

			log.info(
					"Slot Ad Unit Path: " + slotData.getPath() + " Bidder Ad Unit Path: " + bidderUtil.getAdUnitPath());
                    if(bidderUtil.getAdUnitPath() == null) {
                        return bidderUtil;
                    }

			if (slotData.getAdUnitPath().contains(bidderUtil.getAdUnitPath())) {

				return bidderUtil;
			}
		}

		return null;
	}
private AbstractBidder getMatchingDataByAdUnitYHB(Slots slotData, List<? extends AbstractBidder> bidderCallData) {
    


		for (AbstractBidder bidderUtil : bidderCallData) {

            if(slotData.getAdUnitPath().contains(bidderUtil.getAdUnitPath())) {
			log.info(
					"Slot Sizes: " + slotData.getSizes() + " Bidder Sizes: " + bidderUtil.getSizes());

			if (slotData.getSizes().equals(bidderUtil.getSizes())) {

				return bidderUtil;
			}
		}
    }

		return null;
	}
	// private YHBUtil getSizesYHB(Slots slotData, List<YHBUtil> yhbCalls) {
	// for (YHBUtil yhbData : yhbCalls) {
	// if (slotData.getSizes().equals(yhbData.getSizes())) {
	// log.info("Inside IF, sizes matched " + slotData.getSizes().toString() + " and
	// "
	// + yhbData.getSizes().toStxring());
	// return yhbData;
	// }
	// }
	// return null;
	// }

	private List<? extends AbstractBidder> getBidderUtils(List<Request> urlsFromNetworkTab, String bidderName) {

		List<? extends AbstractBidder> ypbjsCallData = filterRequestsByTypeAndReturnPostData(
				knownBidderCallPatterns.get(bidderName), urlsFromNetworkTab).stream().map(data -> {
					try {

						switch (bidderName) {
						case "YPBJS":
							return YPBJSUtil.builder().bidderCallFormat(knownBidderCallPatterns.get(bidderName))
									.yhbPostData(jsonReader.readValue(data, JsonNode.class)).build();
						case "YHBcall":
							return YHBUtil.builder().bidderCallFormat(knownBidderCallPatterns.get(bidderName))
									.yhbPostData(jsonReader.readValue(data, JsonNode.class)).build();
						}

						// JsonNode asdfas = jsonReader.readValue(data, JsonNode.class);
						// AbstractBidder bidder = bidderDataUtils.get(bidderName).

						// bidderDataUtils.get(bidderName).setYhbPostData(asdfas);
						// return bidderDataUtils.get(bidderName);

					} catch (JsonProcessingException e) {
						log.error("Error while converting YPBJS call data to JsonNode: " + data);
						return null;
					}
					return null;
				}).filter(data -> data != null).collect(Collectors.toList());

		return ypbjsCallData;
	}

//	private TempClass verifyYPBJSCallData(List<Slots> slotsData, List<Request> urlsFromNetworkTab) {
//
//		List<TestData> listOfSlots = new ArrayList<>();
//
//		TempClass result = new TempClass();
//
//		List<YPBJSUtil> ypbjsCallData = filterRequestsByTypeAndReturnPostData(knownBidderCallPatterns.get("YPBJS"),
//				urlsFromNetworkTab).stream().map(data -> {
//					try {
//
//						return YPBJSUtil.builder().ypbjsPostData(jsonReader.readValue(data, JsonNode.class)).build();
//
//					} catch (JsonProcessingException e) {
//						log.error("Error while converting YPBJS call data to JsonNode: " + data);
//						return null;
//					}
//				}).filter(data -> data != null).collect(Collectors.toList());
//
//		log.info("count of ypbjs calls: " + ypbjsCallData.size());
//
//		for (int i = 0; i < slotsData.size(); i++) {
//
//			YPBJSUtil ypbjsdata = getMatchingDataByAdUnit(slotsData.get(i), ypbjsCallData);
//
//			if (ypbjsdata != null) {
//
//				if (ypbjsdata.getSizes().equals(slotsData.get(i).getSetOfSizes())) {
//					log.info("Sizes matched for slot: " + slotsData.get(i).getSlothPath());
//					// test case pass
//					// <Result, Message>
//
//					result.setStatus(TestStatus.PASS);
//					result.setMessage("Size matched");
//
//					// map.put(ypbjsdata, );
//
//				} else {
//					log.info("Sizes did not match for slot: " + slotsData.get(i).getSlothPath());
//					// map.put("FAIL", "Sizes did not match for slot: " + slot.getSlothPath());
//					// test case fail
//
//					result.setStatus(TestStatus.FAIL);
//					result.setMessage("Size mismatched Expactesd Size = " + slotsData.get(i).getSizes() + " "
//							+ " found  " + ypbjsdata.getSizes());
//
//					// listOfSlots.add(result);
//				}
//
//			} else {
//				log.info("Slots Data with ad unit path: " + slotsData.get(i).getSlothPath()
//						+ " not found in YPBJS call data");
//				// test case fail
////				map.put("FAIL",
////						"Slots Data with ad unit path: " + slot.getSlothPath() + " not found in YPBJS call data");
//
//				result.setStatus(TestStatus.FAIL);
//
//			}
//
//			// listOfSlots.add(result);
//		}
//
//		return result;
//	}

	// private String getYHBCallData(List<Slots> slotsData, List<Request>
	// urlsFromNetworkTab) {

	// List<YHBUtil> yhbCallData =
	// returnYhbPostData(knownBidderCallPatterns.get("YHBcall"), urlsFromNetworkTab)
	// .stream().map(data -> {
	// try {
	// return YHBUtil.builder().yhbPostData(jsonReader.readValue(data,
	// JsonNode.class)).build();
	// } catch (JsonProcessingException e) {
	// log.error("Error while converting YHB call data to JsonNode: " + data);
	// return null;
	// }
	// }).filter(data -> data != null).collect(Collectors.toList());

	// log.info("count of YHB calls: " + yhbCallData.size());

	// System.err.println("Yhb Data: " + yhbCallData.get(0).getSizes());

	// for (Slots slot : slotsData) {

	// YHBUtil yhbDataMatchResult = getSizesYHB(slot, yhbCallData);
	// if (yhbDataMatchResult != null) {
	// log.info("Reached Here");
	// } else {
	// log.info("Slots Data with ad unit path: " + slot.getSlothPath() + " not found
	// in YHB call data");
	// }

	// }

	// return "YHB Utils Data";
	// }

	private List<Slots> extractSlotsDataFromPage2(Page page, String url) {

		try {

//			page.onConsoleMessage(consoleMessage -> {
//				jsExtraction.add("\n" + consoleMessage.text() + "\n");
//			});

			page.navigate(url);
			page.waitForTimeout(12000);

			Object adUnitDataEval = page.evaluate(adUnitDataCheckV2());
			System.err.println("Slots: "+adUnitDataEval);
			
			log.info("Extracted json data from the page: " + adUnitDataEval.toString());
//        SlotsDataUtil slotsDataUtil = jsonReader.readValue(adUnitDataEval.toString(), SlotsDataUtil.class);
			List<Slots> slotsData = jsonReader.readValue(adUnitDataEval.toString(), new TypeReference<List<Slots>>() {
			});
			// log.info("JSON Node as pretty print: " +
			// jsonReader.writerWithDefaultPrettyPrinter().writeValueAsString(slotsData));
//        return SlotsDataUtil.builder().slots(slotsData).build();
			return slotsData;

		} catch (Exception e) {
			log.error("Error in scraping content", e);
		}

		return null;
	}

	private JsonNode extractSlotsDataFromPage(Page page, String url) {

		try {

			page.onConsoleMessage(consoleMessage -> {
				jsExtraction.add("\n" + consoleMessage.text() + "\n");
			});

			page.navigate(url);
			page.waitForTimeout(500);

			try {
				JsonNode slotsData;
				Object adUnitDataEval = page.evaluate(adUnitDataCheckV2());
				
				log.info("Extracted json data from the page: " + adUnitDataEval.toString());
				slotsData = jsonReader.readTree(adUnitDataEval.toString());
				// log.info("JSON Node as pretty print: " +
				// jsonReader.writerWithDefaultPrettyPrinter().writeValueAsString(slotsData));
				return slotsData;

			} catch (JsonProcessingException e) {
				log.error("Json Parsing error", e);
			}

		} catch (PlaywrightException e) {
			log.error("Error in scraping content", e);
		}

		return null;
	}

	private String pbjsVarExtrationLogic() {
		return """
				if(window._pbjsGlobals){
				var pbjsVar = window._pbjsGlobals[0];
				console.log(JSON.stringify(window[pbjsVar], null , 4));
				}
				    """;
	}

	// Scraping ad unit detials
	private String adUnitExtractionLogic() {
		return """
				    if(googletag.pubads()){
				        console.log('List of Ad units and Targeting: ');
				    googletag.pubads().getSlots().map(s => {
				        console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
				    });
				}""";
	}

	private String adUnitDataCheck() {
		return """
				var jsonObj = [];
				// function adUnitJs(){
				//     return JSON.stringify(jsonObj);
				// }

				       if(googletag.pubads()){
				        googletag.pubads().getSlots().map(s => {
				        jsonObj.push(
				                new Map([
				                        ["adunitPath", s.getAdUnitPath()], ["sizes", s.getSizes()], ["targetting", s.getTargetingMap()]
				                        ])
				            )
				        });
				        console.log(JSON.stringify(jsonObj));
				        //console.dir(jsonObj)
				    }
				    //adUnitJs();
				    // console.dir(jsonObj);
				    """;
	}

	private String adUnitDataCheckV2() {
		return """
				class AdUnitsData {
				    constructor(path, sizes, targetting) {
				        this.path = path;
				        this.sizes = sizes;
				      this.targetting = targetting;
				    }
				  }

				slotsData = [];

				function adUnitJs(){
				         return JSON.stringify(slotsData);
				     }

				if(googletag.pubads()){
				    googletag.pubads().getSlots().map(s => {

				        // console.log(s.getAdUnitPath(), JSON.stringify(s.getSizes(), null, 4), JSON.stringify(s.getTargetingMap(), null, 4));
				        slotsData.push( new AdUnitsData(s.getAdUnitPath(), s.getSizes(), s.getTargetingMap()));

				    });

				}

				//console.log(JSON.stringify(slotsData, null, 4));

				adUnitJs();
				""";
	}
}