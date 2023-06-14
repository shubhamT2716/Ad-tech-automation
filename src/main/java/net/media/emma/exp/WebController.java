package net.media.emma.exp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.BrowserRemote;
import net.media.emma.utils.URLDataProcessing;

@Slf4j
@Controller("threadsExp") @RequestMapping("/threadsExp/")
public class WebController {

    @Autowired BrowserRemote browserRemote;
    @Autowired URLDataProcessing urlUtil;
    RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public String openPlayWrightThreads() {
    
        return "playWrightThreads";
    }

    @PostMapping
    public String postPlayWrightThreads(@RequestParam String url1, @RequestParam String url2) {

        browserRemote.openMultiBrowser(url1, url2);
 

        log.info("opening two urls in async mode");
        return "playWrightThreads";
    }

    

    @GetMapping("/urlAnalytics")
    public String inputUrl() {
    
        return "inputUrl";
    }

    @PostMapping("/urlAnalytics")
    public ModelAndView postUrl(@RequestParam String url, @RequestParam String urlPatterns) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("inputUrl");

        String urlData = urlUtil.printURLDetails(browserRemote.getURLAnalyticsFor(url, new String[] {urlPatterns}));

        modelAndView.addObject("message", urlData);
        return modelAndView;  
    }
}