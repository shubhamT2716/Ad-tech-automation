package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WebhookService implements WebhookServiceIn {

    @Autowired
    private FileOperations fileOperations;

    @Value("${commonYamlFilePath}")
    private String commonYamlFile;

    @Value("${autoTestFilePath}")
    private String autoTesterYamlFile;

    @Autowired
    private YamlTestSuiteScheduling yamlTestSuiteScheduling;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlTestCriteriaFilesPath;
	
	@Value("${emma.token}")
	private String emmaToken;
	
	@Value("${ssp.token}")
	private String sspToken;
	
	@Value("${prebid.token}")
	private String prebidToken;
	
	@Value("${emma}")
	private String emma;
	
	@Value("${ssp}")
	private String ssp;
	
	@Value("${prebid}")
	private String prebid;


    public static String generateMarkdownReport(SmokeTestRawDataDto data) {
        StringBuilder markdownBuilder = new StringBuilder();
        data.getRawData().entrySet().forEach(entry -> {

            markdownBuilder.append("**Test URL:** ").append(entry.getValue().getTestMetaData().getPublisherUrl())
                    .append("\n\n");

            markdownBuilder.append("\n## Test Result\n");
            markdownBuilder.append("- **Overall Result:** ").append(data.getTestResult().get("testResult")).append("\n")
                    .append("\n");

            markdownBuilder.append("- **Reason:** ").append("\n");

            data.getErrorMessage().get(entry.getKey()).entrySet().forEach(errorMessage -> {
                markdownBuilder.append("- " + errorMessage.getKey()).append(" : ").append(errorMessage.getValue())
                        .append("\n");

            });

            markdownBuilder.append("<details><summary>Click to expand</summary>");
            markdownBuilder.append("\n\n");
            markdownBuilder.append("\n");

            markdownBuilder.append("# Test Summary\n");
            markdownBuilder.append("\n");
            markdownBuilder.append("\n");
            markdownBuilder.append("**Test URL:** ").append(entry.getValue().getTestMetaData().getPublisherUrl())
                    .append("\n\n");
            markdownBuilder.append("**Scripts Used:**\n");

            markdownBuilder.append("-").append(entry.getValue().getBidExController().getMatchingUrls());

            // Test Details
            markdownBuilder.append("\n### Test Details\n");
            markdownBuilder.append("- **Size:** ")
                    .append(entry.getValue().getBidExController().getTotalSizeDownloaded()).append("\n");
            markdownBuilder.append("- **Svr:** ").append(entry.getValue().getBidExController().getBidexData().getSvr())
                    .append("\n");
            markdownBuilder.append("- **Ugd:** ").append(entry.getValue().getBidExController().getBidexData().getSugd())
                    .append("\n");
            markdownBuilder.append("- **Cid:** ").append(entry.getValue().getBidExController().getBidexData().getCid())
                    .append("\n");
            markdownBuilder.append("- **servname:** ")
                    .append(entry.getValue().getBidExController().getBidexData().getServname()).append("\n");
            markdownBuilder.append("- **Location:** ")
                    .append(entry.getValue().getBidExController().getBidexData().getCt()).append("\n");
            markdownBuilder.append("- **Console Message:** ")
                    .append(entry.getValue().getBidExController().getConsoleMessages()).append("\n");

            // network calls negative
            markdownBuilder.append("\n### Negative URL Checks\n");

            entry.getValue().getTestSummery().get("NegativeURLChecks").entrySet().forEach(nagativeCalls -> {

//				markdownBuilder.append("- **" + nagativeCalls.getKey() + ":** ").append(nagativeCalls.getValue())
//						.append("\n");
                if (nagativeCalls.getValue().equalsIgnoreCase("NOT PRESENT")) {
                    markdownBuilder.append("- NO " + nagativeCalls.getKey()).append("\n\n");
                } else {
                    markdownBuilder.append("- **" + nagativeCalls.getKey() + ":** ").append(nagativeCalls.getValue())
                            .append("\n");
                }

            });

            // network calls Positive
            markdownBuilder.append("\n### Positive URL Checks\n");

            entry.getValue().getTestSummery().get("PositiveURLChecks").entrySet().forEach(positiveCalls -> {

                markdownBuilder.append("- " + positiveCalls.getKey()).append("\n").append("\n");

            });

            markdownBuilder.append("\n### Cookie \n");

            System.err.println("Cookies: " + entry.getValue().getCookie());

            entry.getValue().getCookie().entrySet().forEach(cookieDomain -> {

                if (cookieDomain.getKey().equals("https://hbx.media.net")) {

                    markdownBuilder.append("- **" + "Domain" + ":** ").append(cookieDomain.getKey()).append("\n");
                    cookieDomain.getValue().entrySet().forEach(cookie -> {

                        markdownBuilder.append(cookie.getKey()).append(",");

                    });

                }

            });

            // Test Result
            markdownBuilder.append("\n## Test Result\n");
            markdownBuilder.append("- **Overall Result:** ").append(data.getTestResult().get("testResult")).append("\n")
                    .append("\n");

            markdownBuilder.append("- **Reason:** ").append("\n");

            data.getErrorMessage().get(entry.getKey()).entrySet().forEach(errorMessage -> {
                markdownBuilder.append("- " + errorMessage.getKey()).append(" : ").append(errorMessage.getValue())
                        .append("\n");

            });

            markdownBuilder.append("\n");

            markdownBuilder.append("**Full Report:** [View Full Report](" + data.getFullReport() + ")\n");

            markdownBuilder.append("</details>");
            markdownBuilder.append("\n");
            markdownBuilder.append("\n");

        });

        return markdownBuilder.toString();
    }

    @Override
    public void executeWebhook(String payload) {
        try {

            triggerEmma(payload);
//			Thread.sleep(5500);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void triggerEmma(String payload) {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> map = new HashMap<>();

        try {
            JsonNode jsonNode = mapper.readValue(payload, JsonNode.class);

            String projectId = jsonNode.get("project_id").toPrettyString();

            map.put("projectId", projectId);

            String brachName = jsonNode.get("merge_request").get("iid").toPrettyString();

            map.put("mr", brachName);

            String comment = jsonNode.get("object_attributes").get("description").toPrettyString();

            if (comment.contains("@emma")) {

                String sanity = "(?i)basic-sanity";

                Pattern patternSanity = Pattern.compile(sanity);

                Matcher matcherSanity = patternSanity.matcher(comment);

                if (matcherSanity.find()) {

                    String url = null;

                    String hostRegex = "(host|env|environment)\\s*:\\s*([\\S]+?)\\s*(?=[\\s,\"])";

                    String hb = "(?i)HB";

                    Pattern patternHb = Pattern.compile(hb);

                    Matcher matcherHb = patternHb.matcher(comment);

                    if (matcherHb.find()) {
                        url = getUrlFromItype("hb");
                    }
                    String mp = "(?i)MP";

                    Pattern patternMp = Pattern.compile(mp);

                    Matcher matcherMp = patternMp.matcher(comment);

                    if (matcherMp.find()) {

                        url = getUrlFromItype("mp");
                    }

                    String hb2 = "(?i)HB2";

                    Pattern patternhb2 = Pattern.compile(hb2);

                    Matcher matcherHb2 = patternhb2.matcher(comment);

//					if(comment.matches(hb2)) {

                    if (matcherHb2.find()) {
                        url = getUrlFromItype("hb2");
                    }

                    String host = extractValue(comment, hostRegex);


                    String domain = getHostFromUrl(url);

                    String itype = getItypeFromUrl(domain);


                    String ua = "Chrome.Desktop.Mac";

                    map.put("processStart", "Bot has started processing for " + url + "  with host: " + host
                            + " please wait for some time");

                    addResponseToGit(map);

                    List<String> autoTester = new ArrayList<>(getAutoTesterFilesTest());

                    List<String> commonFile = new ArrayList<>(getCommonFilesForApi(itype));

                    SmokeTestRawDataDto dataDto = this.yamlTestSuiteScheduling.getAutoTest(host, ua, autoTester,
                            commonFile, url, itype, "", false);

                    map.put("response", dataDto);

                } else {

                    String hostRegex = "(host|env|environment)\\s*:\\s*([\\S]+?)\\s*(?=[\\s,\"])";
                    String uaRegex = "(userAgent|ua)\\s*:\\s*([\\S]+)\\s*(?=[\\s,\"])";
                    String publisher = "(url|publisher|urls|pubs)\\s*:\\s*([\\S]+)\\s*(?=[\\s,\"])";
                    String itype = "itype\\s*:\\s*([\\S]+)\\s*(?=[\\s,\"])";

                    String pub = extractValue(comment, publisher);
                    String host = extractValue(comment, hostRegex);
                    String ua = extractValue(comment, uaRegex);
                    String it = extractValue(comment, itype);


                    if (it == null) {
                        String domain = getHostFromUrl(pub);

                        it = getItypeFromUrl(domain);
                    }

                    map.put("processStart",
                            "Bot has started processing with host:" + host + " please wait for some time");

                    addResponseToGit(map);

                    List<String> commonFile = new ArrayList<>(getCommonFilesForApi(it));

                    List<String> autoTest = new ArrayList<>(getAutoTesterFilesTest());

                    SmokeTestRawDataDto dataDto = this.yamlTestSuiteScheduling.getAutoTest(host, ua, autoTest,
                            commonFile, pub, it, "", false);


                    map.put("response", dataDto);
                }

                addResponseToGit(map);

            }

        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String getUrlFromItype(String itype) {

        Map<String, String> list = new HashMap<>();

        list.put("hb2", "https://time.com/?mnet_test=1");

        list.put("mp", "https://www.forbes.com/?medianet_test=1,https://www.fox13now.com/?medianet_test=1,https://www.daily.co.jp/?medianet_test=1");

        list.put("hb", "https://www.nutritionix.com/brands/restaurant?mnet_test=1,https://www.webmd.com/?mnet_test=1");

        return list.get(itype);
    }

//	private String getFileNameFromUrl(String url) {
//		Map<String, String> listOfurls = new HashMap<>();
//
//		List<String> list = new ArrayList<>();
//
//		listOfurls.put("daily.co.jp", "DailyCoMP.yaml");
//		listOfurls.put("fox13now.com", "fox13now.yaml");
//		listOfurls.put("forbes.com", "forbesMp.yaml");
//		listOfurls.put("time.com", "timeHome.yaml");
//		listOfurls.put("webmd.com", "webmd.yaml");
//		listOfurls.put("nutritionix.com", "nutritionix.yaml");
//
//		String a[] = url.split(",");
//
//		for (String pub : a) {
//			String yaml = listOfurls.get(pub);
//
//			list.add(yaml);
//		}
//
//		String yml = String.join(",", list);
//
//		return yml;
//
//	}

    private String getItypeFromUrl(String url) {

        String[] pub = url.split(",");

        Map<String, String> listOfurls = new HashMap<>();

        listOfurls.put("daily.co.jp", "MP");
        listOfurls.put("fox13now.com", "MP");
        listOfurls.put("forbes.com", "MP");
        listOfurls.put("time.com", "HB2");
        listOfurls.put("webmd.com", "HB");
        listOfurls.put("nutritionix.com", "HB");
        listOfurls.put("shubhammedia.github.io", "MP");

        String itype = null;

        for (String a : pub) {
            itype = listOfurls.get(a);
        }

        return itype;

    }

//	private List<String> getYamlFromUrl(String url) {
//		List<String> listOfFiles = new ArrayList<>();
//
//		String publishers[] = url.split(",");
//
//		try {
//
//			for (String yaml : getYamlFiles()) {
//
//				for (String pub : publishers) {
//					if (yaml.contains(getFileNameFromUrl(pub))) {
//						listOfFiles.add(yaml);
//					}
//				}
//
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		return listOfFiles;
//
//	}

    private String getHostFromUrl(String urls) {

        String[] url = urls.split(",");

        List<String> li = new ArrayList<>();

        try {

            for (String pub : url) {
                URL ur = new URL(pub);

                String host = ur.getHost();

                if (host.contains("www.")) {
                    host = host.substring(4);

                }

                li.add(host);

            }

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String updateUrl = String.join(",", li);

        return updateUrl;

    }

    private String extractValue(String comment, String pattern) {
        Pattern pattern2 = Pattern.compile(pattern);

        Matcher matcher = pattern2.matcher(comment);

        if (matcher.find()) {

            try {
                return matcher.group(2).trim();
            } catch (Exception e) {
                return matcher.group(1).trim();
            }


        }

        return null;
    }

    private String getAccessToken(Object projectId) {
        Map<String, String> mapOfProjectIdKy = new HashMap<>();

        mapOfProjectIdKy.put("15491", emmaToken);
        mapOfProjectIdKy.put("8300", prebidToken);
        mapOfProjectIdKy.put("6340", sspToken);

        String key = mapOfProjectIdKy.get(projectId);

        return key;

    }

    private void addResponseToGit(Map<String, Object> response) {

        String girUrl = "http://tree.mn/api/v4/projects/" + response.get("projectId") + "/merge_requests/"
                + response.get("mr") + "/notes";

        log.info("git process start");
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        //glpat-zzNwY6CKgyLaBYg_7PuL
        //glpat-spsyh2z6QMEQs5sQnKD7

        // emma key : glpat-fyX_Nidzz5EE6Vazr-Go
        // prebidKey: glpat-eiZcFRxrY4n_vwhzqpfF


        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("PRIVATE-TOKEN", getAccessToken(response.get("projectId")));

        Map<String, String> map = new HashMap<>();


        if (response.get("response") == null) {
            map.put("body", response.get("processStart").toString());
        } else {
            SmokeTestRawDataDto rawReport = (SmokeTestRawDataDto) response.get("response");
            String html = null;

            try {

                log.info(rawReport.getBidExReport().toString());
                html = generateMarkdownReport(rawReport);

            } catch (Exception e) {
                e.printStackTrace();
            }

            map.put("body", html);
        }

        HttpEntity<Map<String, String>> entity = new HttpEntity<Map<String, String>>(map, headers);

        ResponseEntity<String> data = restTemplate.postForEntity(girUrl, entity, String.class);

        log.info("process completed: " + data.getBody() + " : " + data.getStatusCode());
    }

    private Set<String> getCommonFilesForApi(String itype) {
        Set<String> fileName = fileOperations.listYamlFiles(commonYamlFile + "/" + itype);

        return fileName;
    }


    private Set<String> getAutoTesterFilesTest() {
        Set<String> fileName = fileOperations.listYamlFiles(autoTesterYamlFile);

        return fileName;
    }

}
