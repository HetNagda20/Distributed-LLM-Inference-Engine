package com.HetNagda.services

import com.HetNagda.config.AppSettings
import com.HetNagda.models.DialogueIteration
import io.github.ollama4j.{OllamaAPI, utils => ollamaUtils}
import org.slf4j.LoggerFactory
import play.api.libs.json._
import sttp.client3._
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

// Main processor for dialogue interactions between remote and local AI models
class DialogueProcessor {
  private val logger = LoggerFactory.getLogger(getClass)
  private val backend = HttpURLConnectionBackend()
  private val localAI = initializeLocalAI()

  private def initializeLocalAI(): OllamaAPI = {
    logger.info("Initializing Ollama API with endpoint: {}", AppSettings.LocalAI.endpoint)
    val api = new OllamaAPI(AppSettings.LocalAI.endpoint)
    api.setRequestTimeoutSeconds(AppSettings.LocalAI.timeoutDuration)
    api
  }

  def getPrimaryResponse(input: String, maxTokens: Option[Int] = None): Future[String] = {
    logger.debug("Sending request to primary AI service with input length: {}", input.length)
    val requestBody = Json.obj(
      "inputText" -> input,
      "temperature" -> AppSettings.RemoteAI.variability,
      "maxTokens" -> maxTokens.getOrElse(AppSettings.RemoteAI.tokenLimit).asInstanceOf[Int]
    )

    val request = basicRequest
      .post(uri"http://${AppSettings.Backend.endpoint}:${AppSettings.Backend.port}/api/v1/generate")
      .header("Content-Type", "application/json")
      .body(requestBody.toString())

    Future {
      backend.send(request).body match {
        case Right(jsonString) =>
          try {
            val json = Json.parse(jsonString.toString)
            (json \ "generatedText").as[String]
          } catch {
            case e: Exception =>
              logger.error("Failed to parse primary AI response", e)
              throw e
          }
        case Left(error) =>
          logger.error("Primary AI service request failed", error)
          throw new Exception(s"Primary AI service error: $error")
      }
    }
  }

  def getSecondaryResponse(context: String, maxTokens: Option[Int] = None): String = {
    logger.debug("Requesting secondary AI response for context length: {}", context.length)
    val enhancedPrompt = s"Provide a response to the following context: $context"

    val options = new java.util.HashMap[String, Object]()
    maxTokens.foreach(tokens => options.put("num_predict", tokens.asInstanceOf[Object]))

    Try {
      val result = localAI.generate(
        AppSettings.LocalAI.modelVersion,
        enhancedPrompt,
        false,
        new ollamaUtils.Options(options)
      )
      result.getResponse
    } match {
      case Success(response) => response
      case Failure(e) =>
        logger.error("Secondary AI failed: {}", e.getMessage)
        throw new Exception(s"Secondary AI error: ${e.getMessage}")
    }
  }

  def processDialogue(initialPrompt: String, maxTokens: Option[Int] = None, maxTurns: Option[Int] = None): Seq[DialogueIteration] = {
    var iterations = Seq.empty[DialogueIteration]
    var currentPrompt = initialPrompt
    val startTime = LocalDateTime.now()

    val iterationLimit = maxTurns.getOrElse(AppSettings.DialogueConfig.iterationLimit)
    logger.info("Starting dialogue processing with limit of {} turns", iterationLimit)

    while (
      iterations.length < iterationLimit &&
        startTime.plusMinutes(AppSettings.DialogueConfig.sessionTimeout).isAfter(LocalDateTime.now())
    ) {
      val iterationStart = System.currentTimeMillis()
      val currentIteration = iterations.length + 1

      try {
        logger.debug("Starting iteration {} of dialogue", currentIteration)

        val primaryResponse = Await.result(
          getPrimaryResponse(currentPrompt, maxTokens),
          AppSettings.RemoteAI.requestTimeout.seconds
        )
        val secondaryResponse = getSecondaryResponse(primaryResponse, maxTokens)

        iterations = iterations :+ DialogueIteration(
          LocalDateTime.now(),
          currentPrompt,
          primaryResponse,
          secondaryResponse,
          System.currentTimeMillis() - iterationStart
        )

        currentPrompt = secondaryResponse
        logger.info("Completed iteration {} in {}ms", currentIteration, iterations.last.latencyMs)
      } catch {
        case e: Exception =>
          logger.error("Failed during iteration {}", currentIteration, e)
          throw e
      }
    }

    logger.info("Dialogue processing completed with {} iterations", iterations.length)
    iterations
  }

  def exportDialogue(dialogue: Seq[DialogueIteration], filepath: String): Unit = {
    logger.info("Beginning dialogue export to CSV: {}", filepath)

    val header = "timestamp,speaker,message,response_time_ms\n"
    val rows = dialogue.flatMap { iteration =>
      Seq(
        s"${iteration.timestamp},Claude,${sanitize(iteration.primaryResponse)},${iteration.latencyMs}",
        s"${iteration.timestamp},Llama,${sanitize(iteration.secondaryResponse)},${iteration.latencyMs}"
      )
    }.mkString("\n")

    Files.write(
      Paths.get(filepath),
      (header + rows).getBytes(StandardCharsets.UTF_8)
    )
    logger.info("Successfully exported dialogue to CSV")
  }

  private def sanitize(text: String): String = {
    "\"" + text.replace("\"", "\"\"") + "\""
  }
}