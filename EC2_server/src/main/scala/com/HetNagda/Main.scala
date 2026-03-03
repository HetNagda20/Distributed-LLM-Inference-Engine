package com.HetNagda

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.HetNagda.config.ServiceConfig
import com.HetNagda.routes.ClaudeRoutes
import com.HetNagda.services.{ClaudeService, ProtobufService}
import org.slf4j.LoggerFactory
import scala.util.{Success, Failure}
import scala.io.StdIn
import scala.concurrent.duration._

object Main extends App {
  private val logger = LoggerFactory.getLogger(getClass)

  logger.info("Starting Claude service application")

  implicit val system = ActorSystem(Behaviors.empty, "claude-service")
  implicit val executionContext = system.executionContext

  try {
    logger.debug("Loading service configuration")
    val config = ServiceConfig.load()

    logger.debug("Initializing service components")
    val protobufService = new ProtobufService()
    val claudeService = new ClaudeService(config, protobufService)
    val routes = new ClaudeRoutes(claudeService)

    logger.info(s"Starting HTTP server at ${config.http.host}:${config.http.port}")
    val bindingFuture = Http()(system)
      .newServerAt(config.http.host, config.http.port)
      .bind(routes.routes)
      .andThen {
        case Success(binding) =>
          logger.info(s"Server bound to ${config.http.host}:${config.http.port}")
        case Failure(ex) =>
          logger.error("Failed to bind server", ex)
          throw ex
      }

    // Health monitoring
    system.scheduler.scheduleAtFixedRate(30.seconds, 30.seconds)(() => {
      val runtime = Runtime.getRuntime
      val usedMemoryMB = (runtime.totalMemory - runtime.freeMemory) / 1024 / 1024
      logger.debug(s"Health check - Memory usage: $usedMemoryMB MB")
      if (usedMemoryMB > 1024) {
        logger.warn(s"High memory usage: $usedMemoryMB MB")
      }
    })

    logger.info("Server ready for requests - Press RETURN to stop")
    StdIn.readLine("Press RETURN to stop the server...")
    // Keep the app alive for debugging
    Thread.sleep(Long.MaxValue)

    logger.info("Shutting down server")
    bindingFuture
      .flatMap(_.unbind())
      .onComplete {
        case Success(_) =>
          system.terminate()
          logger.info("Server shutdown complete")
        case Failure(ex) =>
          logger.error("Error during shutdown", ex)
          system.terminate()
      }

  } catch {
    case ex: Exception =>
      logger.error("Fatal error during startup", ex)
      system.terminate()
      throw ex
  }
}