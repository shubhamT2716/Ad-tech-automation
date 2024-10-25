package net.media.emma.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/harUpload/")
public class HarUploadController {

    @GetMapping
    public ModelAndView listUploadedFiles() throws IOException {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("index");

        modelAndView.addObject("message", "Emma - COP Test Automation Tool");
        return modelAndView;

        // return "index.html";
    }

    @PostMapping
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

}
