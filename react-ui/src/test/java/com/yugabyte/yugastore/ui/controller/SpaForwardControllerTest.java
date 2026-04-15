package com.yugabyte.yugastore.ui.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SpaForwardControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new SpaForwardController()).build();
    }

    @Test
    void forwardItemRouteToIndexHtml() throws Exception {
        mockMvc.perform(get("/item/0000031852"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardTenantStorefrontRouteToIndexHtml() throws Exception {
        mockMvc.perform(get("/northwind-books"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardTenantSignupRouteToIndexHtml() throws Exception {
        mockMvc.perform(get("/northwind-books/signup"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardTenantItemRouteToIndexHtml() throws Exception {
        mockMvc.perform(get("/northwind-books/item/0000031852"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    void forwardTenantCategoryRouteToIndexHtml() throws Exception {
        mockMvc.perform(get("/northwind-books/Books"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}