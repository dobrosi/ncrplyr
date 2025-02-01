package com.github.dobrosi.ncrplyr;

import com.github.dobrosi.api.ExtendedDelugeApi;
import com.github.dobrosi.imdbclient.ImdbClient;
import com.github.dobrosi.ncrplyr.configuration.DelugeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class Main {
    private final DelugeConfiguration delugeConfiguration;

    public Main(
        final DelugeConfiguration delugeConfiguration) {

        this.delugeConfiguration = delugeConfiguration;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public ExtendedDelugeApi extendedDelugeApi() {
        return new ExtendedDelugeApi(delugeConfiguration.getBaseUrl(), delugeConfiguration.getPassword());
    }

    @Bean
    public ImdbClient imdbClient() {
        return new ImdbClient();
    }
}