package com.miniSprint.Digital_lending_loan_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebPageController {

    @GetMapping("/")
    public String homePage() {
        return "index";
    }
}