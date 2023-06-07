package net.media.emma.cop.testsuites.pagetests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller("pageLevelTestsController") @RequestMapping("/pageLevelTests/")
public class WebController {

    @Autowired PageLevelTests pageLevelTests;

    // @GetMapping("/domInject")
    // public String domINject() {

    //     return "welcome2";
      
    // }

    // @PostMapping("/welcome2")
    // public void openAndDOMINject(@RequestParam String url,
    //                                 @RequestParam String xPath,
    //                                 @RequestParam String htmlElement,
    //                                 @RequestParam Boolean append) {
    //     String forceHtmlElement = "src/main/resources/templates/random.html";
    //     System.out.println("Calling Dom Inject");
    //     browserRemote.injectDom(url, xPath, forceHtmlElement, append);
        
    // }

    @GetMapping
        public String response() {
            return "contentScrape";
        }
    

    @PostMapping
    public ModelAndView scrapeContent(@RequestParam String url){

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("contentScrape");

        modelAndView.addObject("message", pageLevelTests.scrapeContent(url));
        return modelAndView;  

    }
    
}
