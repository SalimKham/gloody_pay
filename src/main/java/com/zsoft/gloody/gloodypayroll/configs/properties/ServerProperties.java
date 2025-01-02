package com.zsoft.gloody.gloodypayroll.configs.properties;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URL;

@Data
@Configuration
@ConfigurationProperties(prefix = "com.zsoft.gloody")
public class ServerProperties {
    private ConfigurationProperties configuration;

    @Data
    public static class ConfigurationProperties {
        private String apiUrl;
        private String fileStorePath;

        public URL getUrl() throws MalformedURLException {
            if (this.apiUrl != null) {
                return new URL(this.apiUrl);
            }
            return null;
        }

        @PostConstruct
        public void afterConstruct() {
            System.out.println(this);
        }
    }
}
