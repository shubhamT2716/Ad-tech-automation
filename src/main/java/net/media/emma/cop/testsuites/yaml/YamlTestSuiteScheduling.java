package net.media.emma.cop.testsuites.yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;
import net.media.emma.utils.MailServiceImpl;

@Slf4j
@Component
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
	private ObjectMapper mapper;

	@Autowired
	private MailServiceImpl mailServiceImpl;

	@Autowired
	private TemplateEngine engine;

//	@Scheduled(cron = "0 0 */24 * * *") // runs every 24 hours
	@Scheduled(cron = "0 */3 * * * *")
	public void getAutoTest() {

		Set<String> listOfYaml = fileOperations.listFilesUsingFilesList(yamlTestCriteriaFilesPath);

		Map<String, JsonNode> result = new HashMap<>();

		int i = 0;

		ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

		try {
			for (String yaml : listOfYaml) {

				if (i < 2) {
					String yamlContent = fileOperations.readFileAsString(yamlTestCriteriaFilesPath + yaml);

					Thread thread = new Thread(yamlContent);
					thread.start();
					// thread.join();

					ObjectMapper objectMapper = new YAMLMapper();
					TestCriteriaBo testCriteria = objectMapper.readValue(yamlContent, TestCriteriaBo.class);

					String message = yamlTestSuite.getTestResults(testCriteria);

					result.put(yaml, mapper.readTree(message));
				}

				i++;
			}
			mav.addObject("message1", result);

		} catch (Exception e) {
			log.error("Error reading file.", e);
			mav.addObject("message", e.getMessage());
		}

		log.info("Result = " + result);

		this.mailServiceImpl.sendMail("vedant.a@media.net", "DailyYamlAutoTestResult",
				templateLoder("vedant.a@media.net", result));

	}

	public String templateLoder(String user, Map<String, JsonNode> result) {
		Context context = new Context();
		context.setVariable("name", user);
		context.setVariable("result", result);
		return this.engine.process("SchedulerReport", context);
	}

}
