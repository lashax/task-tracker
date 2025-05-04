package com.lasha.tasktracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Tracker API")
                        .version("v1.0")
                        .description("A RESTful API for managing projects and tasks with RBAC")
                        .contact(new Contact()
                                .name("Lasha Sherozia")
                                .email("lasha.sheroza@gmail.com")
                        )
                );
    }
}
