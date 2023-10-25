//package net.media.emma.utils;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.yaml.snakeyaml.Yaml;
//import org.yaml.snakeyaml.constructor.Constructor;
//
//import java.io.FileReader;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.Map;
//
//public class MergeYamlFiles {
//	
//	public static void deepMerge(Map<String, Object> target, Map<String, Object> source) {
//        for (Map.Entry<String, Object> entry : source.entrySet()) {
//            String key = entry.getKey();
//            Object value = entry.getValue();
//
//            if (value instanceof Map) {
//                // If the value is another map, recursively merge it
//                if (target.containsKey(key) && target.get(key) instanceof Map) {
//                    deepMerge((Map<String, Object>) target.get(key), (Map<String, Object>) value);
//                } else {
//                    // If the key doesn't exist in the target, or if it's not a map, just put it
//                    target.put(key, value);
//                }
//            } else {
//                // For non-map values, simply overwrite the target value
//                target.put(key, value);
//            }
//        }
//    }
//
//    public static void main(String[] args) throws IOException {
//        // Load the contents of the two YAML files
//        Yaml yaml = new Yaml(new Constructor(Map.class));
//
//        try (FileReader reader1 = new FileReader("src/main/resources/yamlSmokeTestSuiteFiles/engadget.yaml");
//             FileReader reader2 = new FileReader("src/main/resources/commonYaml/AllLogs.yaml")) {
//            Map<String, Object> data1 = yaml.load(reader1);
//            Map<String, Object> data2 = yaml.load(reader2);
//
//            // Merge the two data structures deeply
//            deepMerge(data1, data2);
//
//            // Serialize the merged data structure back to YAML
//            try (FileWriter writer = new FileWriter("merged.yaml")) {
//                yaml.dump(data1, writer);
//            }
//        }
//    }
//}