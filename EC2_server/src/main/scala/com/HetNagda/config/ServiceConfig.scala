package com.HetNagda.config

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

// Configuration for HTTP server settings
case class HttpConfig(
                       host: String,               // Server host address
                       port: Int                   // Server port number
                     )

// Configuration for Claude LLM settings
case class ClaudeConfig(
                         defaultTemperature: Double,  // Default sampling temperature for text generation
                         defaultMaxTokens: Int,       // Default maximum tokens to generate
                         headers: Map[String, String] // HTTP headers for API requests
                       )

// Main service configuration
case class ServiceConfig(
                          apiGatewayUrl: String,      // URL for the API Gateway endpoint
                          http: HttpConfig,           // HTTP server configuration
                          claude: ClaudeConfig        // Claude model configuration
                        )

object ServiceConfig {
  private val logger = LoggerFactory.getLogger(getClass)

  def load(): ServiceConfig = {
    logger.info("Loading service configuration")
    val config = ConfigFactory.load()

    logger.debug("Parsing configuration values")
    val serviceConfig = ServiceConfig(
      apiGatewayUrl = config.getString("service.api-gateway-url"),
      http = HttpConfig(
        host = config.getString("http.host"),
        port = config.getInt("http.port")
      ),
      claude = ClaudeConfig(
        defaultTemperature = config.getDouble("claude.default-temperature"),
        defaultMaxTokens = config.getInt("claude.default-max-tokens"),
        headers = Map(
          "Content-Type" -> config.getString("claude.headers.content-type")
        )
      )
    )

    logger.info("Service configuration loaded successfully")
    logger.debug(s"HTTP server will bind to ${serviceConfig.http.host}:${serviceConfig.http.port}")
    logger.debug(s"Using API Gateway URL: ${serviceConfig.apiGatewayUrl}")

    serviceConfig
  }
}