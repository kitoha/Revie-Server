package revie.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientConfig(
  private val gitHubProperties: GitHubProperties
) {

  @Bean
  fun gitHubWebClient(
    gitHubProperties: GitHubProperties,
    webClientBuilder: WebClient.Builder
  ): WebClient {
    return webClientBuilder
      .baseUrl(gitHubProperties.apiUrl)
      .defaultHeader("Authorization", "Bearer ${gitHubProperties.token}")
      .defaultHeader("Accept", "application/vnd.github.v3+json")
      .clientConnector(ReactorClientHttpConnector(createHttpClient()))
      .build()
  }

  private fun createHttpClient(): HttpClient {
    return HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
      .responseTimeout(Duration.ofSeconds(30))
      .doOnConnected { conn ->
        conn.addHandlerLast(ReadTimeoutHandler(30, TimeUnit.SECONDS))
        conn.addHandlerLast(WriteTimeoutHandler(30, TimeUnit.SECONDS))
      }
  }
}