package com.yugabyte.yugastore.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping({"/login", "/register", "/merchant/signup", "/item/{asin}",
            "/{tenantSlug:[a-z0-9-]+}", "/{tenantSlug:[a-z0-9-]+}/signup",
            "/{tenantSlug:[a-z0-9-]+}/item/{asin}", "/{tenantSlug:[a-z0-9-]+}/{category:Books|Music|Beauty|Electronics}"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}