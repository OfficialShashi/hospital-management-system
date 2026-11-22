
package com.hms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hospitalManagementOpenAPI() {

        final String securitySchemeName = "Bearer Authentication";

        return new OpenAPI()
                .info(new Info()
                        .title("Hospital Management System API")
                        .description(
                                "REST APIs for Hospital Management System. "
                                + "Includes Authentication, Users, Doctors, "
                                + "Patients, Appointments, Medical Records, "
                                + "Prescriptions, Laboratory, Pharmacy, "
                                + "Billing, Payments, Dashboard and Reports."
                        )
                        .version("1.0.0")
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token here.")
                        )
                );
    }
}
