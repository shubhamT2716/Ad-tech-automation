package net.media.emma.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class ExecuteTests {

    @PostMapping("/fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            System.out.println(System.getProperty("user.dir"));
            String filePath = "C:\\Users\\mohib.s\\Desktop\\springFramework\\src\\main\\resources\\yaml\\" + fileName; // Replace with the actual path to your files directory
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            return new String(fileBytes);
        } else {
            return "";
        }
    }

    @GetMapping("/TestDataInput")
    public void printDemo2() {
    }

    @PostMapping("/TestDataInput")
    public String printDemoPos2t() {
        return "report";
    }
}
