package net.media.emma.cop.testsuites.yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;

@Controller("yamlTestSuiteController") @Slf4j @RequestMapping("/yamlTestSuite/")
public class WebController {

    @Autowired GenericTestSuite yamlTestSuite;
    @Autowired FileOperations fileOperations;

    @GetMapping
    public ModelAndView yamlTestSuite() {

        ModelAndView mav = new ModelAndView("yamlTestSuite");
        mav.addObject("files", fileOperations.listFilesUsingFilesList("src/main/resources/yamlTestCriteriaFiles/"));//  Arrays.asList(null, "testCriteria.yaml", "testCriteria2.yaml"));

        return mav;
    }

    @PostMapping
    public ModelAndView yamlTestSuite(@RequestParam String yaml) {

        ModelAndView mav = new ModelAndView("yamlTestSuiteReport");

        try{

            ObjectMapper mapper = new YAMLMapper();
            TestCriteriaBo yamlTc = mapper.readValue(yaml, TestCriteriaBo.class);
            // mav.addObject("message", yamlTc.getEnvironment());
            //mav.addObject("message", yamlTestSuite.getTestResults(yamlTc));
            mav.addObject("message", yamlTestSuite.getTestResults_trishul(yamlTc));

        }catch( JsonProcessingException  e){
            log.error("YAML Processing Error", e);;
            mav.addObject("message", e.getMessage());
        }

       return mav;
    }

    // @Value("${yamlFolderPath}")
    // private String yamlFolderPath;

    @PostMapping("fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName){

        if(fileName.length() > 0){

            try {
                return new String( Files.readAllBytes(Paths.get("src/main/resources/yamlTestCriteriaFiles/" + fileName)));
            } catch (IOException e) {
                log.error("File Read Error", e);
                return e.getMessage();
            }
        }
        return "Input File Name Empty";
    }
}