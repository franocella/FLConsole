package it.unipi.mdwt.flconsole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.*;

import static it.unipi.mdwt.flconsole.utils.Constants.*;

@Configuration
public class ApplicationLogConfig {
    private static final Logger applicationLogger = createLogger();

    @Bean
    public Logger applicationLogger() {
        return applicationLogger;
    }

    private static Logger createLogger() {
        try {
            createLogDirectory();

            Logger logger = Logger.getLogger("ApplicationLogger");
            Path logFilePath = Paths.get(PROJECT_PATH, DIR, LOG_FILE);
            Handler fileHandler = new FileHandler(logFilePath.toString(), LOG_SIZE_LIMIT, LOG_FILE_COUNT, true);
            SimpleFormatter formatterTxt = new SimpleFormatter();
            fileHandler.setFormatter(formatterTxt);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            logger.log(Level.CONFIG, "Logger: {0} created.", logger.getName());
            return logger;
        } catch (IOException | SecurityException e) {
            Logger.getLogger(ApplicationLogConfig.class.getName())
                    .log(Level.SEVERE, "Error during Logger creation", e);
            throw new RuntimeException(e);
        }
    }
    private static void createLogDirectory() throws IOException {
        Path logDirectoryPath = Paths.get(PROJECT_PATH, DIR);
        if (!Files.exists(logDirectoryPath)) {
            try {
                Files.createDirectories(logDirectoryPath);
            } catch (IOException e) {
                throw new IOException("Failed to create log directory: " + logDirectoryPath, e);
            }
        }
    }
}

