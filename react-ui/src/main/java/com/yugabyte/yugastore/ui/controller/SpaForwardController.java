package com.yugabyte.yugastore.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/login", "/register", "/merchant/signup"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}