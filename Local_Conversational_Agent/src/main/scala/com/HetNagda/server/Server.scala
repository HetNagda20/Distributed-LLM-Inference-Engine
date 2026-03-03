package com.HetNagda.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.HetNagda.api.DialogueRoutes
import com.HetNagda.services.DialogueProcessor
import com.HetNagda.config.AppSettings
import org.slf4j.LoggerFactory
import scala.util.{Success, Failure}
import scala.concurrent.{ExecutionContextExecutor, Promise}
import scala.concurrent.duration._

// Main server application that initializes and manages the HTTP service
object Server {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Initializing Dialogue System server")

    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "DialogueSystem")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val dialogueProcessor = new DialogueProcessor()
    val routes = new DialogueRoutes(dialogueProcessor)
    val shutdownPromise = Promise[Boolean]()

    logger.info("Starting server on {}:{}", AppSettings.NetworkConfig.bindAddress, AppSettings.NetworkConfig.bindPort)
    val serverBinding = Http().newServerAt(
      AppSettings.NetworkConfig.bindAddress,
      AppSettings.NetworkConfig.bindPort
    ).bind(routes.routes)

    serverBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("Server successfully started at http://{}:{}/", address.getHostString, address.getPort)

        sys.addShutdownHook {
          logger.info("Initiating graceful shutdown")
          binding.terminate(AppSettings.NetworkConfig.gracefulShutdownTimeout.seconds).onComplete { _ =>
            system.terminate()
            shutdownPromise.success(true)
            logger.info("Server shutdown completed")
          }
        }

      case Failure(ex) =>
        logger.error("Server failed to start: {}", ex.getMessage)
        system.terminate()
        shutdownPromise.success(false)
    }

    scala.io.StdIn.readLine()
    logger.info("Shutdown signal received")
    shutdownPromise.future.map { _ => system.terminate() }
  }
}