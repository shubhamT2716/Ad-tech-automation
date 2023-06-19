package net.media.emma.cop.testsuites.yaml;

import java.util.List;

import com.microsoft.playwright.Request;

import net.media.emma.cop.testsuites.yaml.TestReportBo.CheckType;
import net.media.emma.cop.testsuites.yaml.TestReportBo.TestStatus;
import net.media.emma.cop.testsuites.yaml.TestReportBo.UrlChecks;

public class ReportBuilder {

    public static TestReportBo getNewReport(TestCriteriaBo testData){

        return TestReportBo.builder().build();
    }

    public static UrlChecks urlCheckExists(String label, TestReportBo trb){

        for(UrlChecks urlCheck : trb.getUrlChecks()){

            if(urlCheck.getUrlLabel().equals(label)){
                return urlCheck;
            }
        }

        return null;

    }

    public static UrlChecks logError(String label, TestReportBo.CheckType checkType, String urlPattern){

        return TestReportBo.UrlChecks.builder()
                                        .urlLabel(label)
                                        .urlCheckType(checkType)
                                        .testStatus(TestStatus.FAIL)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlPattern)
                                            .build()).build();
    }

    public static UrlChecks logSuccess(String key, CheckType possitive, String urlPattern, String matchedUrl) {
        return TestReportBo.UrlChecks.builder()
                                        .urlLabel(key)
                                        .urlCheckType(possitive)
                                        .testStatus(TestStatus.PASS)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlPattern)
                                            .matchingUrls( List.of(matchedUrl))
                                            .build()).build();
    }   

    public static void logError(TestReportBo trb, String label, TestReportBo.CheckType checkType, Request urlData){
        trb.getUrlChecks().add(TestReportBo.UrlChecks.builder()
                                        .urlLabel(label)
                                        .urlCheckType(checkType)
                                        .testStatus(TestStatus.FAIL)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlData!=null?urlData.url():"X")
                                            .statusCode(urlData!=null?urlData.response().statusText():"X")
                                            .build()).build());

    }

    public static void logLogSuccess(TestReportBo trb, String label, TestReportBo.CheckType checkType, Request urlData){
        trb.getUrlChecks().add(TestReportBo.UrlChecks.builder()
                                        .urlLabel(label)
                                        .urlCheckType(checkType)
                                        .testStatus(TestStatus.PASS)
                                        .urlData(
                                            TestReportBo.UrlData.builder().urlPattern(urlData !=null?urlData.url():"Y")
                                            .statusCode(urlData !=null? urlData.response().statusText():"Y")
                                            .size(urlData !=null?urlData.sizes().toString():"Y")
                                            .timeTakenToLoad(urlData !=null?urlData.timing().toString():"Y")
                                            .build()).build());

    }


}