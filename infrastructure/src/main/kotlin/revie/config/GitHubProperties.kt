package revie.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "github")
data class GitHubProperties(
  var token: String = "",
  var apiUrl: String = "https://api.github.com"
)