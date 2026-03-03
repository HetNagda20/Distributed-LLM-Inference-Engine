package com.HetNagda.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import com.HetNagda.domain._
import com.HetNagda.domain.JsonFormats._
import com.HetNagda.services.ClaudeService
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.concurrent.ExecutionContext
import org.slf4j.LoggerFactory

class ClaudeRoutes(claudeService: ClaudeService)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  val routes = {
    pathPrefix("api" / "v1") {
      path("generate") {
        post {
          entity(as[ClaudeRequest]) { request =>
            logger.info(s"Received Claude generation request, input length: ${request.inputText.length}")

            onComplete(claudeService.processQuery(request)) {
              case scala.util.Success(response) =>
                logger.info("Successfully processed Claude request")
                complete(ClaudeResponse(response))

              case scala.util.Failure(ex) =>
                logger.error("Failed to process Claude request", ex)
                complete((StatusCodes.InternalServerError, ErrorResponse(ex.getMessage)))
            }
          }
        }
      }
    }
  }
}