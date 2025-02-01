package com.github.dobrosi.ncrplyr.configuration;

import java.net.URL;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deluge")
@Data
public class DelugeConfiguration {
    private URL baseUrl;
    private String password;
}
