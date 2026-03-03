package com.HetNagda.config

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

// Central configuration manager for application settings loaded from application.conf
object AppSettings {
  private val logger = LoggerFactory.getLogger(getClass)
  private val settings = ConfigFactory.load()
  logger.info("Loading application configuration")

  // Configuration for local Ollama AI service
  object LocalAI {
    private val localConfig = settings.getConfig("ollama")
    val endpoint: String = localConfig.getString("host")
    val modelVersion: String = localConfig.getString("model")
    val timeoutDuration: Int = localConfig.getInt("request-timeout-seconds")
    logger.debug("Loaded Ollama configuration - endpoint: {}, model: {}", endpoint, modelVersion)
  }

  // Backend service configuration
  object Backend {
    private val backendConfig = settings.getConfig("service")
    val endpoint: String = backendConfig.getString("host")
    val port: Int = backendConfig.getInt("port")
    logger.debug("Loaded backend configuration - endpoint: {}:{}", endpoint, port)
  }

  // Dialogue processing parameters
  object DialogueConfig {
    private val dialogueConfig = settings.getConfig("conversation")
    val iterationLimit: Int = dialogueConfig.getInt("max-turns")
    val sessionTimeout: Int = dialogueConfig.getInt("timeout-minutes")
    logger.debug("Loaded dialogue configuration - max turns: {}, timeout: {} minutes", iterationLimit, sessionTimeout)
  }

  // Remote AI service settings
  object RemoteAI {
    private val remoteConfig = settings.getConfig("cloud-service")
    val variability: Double = remoteConfig.getDouble("temperature")
    val tokenLimit: Int = remoteConfig.getInt("max-tokens")
    val requestTimeout: Int = remoteConfig.getInt("request-timeout-seconds")
    logger.debug("Loaded remote AI configuration - temperature: {}, max tokens: {}", variability, tokenLimit)
  }

  // Server networking configuration
  object NetworkConfig {
    private val networkConfig = settings.getConfig("server")
    val bindAddress: String = networkConfig.getString("host")
    val bindPort: Int = networkConfig.getInt("port")
    val gracefulShutdownTimeout: Int = networkConfig.getInt("termination-timeout-seconds")
    logger.debug("Loaded network configuration - binding to {}:{}", bindAddress, bindPort)
  }
}