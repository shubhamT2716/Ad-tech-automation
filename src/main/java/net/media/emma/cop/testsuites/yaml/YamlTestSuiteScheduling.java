package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
import net.media.emma.service.GenericTestSuite;
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

	@Autowired
	private MailServiceImpl mailServiceImpl;

	@Autowired
	private TemplateEngine engine;

	@Scheduled(fixedDelayString = "${smokeTestSchedulerFixedDelay}")
	public String getAutoTest() {

		Set<String> listOfYaml = fileOperations.listFilesUsingFilesList(yamlTestCriteriaFilesPath).stream().filter(x -> !x.contains("aol") && !x.contains("tech") && !x.contains("webmd")).limit(4).collect(Collectors.toSet());

		for (String yaml : listOfYaml) {

			TestCriteriaBo testCriteriaBo = getYamlObject(yamlTestCriteriaFilesPath + yaml);

			if( null == testCriteriaBo ) {
				log.error("Error while reading yaml file");
				continue;
			}

			CompletableFuture<String> result = yamlTestSuite.getTestResultsThreadPool(testCriteriaBo);
	
		}

		return "adsf";

		// this.mailServiceImpl.sendMail("rathnakar.ko@media.net", "rathnakar.ko@media.net", result);

	}

	private TestCriteriaBo getYamlObject(String fileName){

		try {

			ObjectMapper yamlMapper = new YAMLMapper();

			return yamlMapper.readValue(fileOperations.readFileAsInputStream(fileName) , TestCriteriaBo.class);

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

}
