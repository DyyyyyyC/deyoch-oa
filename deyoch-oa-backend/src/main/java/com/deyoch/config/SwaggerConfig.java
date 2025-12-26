package com.deyoch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger配置类
 * 使用SpringDoc OpenAPI 3.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档的基本信息
     * @return OpenAPI对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Deyoch OA 系统 API 文档")
                        .version("1.0.0")
                        .description("Deyoch OA 系统的RESTful API文档")
                        .contact(new Contact()
                                .name("Deyoch开发团队")
                                .email("contact@deyoch.com")
                                .url("https://www.deyoch.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
