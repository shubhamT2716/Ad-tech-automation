package net.media.emma.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.testsuites.pagetests.TestCriteria;
import net.media.emma.cop.testsuites.yaml.TestCriteriaBo;


@Slf4j
@Service
public class YamlUtils {
	

    public  TestCriteriaBo buildYaml(String [] yamlFiles)
    {
    	try {
    		
    		Yaml yaml=new Yaml();
    		
    		ObjectMapper mapper=new YAMLMapper();

            Map<String, Object> map = new HashMap<>();

            for(String yamlContent: yamlFiles)
            {
            	
//            	System.err.println(yamlContent);
            	
                    // read yaml file as map
            	Map<String, Object> data2=yaml.load(yamlContent);
            	
            	
            	System.out.println(data2);;
//                      map = yaml.load(yamlContent);
            	meargeYaml(map, data2);	
            }
            
            String finalYaml=yaml.dump(map);
            
            System.err.println("final Yaml  = "+finalYaml  );
            
            TestCriteriaBo bo=mapper.readValue(finalYaml, TestCriteriaBo.class);
            
            return  bo;
            
    	}
    	catch (Exception e) {
    		log.info("Error while mearging "+e.getMessage());
    		return null;
    		
		}
    }
    
    
    public net.media.emma.cop.testsuites.pagetests.TestCriteriaBo buildAutoTesterYaml(String [] yamlFiles)
    {
try {
    		
    		Yaml yaml=new Yaml();
    		
    		ObjectMapper mapper=new YAMLMapper();

            Map<String, Object> map = new HashMap<>();

            for(String yamlContent: yamlFiles)
            {
                    // read yaml file as map
            	Map<String, Object> data2=yaml.load(yamlContent);
//                      map = yaml.load(yamlContent);
            	meargeYaml(map, data2);	
            }
            
            String finalYaml=yaml.dump(map);
            
            net.media.emma.cop.testsuites.pagetests.TestCriteriaBo bo=mapper.readValue(finalYaml, net.media.emma.cop.testsuites.pagetests.TestCriteriaBo.class);
            
            return  bo;
            
    	}
    	catch (Exception e) {
    		log.info("Error while mearging "+e.getMessage());
    		return null;
    		
		}
    }
    
    
    public static void meargeYaml(Map<String, Object>yamlFile, Map<String,Object> commonfiles)
    {
    	for (Map.Entry<String, Object> entry : commonfiles.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // If the value is another map, recursively merge it
                if (yamlFile.containsKey(key) && yamlFile.get(key) instanceof Map) {
                	meargeYaml((Map<String, Object>) yamlFile.get(key), (Map<String, Object>) value);
                } else {
                    // If the key doesn't exist in the target, or if it's not a map, just put it
                	yamlFile.put(key, value);
                }
            } else {
                // For non-map values, simply overwrite the target value
            	yamlFile.put(key, value);
            }
        }
    }
    
    
	
}
