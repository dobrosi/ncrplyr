package com.github.dobrosi.ncrplyr.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "digi-online-client")
public class DigiOnlineClientConfiguration {
    public String getUser() {
        return user;
    }

    public void setUser(final String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public boolean isHeadless() {
        return headless;
    }

    public void setHeadless(final boolean headless) {
        this.headless = headless;
    }

    private String user;
    private String password;
    private boolean headless;
}
