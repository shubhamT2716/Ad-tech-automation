package net.media.emma.cop.testsuites.yaml;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/yamlTestSuite/customYaml")
public class CustomYamlController {

	
	@Autowired
	private CustomYamlService customYamlService;
	
	@GetMapping()
	public ModelAndView createCustomYaml() {
		ModelAndView modelView = new ModelAndView("CustomYaml");

		return modelView;

	}
	
	
	@GetMapping("/positiveCalls")
	public Map<String, String> getPositiveCalls()
	{
		return this.customYamlService.getPositiveCalls();
	}
	
	@GetMapping("/negativeCalls")
	public Map<String, String> getNegativeCalls()
	{
		return this.customYamlService.getNegativeCalls();
	}
	
	@PostMapping("/submitForm")
	public String getCustomYaml(@ModelAttribute  CustomYamlTestCriteria bo,  Model model)
			//, @RequestParam("networkCallsNegative") String networkCallsNegative)
	{
		return bo.getEnvironment()+" : "+bo.getNegativeCalls()+" ::::";
	}
	
	
}
