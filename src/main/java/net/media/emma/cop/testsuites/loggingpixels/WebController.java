package net.media.emma.cop.testsuites.loggingpixels;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;
import net.media.emma.service.GenericTestSuite;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Controller("loggingPixelsTestSuiteController") @Slf4j @RequestMapping("/loggingPixelsTestSuite/")
public class WebController
{

//    @Autowired GenericTestSuite yamlTestSuite;
    @Autowired FileOperations fileOperations;
    @Autowired LoggingPixelTestSuite pixelTestSuite;

    @GetMapping
    public ModelAndView yamlTestSuite() {

        ModelAndView mav = new ModelAndView("yamlTestSuite");
//        mav.addObject("files", fileOperations.listFilesUsingFilesList("src/main/resources/loggingPixelsTestCriteriaFiles/"));//  Arrays.asList(null, "testCriteria.yaml", "testCriteria2.yaml"));

        return mav;
    }

    @PostMapping
    public ModelAndView yamlTestSuite(@RequestParam String yaml) {

        ModelAndView mav = new ModelAndView("yamlTestSuiteReport");
        LoggingPixelTestDataModel testCriteria;

        try{

            ObjectMapper mapper = new YAMLMapper();
            testCriteria = mapper.readValue(yaml, LoggingPixelTestDataModel.class);
            mav.addObject("message", "test");

//            List<LoggingPixelTestDataModel.DataTypeChecks> loggingKeyVal =  testCriteria.getLoggingPixelTests();

        }catch( Exception  e){
            log.error("YAML Processing Error", e);;
            mav.addObject("message", e.getMessage());
            testCriteria = LoggingPixelTestDataModel.builder().build();
        }

        pixelTestSuite.getLoggingPixelResults(testCriteria);
        mav.addObject("message",  pixelTestSuite.getLoggingPixelResults(testCriteria));//t

       return mav;
    }

    // @Value("${yamlFolderPath}")
    // private String yamlFolderPath;

    @PostMapping("fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName){

        if(fileName.length() > 0){

            try {
                return new String( Files.readAllBytes(Paths.get("src/main/resources/loggingPixelsTestCriteriaFiles/" + fileName)));
            } catch (IOException e) {
                log.error("File Read Error", e);
                return e.getMessage();
            }
        }
        return "Input File Name Empty";
    }
}