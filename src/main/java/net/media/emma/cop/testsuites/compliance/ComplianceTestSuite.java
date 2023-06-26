package net.media.emma.cop.testsuites.compliance;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.cop.components.BrowserFactory;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ComplianceTestSuite {

    @Autowired
    BrowserFactory browserFactory;

    private static String windscribeLogin = "xpath=//button[text()='Login']";
    private static String windscribeUsername = "xpath=//input[@name='username']";
    private static String windscribepassword = "xpath=//input[@name='password']";
    private static String windscribeSkip = "xpath=//div[text()='Skip']";
    private static String username="mohib1233";
    private static String password = "mohib123";
    private static String locationParis = "Paris";
    private static String windscribeLocations = "xpath=//button[@aria-label='Locations']";
    private static String windscribeSearchLocation="xpath=//div//i";
    private static String windscribeSearchbox = "xpath=//input[contains(@class,'css-1c1txoj')]";
    private static String windscribeParisLoc = "xpath=(//div[@aria-label='Paris'])[1]";


    String url = "chrome-extension://hnmpcagpplmpfojmgmnngilcnanddlhb/popup.html";
    String windScribeExtensionPath = System.getProperty("user.dir")+"/extensions/hnmpcagpplmpfojmgmnngilcnanddlhb/3.4.6_0";

    public String getTestResults(ComplianceTestCriteria testData){

        BrowserContext browserContext = browserFactory.getSystemBc(testData.getUserAgent(), testData.getCookies(), getExtensionList());
        Page page = browserContext.newPage();
        openWindscribe(page);

        return "testResults";
    }

    public List<String> getExtensionList() {
        List<String> ext = new ArrayList<>();
        ext.add("--disable-extensions-except=" + windScribeExtensionPath);
        ext.add("--load-extension=" + windScribeExtensionPath);
        return ext;
    }

    public void openWindscribe(Page page){
        page.navigate(url);
        if(page.locator(windscribeLogin).isVisible()){
            page.locator(windscribeLogin).click();
            page.locator(windscribeUsername).click();
            page.locator(windscribeUsername).fill(username);
            page.locator(windscribepassword).click();
            page.locator(windscribepassword).fill(password);
            page.locator(windscribeLogin).click();
            page.locator(windscribeSkip).click();
        }
        page.locator(windscribeLocations).click();
        page.locator(windscribeSearchLocation).click();
        page.locator(windscribeSearchbox).click();
        page.locator(windscribeSearchbox).fill(locationParis);
        page.locator(windscribeParisLoc).click();
    //    page.close();
    }
}
