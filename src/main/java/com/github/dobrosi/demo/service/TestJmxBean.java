package com.github.dobrosi.demo.service;

import java.time.LocalDateTime;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource(
    objectName="bean:name=testBean",
    description="My Managed Bean")
public class TestJmxBean {
    @ManagedOperation(description="Say hello")
    public String sayHello(String name) {
        return "Hello " + name + "!";
    }

    @ManagedOperation
    public LocalDateTime getDate() {
        return LocalDateTime.now();
    }
}
