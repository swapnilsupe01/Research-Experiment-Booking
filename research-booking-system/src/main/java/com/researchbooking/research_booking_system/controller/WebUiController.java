
package com.researchbooking.research_booking_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebUiController {

    @GetMapping("/ui")
    public String home() {
        return "index";
    }
}
