package com.HetNagda.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.HetNagda.models.DialogueFormats._
import com.HetNagda.models.SingleResponseFormats._
import com.HetNagda.models.{DialogueRequest, DialogueResponse, SingleResponseRequest, SingleResponseResponse}
import com.HetNagda.services.DialogueProcessor
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// Main routes handler for the dialogue system
class DialogueRoutes(dialogueProcessor: DialogueProcessor)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)

  val routes: Route = {
    pathPrefix("api") {
      concat(
        path("dialogue") {
          post {
            entity(as[String]) { body =>
              try {
                logger.info("Starting new dialogue session")
                val request = Json.parse(body).as[DialogueRequest]
                logger.debug("Processing dialogue with prompt: {}", request.prompt)

                val dialogueResult = dialogueProcessor.processDialogue(
                  request.prompt,
                  request.maxTokens,
                  request.maxTurns
                )

                request.exportPath.foreach { path =>
                  logger.info("Exporting dialogue to: {}", path)
                  dialogueProcessor.exportDialogue(dialogueResult, path)
                }

                val response = DialogueResponse(
                  iterationCount = dialogueResult.length,
                  meanProcessingTime = dialogueResult.map(_.latencyMs).sum / dialogueResult.length,
                  dialogue = dialogueResult
                )

                logger.info("Dialogue completed - Turns: {}, Avg Time: {}ms",
                  dialogueResult.length, response.meanProcessingTime)
                complete(StatusCodes.OK -> Json.toJson(response).toString)
              } catch {
                case e: Exception =>
                  logger.error("Dialogue processing failed: {}", e.getMessage, e)
                  complete(StatusCodes.InternalServerError -> s"Error: ${e.getMessage}")
              }
            }
          }
        },
        path("ollama") {
          post {
            entity(as[String]) { body =>
              try {
                logger.info("Processing Ollama request")
                val request = Json.parse(body).as[SingleResponseRequest]
                val startTime = System.currentTimeMillis()

                val response = dialogueProcessor.getSecondaryResponse(request.prompt, request.maxTokens)
                val latency = System.currentTimeMillis() - startTime

                val apiResponse = SingleResponseResponse(
                  response = response,
                  latencyMs = latency
                )

                logger.info("Ollama request completed in {}ms", latency)
                complete(StatusCodes.OK -> Json.toJson(apiResponse).toString)
              } catch {
                case e: Exception =>
                  logger.error("Ollama request failed: {}", e.getMessage, e)
                  complete(StatusCodes.InternalServerError -> s"Error: ${e.getMessage}")
              }
            }
          }
        },
        path("bedrock") {
          post {
            entity(as[String]) { body =>
              try {
                logger.info("Processing Bedrock request")
                val request = Json.parse(body).as[SingleResponseRequest]
                val startTime = System.currentTimeMillis()

                onSuccess(dialogueProcessor.getPrimaryResponse(request.prompt, request.maxTokens)) { response =>
                  val latency = System.currentTimeMillis() - startTime

                  val apiResponse = SingleResponseResponse(
                    response = response,
                    latencyMs = latency
                  )

                  logger.info("Bedrock request completed in {}ms", latency)
                  complete(StatusCodes.OK -> Json.toJson(apiResponse).toString)
                }
              } catch {
                case e: Exception =>
                  logger.error("Bedrock request failed: {}", e.getMessage, e)
                  complete(StatusCodes.InternalServerError -> s"Error: ${e.getMessage}")
              }
            }
          }
        }
      )
    }
  }
}
