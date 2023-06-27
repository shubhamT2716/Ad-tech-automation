package net.media.emma.cop.testsuites.loggingpixels;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class TestReportV2
{

    List<String> networkCalls;
    List<KeyData> allKeysData;
//networkCalls:[{url1},{url2}]allKeysData:[{key:ugd,testResult:pass/fail,message2:[{ugd should contain 1,2,3 however contains 4},{regex match failed}]},

    @Getter @Setter @Builder
    public static class KeyData{
        private String key;
        private String testResult;
        @Builder.Default
        List<String> message2 = new ArrayList<>();
    }
    
}
