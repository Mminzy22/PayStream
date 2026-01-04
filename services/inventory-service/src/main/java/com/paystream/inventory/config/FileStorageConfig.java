package com.paystream.inventory.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties(prefix = "file.upload-dir")
public class FileStorageConfig {

    private final String selectedPath;

    @ConstructorBinding
    public FileStorageConfig(Map<String, String> os) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            this.selectedPath = os.get("window");
        } else if (osName.contains("mac")) {
            this.selectedPath = os.get("mac");
        } else {
            this.selectedPath = os.get("linux");
        }
    }

    public String getBasePath() {
        return this.selectedPath;
    }
}
