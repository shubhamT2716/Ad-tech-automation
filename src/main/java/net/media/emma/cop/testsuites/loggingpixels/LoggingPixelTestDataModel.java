package net.media.emma.cop.testsuites.loggingpixels;

import lombok.*;
import java.util.List;
@Setter @Getter
public class LoggingPixelTestDataModel{

    String userAgent;
    String environment;
    String publisherUrl;
    List<DataTypeChecks> loggingPixelTests;

    @Getter @Setter
    public static class DataTypeChecks{

        public String key;
        public String valueDataType;
        public String valueRegex;
        public List<String> negativeStrings;

    }

    public DataTypeChecks getDataTypeCheckFor(String key){

        for(DataTypeChecks dataTypeChecks : loggingPixelTests){
            if(dataTypeChecks.key.equals(key)){
                return dataTypeChecks;
            }
        }

        return null;

    }
}
