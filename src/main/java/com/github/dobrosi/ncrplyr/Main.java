package com.github.dobrosi.ncrplyr;

import com.github.dobrosi.api.ExtendedDelugeApi;
import com.github.dobrosi.imdbclient.DigiOnlinePlaywrightClient;
import com.github.dobrosi.imdbclient.ImdbClient;
import com.github.dobrosi.ncrplyr.configuration.DigiOnlineClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class Main {
    @Autowired
    private DigiOnlineClientConfiguration digiOnlineClientConfiguration;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public ExtendedDelugeApi extendedDelugeApi() {
        return new ExtendedDelugeApi("localhost", 8112, "secret");
    }

    @Bean
    public ImdbClient imdbClient() {
        return new ImdbClient();
    }

    @Bean
    public DigiOnlinePlaywrightClient digiOnlineClient() {
        return DigiOnlinePlaywrightClient.create(digiOnlineClientConfiguration.getUser(),
                                                 digiOnlineClientConfiguration.getPassword()).withHeadless(digiOnlineClientConfiguration.isHeadless());
    }
}