package com.cbap.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CBAP OSS Application Launcher
 * 
 * This is the main entry point for the Composable Business Application Platform.
 * The application is metadata-driven and provides a runtime for business applications.
 */
@SpringBootApplication
public class CbapApplication {

    public static void main(String[] args) {
        SpringApplication.run(CbapApplication.class, args);
    }
}
