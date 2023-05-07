package net.media.emma.web;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ch.qos.logback.core.model.Model;

@Controller
public class HarUploadController {

    // @GetMapping("/")
	// public String listUploadedFiles(Model model) throws IOException {

	// 	return "index.html";
	// }

    // @PostMapping("/")
	// public String handleFileUpload(@RequestParam("file") MultipartFile file,
	// 		RedirectAttributes redirectAttributes) {

	// 	redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");

	// 	return "redirect:/";
	// }
    
}
