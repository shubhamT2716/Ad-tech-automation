package net.media.emma.utils;

import net.media.emma.config.Loggers;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@ContextConfiguration(classes = Loggers.class)
public class TestDataFromYaml {

    private static String yamlFilePath;

    public void setYamlFilePath(String path) {
        yamlFilePath = path;
    }

    @DataProvider(name = "datapo")
    public Object[][] getTestData() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        FileInputStream fileInputStream = new FileInputStream(yamlFilePath);
        Map<String, LinkedHashMap<String, Object>> testDataMap = yaml.load(fileInputStream);

        List<Object[]> testDataList = new ArrayList<>();

        // Iterate over each test data entry in the YAML file
        for (String testDataKey : testDataMap.keySet()) {
            LinkedHashMap<String, Object> testData = testDataMap.get(testDataKey);
            testDataList.add(new Object[]{testData});
        }

        // Convert the list of test data to a two-dimensional array
        Object[][] testDataArray = new Object[testDataList.size()][1];
        testDataList.toArray(testDataArray);

        return testDataArray;
    }
}
