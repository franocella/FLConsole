package it.unipi.mdwt.flconsole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class ExecutorConfig {
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(20);
    }
}
