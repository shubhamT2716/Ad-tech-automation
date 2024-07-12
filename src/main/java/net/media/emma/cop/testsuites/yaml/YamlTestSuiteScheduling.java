package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;


import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.SmokeTestSummeryDto.Testdata;
import net.media.emma.cop.testsuites.yaml.TestReportModel.BidExController;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.NegativeURLChecks;
import net.media.emma.cop.testsuites.yaml.URLChecksReportModel.PositiveURLChecks;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.CommonBrowserUtil;
import net.media.emma.utils.FileOperations;
import net.media.emma.utils.MailServiceImpl;
import net.media.emma.utils.YamlUtils;

@Slf4j
@Service
@ComponentScan
@EnableScheduling
public class YamlTestSuiteScheduling  implements YamlSuiteSmokeTestsService{

	@Autowired
	private FileOperations fileOperations;

	@Autowired
	private YamlUtils yamlUtils;

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

//	@Autowired
//	private ObjectMapper yamlMapper;

	@Value("${zipFileBasePath}")
	private String zipFilePath;

	@Value("${yamlSmokeTestSuiteFilesPath}")
	private String smokeTestYamlPath;

	@Value("${commonYamlFilePath}")
	private String commonYaml;

	@Value("${autoTestFilePath}")
	private String autoTesterYamlFile;

//	@Scheduled(fixedDelayString = "${smokeTestSchedulerFixedDelay}")
	public void smokeTestScheduling() {

		String environment = "hbx.media.net";

		String userAgent = "Chrome.Desktop.Mac";

		List<String> list = this.fileOperations.listFilesUsingFilesList(smokeTestYamlPath).stream()
				.filter(x -> !x.contains("techinvestor") && !x.contains("247")).collect(Collectors.toList());

		boolean flagForMail = true;

		// getAutoTest(environment, userAgent, list, null, null,  null, flagForMail);
		getAutoTest(environment, userAgent, list, null, null,  null,null,flagForMail);

	}

	public String getTestResult(Map<String, Map<String, List<String>>> error) {
		// here we have to add some condition based requirement to handle result
		
		if(error==null)
		{
			return "pass";
		}
		else {
			return "fail";
		}
		
	}

	@Override
	public SmokeTestRawDataDto getAutoTest(String environment, String userAgent, List<String> selectedFiles,
			List<String> commonFiles, String pubs, String itype,String email,boolean flagForMail) {

		// if (selectedFiles == null || selectedFiles.isEmpty()) {
		// return new HashMap<>();
		// }

		
		
		
		SmokeTestRawDataDto rawDataDto = new SmokeTestRawDataDto();

		Map<String, CompletableFuture<TestReportModel>> map = new HashMap<>();
		List<String> listOfYamlEnv = new ArrayList<>();

		Set<String> listOfYaml = selectedFiles.stream()
				.collect(Collectors.toSet());

		String[] environments = environment.trim().split(",");
		String[] userAgents = userAgent.trim().split(",");
		;
	
		if(pubs!=null)
		{
			
			String[] publisher=pubs.trim().split(",");
			TestCriteriaBo testCriteriaBo=null;
			
			
			StringBuilder yamlBuilder=new StringBuilder();
			
			
			for (String commonYamls : commonFiles) {
			String commonContent = this.fileOperations.getCommonFile(itype+"/"+commonYamls);
				
				yamlBuilder.append(commonContent).append("\n");
//			 combinedCommonContent += commonContent;
				
				
			}

			
		for (String yaml : listOfYaml) {  
			for (String env : environments) {
				for (String agent : userAgents) {
					for(String pub: publisher) {
						
						
				
				ClassPathResource classPathResource = new ClassPathResource(autoTesterYamlFile + "/" + yaml);
				
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


					String a[] = { content, yamlBuilder.toString() };
					
					testCriteriaBo = yamlUtils.buildYaml(a);
					
					
					
			
					
							testCriteriaBo.setUserAgent(agent);
							testCriteriaBo.setEnvironment(env);
							testCriteriaBo.setPublisherUrl(pub);							
							
							map.put(yaml+"_"+env+"_"+agent , yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
							listOfYamlEnv.add(yaml+"_"+env +"_"+agent);
					}

					}
		
			}
			
			
		}
			
		}else {
			
			TestCriteriaBo testCriteriaBo=null;
			
			
			StringBuilder yamlBuilder=new StringBuilder();
			
			if(itype.equalsIgnoreCase("all")) {
			
				for (String commonYamls : commonFiles) {
					String commonContent = this.fileOperations.getCommonFile(commonYamls);
						
						yamlBuilder.append(commonContent).append("\n");
//					 combinedCommonContent += commonContent;
						
						
					}
			}
			else {
				for (String commonYamls : commonFiles) {
					String commonContent = this.fileOperations.getCommonFile(itype + "/" + commonYamls);

					yamlBuilder.append(commonContent).append("\n");
//					 combinedCommonContent += commonContent;

				}
			}

//			String combinedCommonContent = "";

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

//						ObjectMapper yamlMapper = new YAMLMapper();

//						List<String> list = new ArrayList<>();
//						list.add(content);

//						System.out.println(content);
						
						String a[] = { content, yamlBuilder.toString() };

						testCriteriaBo = yamlUtils.buildYaml(a);

						System.err.println("Agnet: "+agent);
						
						testCriteriaBo.setUserAgent(agent);
						testCriteriaBo.setEnvironment(env);
	
						System.err.println(testCriteriaBo.getPublisherUrl()+" :  "+testCriteriaBo.getKeysToVerifyPositive());
						System.out.println();
						System.out.println(testCriteriaBo.getUserAgent());

						map.put(yaml + "_" + env+"_"+agent, yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
						listOfYamlEnv.add(yaml + "_" + env+"_"+agent);
					}

				}

			}

		}
		
		// get values of Map into CompeletableFuture
		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(map.values().toArray(new CompletableFuture[map.size()]));

		allFutures.join();

		
		
		Map<String, TestReportModel> finalResult = new HashMap<>();
		if (allFutures.isDone()) {
			
//			System.err.println(" MAP : "+map);

			finalResult = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> {
				try {
					
//					System.err.println(" Result Value : "+e.getKey()+"  : "+ e.getValue().get());
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

		Map<String, BidExController> bidExReport = new HashMap<>();

		Map<String, List<String>> errorMessage = new HashMap<>();

		Map<String, Map<String, List<String>>> error = new HashMap<>();

		
		
		for (String yaml : listOfYamlEnv) {
			
			
			Testdata testdata = new SmokeTestSummeryDto().new Testdata();

			
			reportModel = finalResult.get(yaml);
			
			
			testReportModelList.add(reportModel);

			bidExReport.put(yaml, reportModel.getBidExController());

			Map<String, PositiveURLChecks> model = reportModel.getUrlChecks().getPositiveUrlChecks();

			errorMessage = model.entrySet().stream()
					.filter(n -> n.getValue().getTestMessages() != null && !n.getValue().getTestMessages().isEmpty())
					.collect(Collectors.toMap(Map.Entry::getKey, e -> {
						try {
							return e.getValue().getTestMessages();
						} catch (Exception e2) {
							log.info("Error While read error message from testReport Data" + e);
						}
						return null;

					}));

			error.put(yaml, errorMessage);

			testdata.setEnvironment(reportModel.getTestMetaData().getEnvironment());
			testdata.setUserAgent(reportModel.getTestMetaData().getUserAgent());
			testdata.setBidExFileSize(reportModel.getBidExController().getTotalSizeDownloaded());
			testdata.setBidExTime(reportModel.getBidExController().getTotalResponseTime());
			testdata.setDateTime(reportModel.getTestMetaData().getDateTime());
			testdata.setHarFileDownloadLink(reportModel.getTestMetaData().getHarFileDownloadLink());
			testdata.setPublisherUrl(reportModel.getTestMetaData().getPublisherUrl());
			testdata.setMatchingUrls(reportModel.getBidExController().getMatchingUrls());
			testdata.setBidexData(reportModel.getBidExController().getBidexData());
			testdata.setTestResultSummery(testResultSummery(reportModel.getUrlChecks()));
			
			
			
			testdataResult.add(testdata);

//			dto.setTestResultSummery(testResultSummery(reportModel.getUrlChecks()));
			
		}

		String zipFile = CommonBrowserUtil.createZipFile(zipFilePath, finalResult);

		String testResult = getTestResult(error);
		
		

		if (flagForMail == true && email!=null && !email.isBlank()&& !email.isEmpty()) {
			this.mailServiceImpl.sendMail(email, email, testReportModelList,
					zipFile, testResult);

		}
		
		
		Map<String, String> testReport=new HashMap<>();
		

//		sdto.setRawDataLink("zipDownload/" + zipFile);
		dto.setTestdatas(testdataResult);
		
		// dto.setTestResultSummery(testResultSummery(reportModel));
		
		testReport.put("testResult", testResult);
		String reportLink="http://10.2.64.124:8080/yamlTestSuite/smokeTestSuite/"+ zipFile;
		
//		String link="<a href=\"" + reportLink + "\">Show Report</a>";
		
		testReport.put("fullReport", reportLink);

		rawDataDto.setTestResult(testReport);
		rawDataDto.setRawData(finalResult);
		rawDataDto.setSmokeReport(dto);
		rawDataDto.setBidExReport(bidExReport);
		rawDataDto.setErrorMessage(error);
		
		
		
		

		return rawDataDto;

	}
	
	
	
	public static Map<String, Map<String, String>> testResultSummery(URLChecksReportModel result)
	{
		Map<String, Map<String, String>> testResult=new HashMap<>();
		
		Map<String, PositiveURLChecks> positiveReport=result.getPositiveUrlChecks();
		
		Map<String, NegativeURLChecks>  negativeReport=result.getNegativeUrlChecks();
		
		positiveReport.entrySet().forEach(entry->{
			String key=entry.getKey();
			PositiveURLChecks value=entry.getValue();
			
			
//			testResult.put(key, value.getTestResult().toString());
			
			testResult.computeIfAbsent("PositiveURLChecks", k->new HashMap<>()).put(key, value.getTestResult().toString());
			
			
			
		});
		
		negativeReport.entrySet().forEach(entry->{
			String key=entry.getKey();
			NegativeURLChecks value=entry.getValue();
			
			if(value.getTestResult().toString().equals("PASS"))
			{
//				testResult.put(key, "NOT PASS");
				
				testResult.computeIfAbsent("NegativeURLChecks", k->new HashMap<>()).put(key, "NOT PASS");
			}
			else {
				testResult.computeIfAbsent("NegativeURLChecks", k-> new HashMap<>()).put(key, "PASS");
			}
			
			
			
			
		});
		
		return testResult;
		
		
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
		
		System.err.println(fileName);

		String filePath = zipFilePath + fileName;
		
		System.err.println(filePath);

		JsonNode resultData = CommonBrowserUtil.readDataFromZip(filePath);

		log.info("Result Data " + resultData);

		return resultData;

	}

}
