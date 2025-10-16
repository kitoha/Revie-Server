package revie.revie.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebConfig: WebFluxConfigurer {

  override fun addCorsMappings(registry: CorsRegistry) { // 임시 CORS 설정
    registry.addMapping("/**")
      .allowedOrigins("http://localhost:3000", "http://localhost:5173")
      .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
      .allowedHeaders("*")
      .allowCredentials(true)
      .maxAge(3600)
  }
}
