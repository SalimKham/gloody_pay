package com.zsoft.gloody.gloodypayroll;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication(exclude = RepositoryRestMvcAutoConfiguration.class)
@ComponentScan(basePackages = {"com.zsoft.gloody.gloodypayroll.*"})
@EnableConfigurationProperties
@EnableScheduling
public class GloodyPayrollApplication {
    private static void logApplicationStartup(Environment env) {
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }
        String serverPort = env.getProperty("server.port");
        String contextPath = env.getProperty("server.servlet.context-path");
        if (!StringUtils.hasText(contextPath)) {
            contextPath = "/";
        }
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        String[] profiles = env.getActiveProfiles();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n" +
                        "--------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol, serverPort, contextPath, protocol, hostAddress, serverPort, contextPath, profiles);
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(GloodyPayrollApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(new Locale("fr", "DZ"));
        log.info("Set default values [Timezone: {}, Locale: {}]", TimeZone.getDefault(), Locale.getDefault());
    }
}
