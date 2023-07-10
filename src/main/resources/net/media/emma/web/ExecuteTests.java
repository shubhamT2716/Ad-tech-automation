package net.media.emma.web;

import net.media.emma.coptests.TestRunner;
import net.media.emma.utils.FileOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ExecuteTests {

    private final FileOperations fileOperations;

    @Autowired
    public ExecuteTests(FileOperations fileOperations) {
        this.fileOperations = fileOperations;
    }

    @Autowired
    private TestRunner testRunner;

    @GetMapping("/testDataInput")
    public void executeTest() {
    }

    @PostMapping("/testDataInput")
    public ResponseEntity<String> executeTest(String yaml) throws IOException {
        fileOperations.saveYamlFile(yaml);
        testRunner.runTests();
        String reportPath= System.getProperty("user.dir") +"/src/main/resources/Test-Automation-Report.html";
        // Read the report file into a string
        String reportContent = Files.readString(Paths.get(reportPath));

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);

        // Return the report content as a response
        return ResponseEntity.ok().headers(headers).body(reportContent);
    }
}
