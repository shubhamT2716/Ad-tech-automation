package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.SmokeTestSummeryDto.Testdata;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.FileOperations;
import net.media.emma.utils.MailServiceImpl;

@Slf4j
@Service
@ComponentScan
@EnableScheduling
public class YamlTestSuiteScheduling {

	@Autowired
	private FileOperations fileOperations;

	@Autowired
	private GenericTestSuite yamlTestSuite;

	@Value("${yamlTestCriteriaFilesPath}")
	private String yamlTestCriteriaFilesPath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String yamlSmokeTestSuiteFilesPath;

	@Autowired
	private MailServiceImpl mailServiceImpl;

	@Autowired
	private TemplateEngine engine;

	@Autowired
	private ObjectMapper yamlMapper;

	@Value("${zipFileBasePath}")
	private String zipFilePath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String smokeTestYamlPath;

	@Scheduled(fixedDelayString = "${smokeTestSchedulerFixedDelay}")
	public void smokeTestScheduling() {

		String environment = "hbx.media.net";

		String userAgent = "Chrome.Desktop.Mac";

		List<String> list = this.fileOperations.listFilesUsingFilesList(smokeTestYamlPath).stream()
				.filter(x -> !x.contains("techinvestor") && !x.contains("247")).collect(Collectors.toList());

		boolean flagForMail = true;

		getAutoTest(environment, userAgent, list, flagForMail);

	}

	public SmokeTestRawDataDto getAutoTest(String environment, String userAgent, List<String> selectedFiles,
			boolean flagForMail) {

		// if (selectedFiles == null || selectedFiles.isEmpty()) {
		// return new HashMap<>();
		// }

		SmokeTestRawDataDto rawDataDto = new SmokeTestRawDataDto();

		Map<String, CompletableFuture<TestReportModel>> map = new HashMap<>();
		List<String> listOfYamlEnv = new ArrayList<>();

		Set<String> listOfYaml = selectedFiles.stream()
				// .filter(x -> !x.contains("aol") && !x.contains("tech") &&
				// !x.contains("webmd")).limit(2)
				.collect(Collectors.toSet());

		String[] environments = environment.split(",");
		String[] userAgents = userAgent.split(",");

		for (String yaml : listOfYaml) {

			for (String env : environments) {
				for (String agent : userAgents) {

					TestCriteriaBo testCriteriaBo = getYamlObject(yamlSmokeTestSuiteFilesPath + yaml);

					if (null == testCriteriaBo) {
						log.error("Error while reading yaml file");
						continue;
					}

					// Update the environment and userAgent parameters in TestCriteriaBo
					testCriteriaBo.setEnvironment(env);
					testCriteriaBo.setUserAgent(agent);

//					map.put(yaml + "_" + env + "_" + agent, yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));

					map.put(yaml + "_" + env + "_" + agent, yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
					listOfYamlEnv.add(yaml + "_" + env + "_" + agent);
				}
			}
		}

		// get values of Map into CompeletableFuture
		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(map.values().toArray(new CompletableFuture[map.size()]));

		allFutures.join();

		Map<String, TestReportModel> finalResult = new HashMap<>();
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

		for (String yaml : listOfYamlEnv) {
			Testdata testdata = new SmokeTestSummeryDto().new Testdata();

			reportModel = finalResult.get(yaml);

			testReportModelList.add(reportModel);

			testdata.setEnvironment(reportModel.getTestMetaData().getEnvironment());
			testdata.setUserAgent(reportModel.getTestMetaData().getUserAgent());
			testdata.setBidExFileSize(reportModel.getBidExController().getTotalSizeDownloaded());
			testdata.setBidExTime(reportModel.getBidExController().getTotalResponseTime());
			testdata.setDateTime(reportModel.getTestMetaData().getDateTime());
			testdata.setHarFileDownloadLink(reportModel.getTestMetaData().getHarFileDownloadLink());
			testdata.setPublisherUrl(reportModel.getTestMetaData().getPublisherUrl());
			testdata.setMatchingUrls(reportModel.getBidExController().getMatchingUrls());

			testdataResult.add(testdata);

		}

		String zipFile = CommonBrowserUtil.createZipFile(zipFilePath, finalResult);

		if (flagForMail == true) {
			this.mailServiceImpl.sendMail("shubhammtrivedi@gmail.com", "shubhammtrivedi@gmail.com", testReportModelList,
					zipFile);

		}

		dto.setRawDataLink("zipDownload/" + zipFile);
		dto.setTestdatas(testdataResult);

		rawDataDto.setRawData(finalResult);
		rawDataDto.setSmokeReport(dto);

		return rawDataDto;

	}

	private TestCriteriaBo getYamlObject(String fileName) {

		try {

			ObjectMapper yamlMapper = new YAMLMapper();

			return yamlMapper.readValue(fileOperations.readFileAsInputStream(fileName), TestCriteriaBo.class);

		} catch (StreamReadException | DatabindException e) {
			log.error("Error while reading yaml file: " + fileName, e);
		} catch (IOException e) {
			log.error("Error while reading yaml file", e);
		}
		return null;
	}

	public String templateLoder(String user, Map<String, JsonNode> result) {
		Context context = new Context();
		context.setVariable("name", user);
		context.setVariable("result", result);
		return this.engine.process("SchedulerReport", context);
	}

	public JsonNode readDataFromZip(String fileName) {

		String filePath = zipFilePath + fileName;

		JsonNode resultData = CommonBrowserUtil.readDataFromZip(filePath);

		return resultData;

	}

}
