package net.media.emma.cop.testsuites.yaml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.SmokeTestSummeryDto.Testdata;
import net.media.emma.cop.testsuites.yaml.TestReportModel.Controller;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.FileOperations;
import net.media.emma.utils.MailServiceImpl;
import net.media.emma.utils.YamlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@ComponentScan
@EnableScheduling
public class YamlTestSuiteScheduling implements YamlSuiteSmokeTestsService {

	@Autowired
	private FileOperations fileOperations;

	@Autowired
	private YamlUtils yamlUtils;

	@Autowired
	private FileHandling fileHandling;

	@Autowired
	private GenericTestSuite yamlTestSuite;

	@Autowired
	private TemplateEngine engine;

	@Autowired
	private MailServiceImpl mailServiceImpl;

	@Autowired
	private Scheduler scheduler;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlTestCriteriaFilesPath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String yamlSmokeTestSuiteFilesPath;

	@Value("${harFileBasePath}")
	private String harFileBasePath;

	@Value("${zipFileBasePath}")
	private String zipFilePath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String smokeTestYamlPath;

	@Value("${commonYamlFilePath}")
	private String commonYaml;

	@Value("${autoTestFilePath}")
	private String autoTesterYamlFile;

	List<String> bidExHosts = Arrays.asList("hbx.media.net", "warp.media.net", "hb.yahoo.net", "contextual.media.net",
			"hbx-stage.media.net", "hbx-stage-2.media.net", "hbx-stage-3.media.net", "hbx-stage-4.media.net",
			"hbx-stage-1.media.net", "ssp-stage-hb.media.net", "ssp-stage-hb-1.media.net", "ssp-stage-hb-2.media.net",
			"ssp-stage-hb-3.media.net", "ssp-stage-hb-4.media.net");
	List<String> bidExControlers = Arrays.asList("bidexchange.js", "clientag.js", "js/tags/clientag.js",
			"ss/nes/bidexl2.js");
	List<String> getConsoleMessageOfUrl = Arrays.asList("checksync.php?", "bidexchange.js", "clientag.js",
			"js/tags/clientag.js", "ss/nes/bidexl2.js", "cksync?", "clientag.js?cid");
	List<String> urlController = List.of("checksync");
	StringBuilder size = new StringBuilder();

	public static Map<String, Map<String, String>> testResultSummery(URLChecksReportModel result) {
		Map<String, Map<String, String>> testResult = new HashMap<>();

		Map<String, PositiveURLChecks> positiveReport = result.getPositiveUrlChecks();

		Map<String, NegativeURLChecks> negativeReport = result.getNegativeUrlChecks();

		positiveReport.entrySet().forEach(entry -> {
			String key = entry.getKey();
			PositiveURLChecks value = entry.getValue();

			if (value.getTestResult().toString().equalsIgnoreCase("FAIL")) {
				testResult.computeIfAbsent("PositiveURLChecks", k -> new HashMap<>()).put(key,
						value.getTestResult().toString() + " : " + value.getTestMessages());
			} else {
				testResult.computeIfAbsent("PositiveURLChecks", k -> new HashMap<>()).put(key,
						value.getTestResult().toString());

			}

		});

		negativeReport.entrySet().forEach(entry -> {
			String key = entry.getKey();
			NegativeURLChecks value = entry.getValue();

			if (value.getTestResult().toString().equals("NOTPASS")) {
//				testResult.put(key, "NOT PASS");

				testResult.computeIfAbsent("NegativeURLChecks", k -> new HashMap<>()).put(key, "NOT PRESENT");
			} else {
				testResult.computeIfAbsent("NegativeURLChecks", k -> new HashMap<>()).put(key, "PASS");
			}

		});

		return testResult;

	}

//   @Scheduled(fixedDelayString = "${smokeTestSchedulerFixedDelay}")
	public void smokeTestScheduling() {

		String envMp = "warp.media.net";

		String userAgents = "Chrome.Desktop.Mac";// ,Chrome.Mobile.iPhone";

//    	getAutoTest(envMp, userAgent, mpFiles, commonFilesForMp, null, "MP", "qayahoohbtasksqainternals@flockgroups.com", flagForMail);
		List<String> urls = Arrays.asList("https://www.forbes.com/?medianet_test=1",
				"https://www.fox13now.com/?medianet_test=1", "https://www.daily.co.jp/?medianet_test=1");

		Map<String, CompletableFuture<TestReportModel>> result = new LinkedHashMap<>();

		Map<String, TestReportModel> finalResult = new ConcurrentHashMap<>();

		List<TestReportModel> listOfTestReportModel = new ArrayList<>();

		List<String> listOfPublisher = new ArrayList<>();

		try {

			String userAgent[] = userAgents.trim().split(",");

			for (String ua : userAgent) {
				for (String publisher : urls) {

					String pub = Scheduler.getHostFromUrl(publisher);

					result.put(pub + "_" + ua, scheduler.getSchedulerThread(envMp, ua, publisher));

					listOfPublisher.add(pub + "_" + ua);

				}
			}

			CompletableFuture<Void> allFutures = CompletableFuture
					.allOf(result.values().toArray(new CompletableFuture[result.size()]));

			allFutures.join();

			if (allFutures.isDone()) {

				finalResult = result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
					try {
						return e.getValue().get();
					} catch (InterruptedException | ExecutionException e1) {
						log.error("Error while getting result from CompletableFuture", e1);
					}
					return null;
				}));
			}

			String zipFile = CommonBrowserUtil.createZipFile(zipFilePath, finalResult);

			for (String pubs : listOfPublisher) {
				TestReportModel reportModel = finalResult.get(pubs);
				listOfTestReportModel.add(reportModel);

			}

			String testResult = scheduler.testResultForScheduler(finalResult);

			if (testResult.equalsIgnoreCase("fail")) {
//   	  	this.mailServiceImpl.sendMail("yahoohbtasksqa@flockgroups.com", "yahoohbtasksqa@flockgroups.com", listOfTestReportModel, zipFile, testResult);
				this.mailServiceImpl.sendMail("qayahoohbtasksqainternals@flockgroups.com",
						"qayahoohbtasksqainternals@flockgroups.com", listOfTestReportModel, zipFile, testResult);

			} else {
				this.mailServiceImpl.sendMail("qayahoohbtasksqainternals@flockgroups.com",
						"qayahoohbtasksqainternals@flockgroups.com", listOfTestReportModel, zipFile, testResult);

//   	  this.mailServiceImpl.sendMail("shubhammtrivedi@gmail.com", "shubhammtrivedi@gmail.com", listOfTestReportModel, zipFile, testResult);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getTestResult(Map<String, Map<String, List<String>>> errorMessage, List<String> consoleError) {
		// here we have to add some condition based requirement to handle result

		boolean hasError = false;

		if (!consoleError.isEmpty()) {
			return "fail";
		}

		for (Map.Entry<String, Map<String, List<String>>> entry : errorMessage.entrySet()) {
			for (Map.Entry<String, List<String>> error : entry.getValue().entrySet()) {
				if (error.getKey().equalsIgnoreCase("awlog") || error.getKey().equalsIgnoreCase("l1log")) {
					continue;
				} else if (error.getKey().equalsIgnoreCase("flog") || error.getKey().equalsIgnoreCase("nerrPing")) {

					return "fail";
				} else {

					for (String message : error.getValue()) {
						if (message.startsWith("Negative key value check failed for key")) {
							hasError = true;
//							break;
							return "fail";
						}
						if (message.startsWith("Call not fired") && error.getKey().equalsIgnoreCase("APPR-RA")) {
							hasError = true;
							return "fail";
						}
					}
				}
			}
		}

		if (hasError == false) {
			return "pass";
		} else {
			return "fail";
		}

	}

	@Override
	public SmokeTestRawDataDto getAutoTest(String environment, String userAgent, List<String> selectedFiles,
			List<String> commonFiles, String pubs, String itype, String email, boolean flagForMail,
			boolean flagForBot) {

		SmokeTestRawDataDto rawDataDto = new SmokeTestRawDataDto();

		Map<String, CompletableFuture<TestReportModel>> map = new ConcurrentHashMap<>();
		List<String> listOfYamlEnv = new ArrayList<>();

		Set<String> listOfYaml = selectedFiles.stream().collect(Collectors.toSet());

		String[] environments = environment.trim().split(",");
		String[] userAgents = userAgent.trim().split(",");

		if (pubs != null) {

			String[] publisher = pubs.trim().split(",");
			TestCriteriaBo testCriteriaBo = null;

			StringBuilder yamlBuilder = new StringBuilder();

			for (String commonYamls : commonFiles) {
				String commonContent = this.fileOperations.getCommonFile(itype + "/" + commonYamls);

				yamlBuilder.append(commonContent).append("\n");

			}

			for (String yaml : listOfYaml) {
				for (String env : environments) {
					for (String agent : userAgents) {
						for (String pub : publisher) {

							ClassPathResource classPathResource = new ClassPathResource(
									autoTesterYamlFile + "/" + yaml);

//				ClassPathResource classPathResource = new ClassPathResource(yamlTestCriteriaFilesPath + "/" + yaml);

							String content = null;

							try {
								InputStream inputStream = classPathResource.getInputStream();

								content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

							} catch (Exception e) {
								// Handle exceptions here
								log.error("Error while reading YAML file", e);
								continue; // Continue to the next iteration
							}

							List<String> list = new ArrayList<>();
							list.add(content);

							String[] a = { content, yamlBuilder.toString() };

							testCriteriaBo = yamlUtils.buildYaml(a);

							testCriteriaBo.setUserAgent(agent);
							testCriteriaBo.setEnvironment(env);
							testCriteriaBo.setPublisherUrl(pub);

							if (flagForBot) {

								map.put(pub + "_" + env + "_" + agent, yamlTestSuite.executeTestForBot(testCriteriaBo));
							} else {
								map.put(pub + "_" + env + "_" + agent,
										yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
							}
							listOfYamlEnv.add(pub + "_" + env + "_" + agent);
						}

					}

				}

			}

		} else {

			TestCriteriaBo testCriteriaBo = null;

			StringBuilder yamlBuilder = new StringBuilder();

			if (itype.equalsIgnoreCase("all")) {

				for (String commonYamls : commonFiles) {
					String commonContent = this.fileOperations.getCommonFile(commonYamls);

					yamlBuilder.append(commonContent).append("\n");

				}
			} else {
				for (String commonYamls : commonFiles) {
					String commonContent = this.fileOperations.getCommonFile(itype + "/" + commonYamls);

					yamlBuilder.append(commonContent).append("\n");

				}
			}

			for (String yaml : listOfYaml) {
				for (String env : environments) {
					for (String agent : userAgents) {

						String filesPath;

						if (itype.equalsIgnoreCase("all")) {
							filesPath = yamlSmokeTestSuiteFilesPath + "/" + yaml;
						} else {
							filesPath = yamlSmokeTestSuiteFilesPath + "/" + itype + "/" + yaml;
						}

						ClassPathResource classPathResource = new ClassPathResource(filesPath);

						String content = null;

						try {
							InputStream inputStream = classPathResource.getInputStream();

							content = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

						} catch (Exception e) {
							// Handle exceptions here
							log.error("Error while reading YAML file", e);
							continue; // Continue to the next iteration
						}

						String[] a = { content, yamlBuilder.toString() };

						testCriteriaBo = yamlUtils.buildYaml(a);

						testCriteriaBo.setUserAgent(agent);
						testCriteriaBo.setEnvironment(env);

						map.put(yaml + "_" + env + "_" + agent, yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
						listOfYamlEnv.add(yaml + "_" + env + "_" + agent);
					}

				}

			}

		}

		// get values of Map into CompeletableFuture
		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(map.values().toArray(new CompletableFuture[map.size()]));

		allFutures.join();

		Map<String, TestReportModel> finalResult = new ConcurrentHashMap<>();
		if (allFutures.isDone()) {

			finalResult = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
				try {
					return e.getValue().get();
				} catch (InterruptedException | ExecutionException e1) {
					log.error("Error while getting result from CompletableFuture", e1);
				}
				return null;
			}));
		}

		List<TestReportModel> testReportModelList = new ArrayList<>();

		SmokeTestSummeryDto dto = new SmokeTestSummeryDto();

		List<Testdata> testdataResult = new ArrayList<>();

		TestReportModel reportModel = new TestReportModel();

		Map<String, Controller> bidExReport = new ConcurrentHashMap<>();

		Map<String, List<String>> errorMessagePositive = new ConcurrentHashMap<>();

		Map<String, List<String>> errorMessageNegative = new ConcurrentHashMap<>();

		Map<String, Map<String, List<String>>> error = new ConcurrentHashMap<>();

		String zipFile = CommonBrowserUtil.createZipFile(zipFilePath, finalResult);

		List<String> consoleError = new ArrayList<>();

		for (String yaml : listOfYamlEnv) {

			Testdata testdata = new SmokeTestSummeryDto().new Testdata();

			reportModel = finalResult.get(yaml);

			testReportModelList.add(reportModel);

			bidExReport.put(yaml, reportModel.getController());

			Map<String, PositiveURLChecks> model = reportModel.getUrlChecks().getPositiveUrlChecks();

			Map<String, NegativeURLChecks> nagativeModel = reportModel.getUrlChecks().getNegativeUrlChecks();

			errorMessagePositive = model.entrySet().stream()
					.filter(n -> n.getValue().getTestMessages() != null && !n.getValue().getTestMessages().isEmpty()
							&& (!n.getKey().equalsIgnoreCase("awlog") || !n.getKey().equalsIgnoreCase("l1log")
									|| !n.getKey().equalsIgnoreCase("RA")))
					.collect(Collectors.toMap(Map.Entry::getKey, e -> {
						try {
							return e.getValue().getTestMessages();
						} catch (Exception e2) {
							log.info("Error While read error message from testReport Data" + e);
						}
						return null;

					}));

			nagativeModel.entrySet().forEach(entry -> {

				if (entry.getValue().getCountOfMatchingUrls() != 0) {
					errorMessageNegative.put(entry.getKey(), entry.getValue().getMatchingUrls());
				}
			});

//			error.put(yaml, errorMessagePositive);

			error.computeIfAbsent(yaml, k -> new ConcurrentHashMap<>(errorMessageNegative))
					.putAll(errorMessagePositive);

			testdata.setEnvironment(reportModel.getTestMetaData().getEnvironment());
			testdata.setUserAgent(reportModel.getTestMetaData().getUserAgent());
			testdata.setBidExFileSize(reportModel.getController().getTotalSizeDownloaded());
			testdata.setBidExTime(reportModel.getController().getTotalResponseTime());
			testdata.setDateTime(reportModel.getTestMetaData().getDateTime());
			testdata.setHarFileDownloadLink(reportModel.getTestMetaData().getHarFileDownloadLink());
			testdata.setPublisherUrl(reportModel.getTestMetaData().getPublisherUrl());
			testdata.setMatchingUrls(reportModel.getController().getMatchingUrls());
			testdata.setBidexData(reportModel.getController().getControllerData());
			testdata.setTestResultSummery(testResultSummery(reportModel.getUrlChecks()));

			consoleError.addAll(reportModel.getController().getConsoleMessages());

			testdataResult.add(testdata);

//			dto.setTestResultSummery(testResultSummery(reportModel.getUrlChecks()));

		}

		try {
			this.fileHandling.addSmokeReportInFile(finalResult, testReportModelList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String testResult = getTestResult(error, consoleError);

		if (flagForMail && email != null && !email.isBlank() && !email.isEmpty()) {
			this.mailServiceImpl.sendMail(email, email, testReportModelList, zipFile, testResult);

		}

		Map<String, String> testReport = new ConcurrentHashMap<>();

		dto.setTestdatas(testdataResult);

		testReport.put("testResult", testResult);
		String reportLink = "http://10.2.64.124:8080/yamlTestSuite/smokeTestSuite/" + zipFile;
		testReport.put("fullReport:", reportLink);

		rawDataDto.setTestResult(testReport);
		rawDataDto.setRawData(finalResult);
		rawDataDto.setSmokeReport(dto);
		rawDataDto.setControllerReport(bidExReport);
		rawDataDto.setErrorMessage(getErrorMessage(error, reportModel));
		rawDataDto.setFullReport(reportLink);

		return rawDataDto;

	}

	public Map<String, Map<String, List<String>>> getErrorMessage(Map<String, Map<String, List<String>>> errorMessage,
			TestReportModel report) {
		Map<String, Map<String, List<String>>> getErrorMessage = new ConcurrentHashMap<>();

		errorMessage.entrySet().forEach(entry -> {

			Map<String, List<String>> getMessage = new LinkedHashMap<>();

			entry.getValue().entrySet().forEach(networkCall -> {

				List<String> error = new ArrayList<>();

//				report.getUrlChecks().getNegativeUrlChecks().entrySet().forEach(negativecall->{
//
////					Map<String, List<String>> getMessage=new HashMap<>();
//
//					if(negativecall.getValue().getCountOfMatchingUrls()!=0)
//					{
//
//						getMessage.put(negativecall.getValue().getLabel(),negativecall.getValue().getMatchingUrls());
////						getErrorMessage.put(negativecall.getKey() ,k->new HashMap<>()).put(negativecall.getValue().getLabel(), negativecall.getValue().getCountOfMatchingUrls());
//					}
//
//
//				});

				for (String message : networkCall.getValue()) {
					if (message.startsWith("Positive key value check failed for key")
							|| message.startsWith("Negative key value check failed for key")
							|| message.startsWith("Call not fired")
							|| message.startsWith("Any one of key value check failed for key")
							|| message.startsWith("https://hblg.media.net/nerrping.php")
							|| message.startsWith("https://hblg.media.net/log?logid=kfk&evtid=flog")) {
						error.add(message);
					}

				}

				getMessage.put(networkCall.getKey(), error);

			});

			getErrorMessage.put(entry.getKey(), getMessage);

		});

		return getErrorMessage;
	}

	public String templateLoder(String user, Map<String, JsonNode> result) {
		Context context = new Context();
		context.setVariable("name", user);
		context.setVariable("result", result);
		return this.engine.process("SchedulerReport", context);
	}

	public Map<String, Map<String, Object>> readDataFromZip(String fileName) {

		ObjectMapper mapper = new ObjectMapper();

		String filePath = zipFilePath + fileName;

		JsonNode resultData = CommonBrowserUtil.readDataFromZip(filePath);

		Map<String, Object> result = mapper.convertValue(resultData, new TypeReference<Map<String, Object>>() {
		});

		log.info("Result Data " + resultData);

		Map<String, Map<String, Object>> report = new LinkedHashMap<>();

		result.entrySet().forEach(entry -> {

			Map<String, Object> data = new HashMap<>();

			Map<String, Object> map = mapper.convertValue(entry.getValue(), new TypeReference<Map<String, Object>>() {
			});

			Object testData = map.get("testMetaData");
			Object bidexData = map.get("bidExController");
			Object summery = map.get("testSummery");

//			Map<String, Object> urlReport=mapper.convertValue(map.get("urlChecks"), new TypeReference<Map<String, Object>>() {});

			Map<String, Object> metaData = mapper.convertValue(testData, new TypeReference<Map<String, Object>>() {
			});

			data.computeIfAbsent(metaData.get("publisherUrl") + ":" + metaData.get("environment"), k -> {
				List<Object> list = new ArrayList<>();

				list.addAll(Arrays.asList(testData, bidexData, summery));
				return list;
			});

			report.computeIfAbsent("basicReport", k -> new HashMap<String, Object>()).putAll(data);

//			 report.computeIfAbsent("fullReport", k -> new HashMap<String, Object>())
//	          .putAll(map);

		});

		report.put("fullReport", result);

		return report;

	}

	public Map<String, Map<String, Object>> readDataFromSchedulerFile(String fileName) {

		ObjectMapper mapper = new ObjectMapper();

		String filePath = zipFilePath + fileName;

		JsonNode resultData = CommonBrowserUtil.readDataFromZip(filePath);

		Map<String, Object> result = mapper.convertValue(resultData, new TypeReference<Map<String, Object>>() {
		});

		Map<String, Map<String, Object>> report = new HashMap<>();

		result.entrySet().forEach(entry -> {

			Map<String, Object> data = new HashMap<>();

			Map<String, Object> map = mapper.convertValue(entry.getValue(), new TypeReference<Map<String, Object>>() {
			});

			Object testData = map.get("testMetaData");

//           SchedulerReport.TestMetaData testmetaData=mapper.convertValue(testData, SchedulerReport.TestMetaData.class);

			Object bidexData = map.get("controller");

//            SchedulerReport.Controller controller=mapper.convertValue(bidexData, SchedulerReport.Controller.class);

			Object summery = map.get("testSummery");

//            SchedulerReport. controller=mapper.convertValue(bidexData, SchedulerReport.Controller.class);

			Map<String, Object> metaData = mapper.convertValue(testData, new TypeReference<Map<String, Object>>() {
			});

			data.computeIfAbsent(
					metaData.get("publisherUrl") + ":" + metaData.get("environment") + ":" + metaData.get("userAgent"),
					k -> {
						List<Object> list = new ArrayList<>();

						list.addAll(Arrays.asList(testData, bidexData, summery));
						return list;
					});

			report.computeIfAbsent("basicReport", k -> new HashMap<String, Object>()).putAll(data);

		});

		return report;

	}

}
