package com.example.plantuml.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Web controller for serving HTML pages and forms.
 * 
 * @author Principal Engineer
 */
@Controller
@RequestMapping("/")
public class PlantUMLWebController {

    /**
     * Serve the main PlantUML diagram generator page.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "PlantUML Diagram Generator");
        model.addAttribute("pageType", "generator");
        return "index";
    }
    
    /**
     * Serve the markdown processor page.
     */
    @GetMapping("/process-markdown")
    public String processMarkdown(Model model) {
        model.addAttribute("title", "Markdown PlantUML Processor");
        model.addAttribute("pageType", "processor");
        return "process-markdown";
    }
    
    /**
     * Serve the about page.
     */
    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About PlantUML Server");
        model.addAttribute("pageType", "about");
        return "about";
    }
}
