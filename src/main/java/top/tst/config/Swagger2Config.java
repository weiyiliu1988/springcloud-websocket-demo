package top.tst.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class Swagger2Config {

	@Value("${swagger.show}")
	private boolean swaggerShow;

	@Bean
	public Docket createRestApi() {
		if (swaggerShow) {
			return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
					.apis(RequestHandlerSelectors.basePackage("top.tst.controller"))
					.paths(PathSelectors.any()).build();
		}
		return new Docket(DocumentationType.SWAGGER_2).select().paths(PathSelectors.none()).build();
	}

	private ApiInfo apiInfo() {
		return new ApiInfoBuilder().title("websocket").description("[websocket]微服务接口")
				.termsOfServiceUrl("http://localhost:9001").version("1.0").build();
	}

}
