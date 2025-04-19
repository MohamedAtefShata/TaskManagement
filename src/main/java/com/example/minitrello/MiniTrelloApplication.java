package com.example.minitrello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Mini Trello API",
        version = "1.0",
        description = "API for Mini Trello application to manage tasks within projects"
))
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class MiniTrelloApplication {
    public static void main(String[] args) {
        SpringApplication.run(MiniTrelloApplication.class, args);
    }
}