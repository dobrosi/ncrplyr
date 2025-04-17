package com.github.dobrosi.demo.controller;

import com.github.dobrosi.demo.configuration.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/")
public class IndexController {
    private final TestConfiguration testConfiguration;

    @Autowired
    public IndexController(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
    }

    @GetMapping()
    public String test() {
        return "Hello " + testConfiguration.getValue();
    }
}
