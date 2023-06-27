package net.media.emma.cop.testsuites.loggingpixels;

import lombok.*;

import java.util.List;
import java.util.Map;
@Setter @Getter @Builder  @NoArgsConstructor @AllArgsConstructor
public class LoggingPixelTestDataModel{

    public List<DataTypeChecks> loggingPixelTests;

    // Map<String, DataTypeChecks> loggingPixelTestsMap; //key-->  DataTypeChecks

    @Getter @Setter
    public static class DataTypeChecks{
        public String key;
        public String valueDataType;
        public String valueRegex;
        public List<String> negativeStrings;

    }
    public String pubUrl;

    public DataTypeChecks getDataTypeCheckFor(String key){

        for(DataTypeChecks dataTypeChecks : loggingPixelTests){
            if(dataTypeChecks.key.equals(key)){
                return dataTypeChecks;
            }
        }

        return null;

    }
}
