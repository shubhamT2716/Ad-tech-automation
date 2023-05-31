package net.media.emma.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.config.YamlTestCriteria;

@Controller @Slf4j
public class YamlTestSuiteController {

    @GetMapping("/yamlTestSuite")
    public String yamlTestSuite() {

        return "yamlTestSuite";
    }

    @PostMapping("/yamlTestSuite")
    public ModelAndView yamlTestSuite(@RequestParam String yaml) {

        ModelAndView mav = new ModelAndView("yamlTestSuite");

        try{

            ObjectMapper mapper = new YAMLMapper();
            YamlTestCriteria yamlTc = mapper.readValue(yaml, YamlTestCriteria.class);
            mav.addObject("message", yamlTc.getEnvironment());
    
        }catch( JsonProcessingException  e){
            log.error("YAML Processing Error", e);;
            mav.addObject("message", e.getMessage());
        }

       return mav;
    }
}