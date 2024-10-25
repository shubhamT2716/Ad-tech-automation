package net.media.emma.cop.testsuites.pagetests;

import com.microsoft.playwright.Request;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.FeatureReport.FeatureChecks;
import net.media.emma.cop.testsuites.pagetests.FeatureReport.TestStatus;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.utils.URLDataProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class FeatureTestCriteria implements FeatureTestServiceIn {

    boolean test = true;
    boolean negativeKey = false;
    @Autowired
    private URLDataProcessing dataProcessing;

    @Override
    public FeatureReport featureTestCasePositvie(Map.Entry<String, String> urlData, List<Request> requests,
                                                 TestCriteriaBo criteriaBo, URLChecksReportModel reportModel, String feature) {

        log.info("urlData : " + urlData);


        Map<String, NegativeURLChecks> negativeCheck = reportModel.getNegativeUrlChecks();

        negativeCheck.entrySet().forEach(negativePoint -> {


            if (negativePoint.getValue().getCountOfMatchingUrls() > 0) {

                negativeKey = true;
            }

        });


        if (!negativeKey) {


            Map<String, PositiveURLChecks> mapReport = reportModel.getPositiveUrlChecks();

            FeatureReport report = FeatureReport.builder().build();

            Map<String, FeatureChecks> positiveReport = new HashMap<>();

            Map<String, Map<String, FeatureChecks>> repo = new HashMap<>();

            Set<String> message = new HashSet<>();

            List<String> matchingUrl = new ArrayList<>();
//		String s=urlData.getKey();

            String v1 = urlData.getValue();

//		Map<String, String> urlQuesryStringMap=dataProcessing.getQueryStringMapFromUrl(v1);

            criteriaBo.getFeatureKeysNegative().entrySet().forEach(entry -> {

                String key = entry.getKey();

                List<String> matchingUrls = mapReport.get(key).getMatchingUrls();

                for (String urls : matchingUrls) {
                    Map<String, String> urlQuesryStringMap = dataProcessing.getQueryStringMap(urls, requests);

                    Map<String, Set<String>> value = entry.getValue();

                    if (mapReport.containsKey(key)) {
                        FeatureChecks negativechecks = new FeatureReport.FeatureChecks();

                        value.entrySet().forEach(valueEntry -> {

                            String vKey = valueEntry.getKey();

                            Set<String> vValue = valueEntry.getValue();

                            for (String sValue : vValue) {

                                if (urlQuesryStringMap.containsKey(vKey) && urlQuesryStringMap.get(vKey).equals(sValue)) {
//				negativeFeatureChecks.setMatchingUrls(matchingUrl);

                                    String msg = "Negative Value Foound: " + vKey + " = " + sValue;

//				 log.info("Message  : "+msg);
//				 

                                    if (vKey.equals("y_dnb")) {
                                        message.add(msg);
                                    } else {
                                        negativechecks.setTestResult(TestStatus.FAIL);

                                        negativechecks.setFeatureValuesChecks(value);
                                        message.add(msg);

                                        test = false;

                                    }

                                } else if (key.equalsIgnoreCase("GAMAPADCall")) {

                                    String url = URLDecoder.decode(urls, StandardCharsets.UTF_8);

                                    int as = url.indexOf("prev_scp");


                                    url = url.substring(as);

                                    url = url.substring(9);

                                    String[] slots = url.split("\\|");

                                    for (String slot : slots) {
                                        System.out.println("slots: " + slot);
                                        if (slot.contains(vKey)) {

                                            String[] getValue = slot.split("&");

                                            Map<String, String> mapOfSlotsData = new HashMap<>();

                                            for (String slotsVal : getValue) {

                                                String[] keyVal = slotsVal.split("=");
                                                if (keyVal.length == 2) {
                                                    mapOfSlotsData.put(keyVal[0], keyVal[1]);
                                                }
                                            }

                                            if (mapOfSlotsData.get(vKey) != null) {

                                                if (mapOfSlotsData.get(vKey).equals(sValue)) {
                                                    String loc = mapOfSlotsData.get("loc");
//					    		 report.getPositiveFeatureChecks().setTestResult(TestStatus.PASS);

                                                    String msg = "Negative Value Found: " + vKey + "=" + sValue
                                                            + " at Location: " + loc;

                                                    if (vKey.equals("y_dnb")) {
                                                        message.add(msg);
                                                    } else {
                                                        negativechecks.setTestResult(TestStatus.FAIL);

                                                        negativechecks.setFeatureValuesChecks(value);
                                                        message.add(msg);
                                                        test = false;
                                                    }

                                                }

                                            }

                                        }

                                    }

                                }

                            }

                            negativechecks.setTestMessages(message);

                            positiveReport.put(key, negativechecks);

                        });

                    }

                }

            });

            if (test) {

                criteriaBo.getFeatureKeysPositive().entrySet().forEach(entry -> {

                    String key = entry.getKey();

//				System.out.println("Report : " + mapReport);
//
//				System.err.println("key: " + key);

                    if (mapReport.containsKey(key)) {

                        List<String> matchingUrls = mapReport.get(key).getMatchingUrls();

                        for (String urls : matchingUrls) {
                            Map<String, String> urlQuesryStringMap = dataProcessing.getQueryStringMap(urls,
                                    requests);

                            Map<String, Set<String>> value = entry.getValue();

                            FeatureChecks positiveFeatureChecks = new FeatureReport.FeatureChecks();
                            value.entrySet().forEach(valueEntry -> {

                                String vKey = valueEntry.getKey();

                                Set<String> vValue = valueEntry.getValue();

                                for (String sValue : vValue) {

                                    if (urlQuesryStringMap.containsKey(vKey)
                                            && urlQuesryStringMap.get(vKey).equals(sValue)) {

//				 positiveFeatureChecks.setMatchingUrls(matchingUrl);
                                        positiveFeatureChecks.setTestResult(TestStatus.PASS);

                                        positiveFeatureChecks.setFeatureValuesChecks(value);

                                        String msg = "Positive Check Pass " + vKey + " " + sValue;

                                        message.add(msg);

                                    } else if (key.equalsIgnoreCase("GAMAPADCall")) {

                                        String url = URLDecoder.decode(urls, StandardCharsets.UTF_8);

                                        int as = url.indexOf("prev_scp");

                                        url = url.substring(as);

                                        url = url.substring(9);

                                        System.out.println("prev URL: " + url);

                                        String[] slots = url.split("\\|");

                                        for (String slot : slots) {

                                            if (slot.contains(vKey)) {

                                                String[] getValue = slot.split("&");

                                                Map<String, String> mapOfSlotsData = new HashMap<>();

                                                for (String slotsVal : getValue) {

                                                    String[] keyVal = slotsVal.split("=");
                                                    if (keyVal.length == 2) {
                                                        mapOfSlotsData.put(keyVal[0], keyVal[1]);
                                                    }
                                                }

                                                if (mapOfSlotsData.get(vKey) != null) {

                                                    if (mapOfSlotsData.get(vKey).equals(sValue)) {

                                                        String location = mapOfSlotsData.get("loc");
//					    		 report.getPositiveFeatureChecks().setTestResult(TestStatus.PASS);

//					    		 positiveFeatureChecks.setMatchingUrls(matchingUrl);
                                                        positiveFeatureChecks.setTestResult(TestStatus.PASS);

                                                        positiveFeatureChecks.setFeatureValuesChecks(value);

                                                        String msg = "Positive Check Pass :" + vKey + "=" + sValue
                                                                + " at location :" + location;

                                                        message.add(msg);

                                                    }

                                                }

                                            }
                                        }

                                    }
                                }

                                positiveFeatureChecks.setTestMessages(message);

                                positiveReport.put(key, positiveFeatureChecks);

                            });

                        }
                    }


                });
            }

            repo.put(feature, positiveReport);

//		report.setNegativeFeatureChecks(negativeReport);
            report.setFeatureChecks(repo);

            return report;

        }

        negativeKey = false;


        return null;

    }

}
