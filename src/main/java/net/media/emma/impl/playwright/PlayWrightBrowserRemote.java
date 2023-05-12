package net.media.emma.impl.playwright;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Route;

import net.media.emma.service.BrowserRemote;

public class PlayWrightBrowserRemote implements BrowserRemote {

    // Browser browser = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
  
    @Autowired Logger logger;

    public void openUrl(String url){
        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();

        page.navigate(url);
        page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
    }

    @Override
    public void mockRequest() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mockRequest'");
    }

    @Override
    public void mockResponse() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mockResponse'");
    }

    @Override
    public void injectScript(String script) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'injectScript'");
    }

    @Override
    public void injectDom(String url, String xPath, String htmlElement, Boolean append) {

        // logger.info("sadf");

        Page page = Playwright.create().chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newPage();

        // page.onResponse(
        //    response -> {

        //         System.out.println(response.allHeaders().toString());
              
        //     }
        // );

        page.route("https://www.engadget.com/?headerbidding=1",  route -> {

          
            // APIResponse apiResponse = route.fetch();
            // String responseBody = new String(apiResponse.body(), StandardCharsets.UTF_8);

            // System.out.println(responseBody);

            route.fulfill(new Route.FulfillOptions().setBody("""

<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Bid Reuse</title>
<script type="text/javascript" src="https://hb.yahoo.net/bidexchange.js?cid=8YHBL12EX&version=9.1&dn=www.engadget.com&ysec=index"></script>
 

  </head>
  <body>
    <div class="heading">
      <h1>Bid Reuse Test Page</h1>
    </div>
    <hr>
    <h2 class="th">RATS TEST</h2>
    <div class="bidReuse" id="LB-MULTI_ATF" >
            <!-- /45361917/rats-test-bidreuse -->
             <script>
                  googletag.cmd.push(function() { googletag.display('LB-MULTI_ATF'); });
             </script>
</div>
    </div>
  </body>
</html>

            """));
            
        });

        page.navigate(url);
        //page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("example.png")));
     
    }
    
}
