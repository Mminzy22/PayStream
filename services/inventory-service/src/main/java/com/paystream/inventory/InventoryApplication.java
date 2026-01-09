package com.paystream.inventory;

import com.paystream.core.exception.PayStreamExceptionAdvice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@ConfigurationPropertiesScan
@EnableDiscoveryClient
@SpringBootApplication
@Import(PayStreamExceptionAdvice.class)
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
