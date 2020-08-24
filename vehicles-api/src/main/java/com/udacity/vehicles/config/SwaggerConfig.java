package com.udacity.vehicles.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Autowired
    private Environment environment;

    @Bean
    public Docket api() {
        String host = environment.getProperty("springfox.swagger2.host");

        return new Docket(DocumentationType.SWAGGER_2)
                .host(host)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "Vehicle API",
                "This API describes the exposed endpoints for a car website.",
                "1.0",
                "http://www.udacity.com/tos",
                new Contact("Ikhiloya Imokhai", "https://github.com/ikhiloya", "imokhaiikhilya@gmail.com"),
                "License of API", "http://www.udacity.com/license", Collections.emptyList());
    }

}
