package br.com.fleetcore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fleetCoreOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FleetCore API")
                        .description("Transforming fleet data into intelligent decisions and real results.")
                        .version("1.0.0"));
    }
}
