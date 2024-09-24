package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
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


import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



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
	private FileHandling fileHandling;

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


					String a[] = { content, yamlBuilder.toString() };
					
					testCriteriaBo = yamlUtils.buildYaml(a);
					
					
					
			
					
							testCriteriaBo.setUserAgent(agent);
							testCriteriaBo.setEnvironment(env);
							testCriteriaBo.setPublisherUrl(pub);							
							
							map.put(pub+"_"+env+"_"+agent , yamlTestSuite.getTestResultsThreadPool(testCriteriaBo));
							listOfYamlEnv.add(pub+"_"+env +"_"+agent);
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

		Map<String, List<String>> errorMessagePositive = new HashMap<>();
		
		Map<String, List<String>> errorMessageNegative = new HashMap<>();

		Map<String, Map<String, List<String>>> error = new HashMap<>();

		String zipFile = CommonBrowserUtil.createZipFile(zipFilePath, finalResult);

		String testResult = getTestResult(error);
		
		for (String yaml : listOfYamlEnv) {
			
			
			Testdata testdata = new SmokeTestSummeryDto().new Testdata();

			
			reportModel = finalResult.get(yaml);
			
			
			testReportModelList.add(reportModel);

			bidExReport.put(yaml, reportModel.getBidExController());

			Map<String, PositiveURLChecks> model = reportModel.getUrlChecks().getPositiveUrlChecks();
			
			Map<String, NegativeURLChecks> nagativeModel=reportModel.getUrlChecks().getNegativeUrlChecks();

			errorMessagePositive = model.entrySet().stream()
					.filter(n -> n.getValue().getTestMessages() != null && !n.getValue().getTestMessages().isEmpty())
					.collect(Collectors.toMap(Map.Entry::getKey, e -> {
						try {
							return e.getValue().getTestMessages();
						} catch (Exception e2) {
							log.info("Error While read error message from testReport Data" + e);
						}
						return null;

					}));
			
			nagativeModel.entrySet().forEach(entry->{
				
				if(entry.getValue().getCountOfMatchingUrls()!=0)
				{
					errorMessageNegative.put(entry.getKey(), entry.getValue().getMatchingUrls());
				}
			});

//			error.put(yaml, errorMessagePositive);
			
			error.computeIfAbsent(yaml, k->new HashMap<>(errorMessageNegative)).putAll(errorMessagePositive);

			
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

		
		
		
		try {
			this.fileHandling.addSmokeReportInFile(finalResult, testReportModelList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (flagForMail == true && email!=null && !email.isBlank()&& !email.isEmpty()) {
			this.mailServiceImpl.sendMail(email, email, testReportModelList,
					zipFile, testResult);

		}
		
		
		Map<String, String> testReport=new HashMap<>();
		
		dto.setTestdatas(testdataResult);
		
		testReport.put("testResult", testResult);
		String reportLink="http://10.2.64.124:8080/yamlTestSuite/smokeTestSuite/"+ zipFile;
		testReport.put("fullReport:", reportLink);
		
		

		rawDataDto.setTestResult(testReport);
		rawDataDto.setRawData(finalResult);
		rawDataDto.setSmokeReport(dto);
		rawDataDto.setBidExReport(bidExReport);
		rawDataDto.setErrorMessage(getErrorMessage(error, reportModel));
		rawDataDto.setFullReport(reportLink);
		
		
		
		

		return rawDataDto;

	}
	
	
	public Map<String, Map<String, List<String>>> getErrorMessage(Map<String, Map<String, List<String>>> errorMessage, TestReportModel report)
	{
		Map<String , Map<String, List<String>>> getErrorMessage=new HashMap<>();
		
		
		
		errorMessage.entrySet().forEach(entry->{
			
			
			
			Map<String, List<String>> getMessage=new HashMap<>();
			
			entry.getValue().entrySet().forEach(networkCall->{
				
				List<String> error=new ArrayList<>();
				
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
				
				for(String message: networkCall.getValue())
				{
					if(message.startsWith("Positive key value check failed for key")||message.startsWith("Negative key value check failed for key")||
							message.startsWith("Call not fired")|| message.startsWith("Any one of key value check failed for key")||message.startsWith("https://hblg.media.net/nerrping.php")||message.startsWith("https://hblg.media.net/log?logid=kfk&evtid=flog"))
					{
						error.add(message);
					}
					
				}
				
				getMessage.put(networkCall.getKey(), error);
				
			});
			
			getErrorMessage.put(entry.getKey(), getMessage);
			
			
		});
		
		
		
		
		
		
		return getErrorMessage;
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
			
			if(value.getTestResult().toString().equalsIgnoreCase("FAIL"))
			{
				testResult.computeIfAbsent("PositiveURLChecks", k->new HashMap<>()).put(key, value.getTestResult().toString()+" : "+ value.getTestMessages());
			}
			else {
				testResult.computeIfAbsent("PositiveURLChecks", k->new HashMap<>()).put(key, value.getTestResult().toString());
				
			}
			
			
			
			
		});
		
		negativeReport.entrySet().forEach(entry->{
			String key=entry.getKey();
			NegativeURLChecks value=entry.getValue();
			
			if(value.getTestResult().toString().equals("NOTPASS"))
			{
//				testResult.put(key, "NOT PASS");
				
				testResult.computeIfAbsent("NegativeURLChecks", k->new HashMap<>()).put(key, "NOT PRESENT");
			}
			else {
				testResult.computeIfAbsent("NegativeURLChecks", k-> new HashMap<>()).put(key, "PASS");
			}
			
			
			
			
		});
		
		return testResult;
		
		
	}
	
	
	

//	private TestCriteriaBo getYamlObject(String fileName) {
//
//		try {
//
//			ObjectMapper yamlMapper = new YAMLMapper();
//
//			return yamlMapper.readValue(fileOperations.readFileAsInputStream(fileName), TestCriteriaBo.class);
//
//		} catch (StreamReadException | DatabindException e) {
//			log.error("Error while reading yaml file: " + fileName, e);
//		} catch (IOException e) {
//			log.error("Error while reading yaml file", e);
//		}
//		return null;
//	}

	public String templateLoder(String user, Map<String, JsonNode> result) {
		Context context = new Context();
		context.setVariable("name", user);
		context.setVariable("result", result);
		return this.engine.process("SchedulerReport", context);
	}

	public Map<String, Map<String, Object>> readDataFromZip(String fileName) {

		ObjectMapper mapper=new ObjectMapper();
		
		String filePath = zipFilePath + fileName;

		JsonNode resultData = CommonBrowserUtil.readDataFromZip(filePath);
		
		Map<String,Object> result=mapper.convertValue(resultData, new TypeReference<Map<String, Object>>() {});

		log.info("Result Data " + resultData);
		
		
		Map<String, Map<String, Object>> report=new HashMap<>();
		
		
		result.entrySet().forEach(entry->{
			
			Map<String, Object> data=new HashMap<>(); 
			
			Map<String, Object>map=mapper.convertValue(entry.getValue(),new TypeReference<Map<String, Object>>(){});
			
			Object testData=map.get("testMetaData");
			Object bidexData=map.get("bidExController");	
			Object summery=map.get("testSummery");	
			
//			Map<String, Object> urlReport=mapper.convertValue(map.get("urlChecks"), new TypeReference<Map<String, Object>>() {});
			
			Map<String, Object> metaData=mapper.convertValue(testData,new TypeReference<Map<String, Object>>(){});
			
			data.computeIfAbsent(metaData.get("publisherUrl")+":"+metaData.get("environment"), k -> {
				List<Object> list=new ArrayList<>();
				
				list.addAll(Arrays.asList(testData,bidexData,summery));
				return list;
			});

			
			 report.computeIfAbsent("basicReport", k -> new HashMap<String, Object>())
	          .putAll(data);
			 
			 report.computeIfAbsent("fullReport", k -> new HashMap<String, Object>())
	          .putAll(map);
			
		});
		
		return report;

	}
	
	
	

}
