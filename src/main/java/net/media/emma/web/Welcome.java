package net.media.emma.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Welcome {

    @Autowired private net.media.emma.service.BrowserRemote browserRemote;

    @GetMapping("/welcome")
    public String openUrl() {
        return "welcome";
    }

    @PostMapping("/welcome")
    public void openUrlPost(@RequestParam String url) {

        // browserRemote.injectDom(url, url, null);
        browserRemote.openUrl(url);
    }

    @GetMapping("/welcome2")
    public String domINject() {

        return "welcome2";
      
    }

    @PostMapping("/welcome2")
    public void openAndDOMINject(@RequestParam String url,
                                    @RequestParam String xPath,
                                    @RequestParam String htmlElement,
                                    @RequestParam Boolean append) {
        String forceHtmlElement = "src/main/resources/templates/random.html";
        System.out.println("Calling Dom Inject");
        browserRemote.injectDom(url, xPath, forceHtmlElement, append);
        
    }

    @GetMapping("/welcome3")
        public String response() {
            return "welcome3";
        }
    

    @PostMapping("/welcome3")
    public void scrapeContent(String url){
        System.out.println("Inside welcome 3");
        browserRemote.scrapeContent(url);
    }
    
}