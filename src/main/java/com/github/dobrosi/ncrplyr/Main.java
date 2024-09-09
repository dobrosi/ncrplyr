package com.github.dobrosi.ncrplyr;

import com.github.dobrosi.api.ExtendedDelugeApi;
import com.github.dobrosi.imdbclient.ImdbClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Main {
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
}