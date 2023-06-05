package net.media.emma.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class DisplayYamlFileData {

    @Value("${yamlFolderPath}")
    private String yamlFolderPath;

    @PostMapping("/fileContent")
    @ResponseBody
    public String getFileContent(@RequestBody String fileName) throws IOException {
        if (fileName != null && !fileName.isEmpty()) {
            String filePath = yamlFolderPath + fileName; // Replace with the actual path to your files directory
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
            return new String(fileBytes);
        } else {
            return "";
        }
    }

}
