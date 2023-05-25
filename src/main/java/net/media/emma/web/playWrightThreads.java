package net.media.emma.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;
import net.media.emma.service.BrowserRemote;
import net.media.emma.utils.URLDataProcessing;

@Slf4j
@Controller
public class playWrightThreads {

    @Autowired BrowserRemote browserRemote;
    @Autowired URLDataProcessing urlUtil;

    @GetMapping("/playWrightThreads")
    public String openPlayWrightThreads() {
    
        return "playWrightThreads";
    }

    @PostMapping("/playWrightThreads")
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
    public ModelAndView postUrl(@RequestParam String url) {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("inputUrl");

        String urlData = urlUtil.printURLDetails(browserRemote.getURLAnalyticsFor(url, new String[] {"https://lg1.hb.yahoo.net/log?logid=aplog&itype=YHB"}));

        modelAndView.addObject("message", urlData);
        return modelAndView;  
    }
}