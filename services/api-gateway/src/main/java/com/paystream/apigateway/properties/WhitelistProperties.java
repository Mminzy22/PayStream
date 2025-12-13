package com.paystream.apigateway.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "whitelist")
public class WhitelistProperties {

    private List<String> paths = new ArrayList<>();

    public String[] getPathsToArray() {
        return paths.toArray(String[]::new);
    }
}
