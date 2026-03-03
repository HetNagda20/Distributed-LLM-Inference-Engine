package com.HetNagda.services

import com.HetNagda.config.ServiceConfig
import com.HetNagda.domain._
import com.HetNagda.domain.JsonFormats._
import com.HetNagda.proto.lambda.{RequestToClaude, ResponseFromClaude}
import sttp.client3._
import sttp.client3.sprayJson._
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import org.slf4j.LoggerFactory

class ClaudeService(config: ServiceConfig, protobufService: ProtobufService)(implicit ec: ExecutionContext) {
  private val logger = LoggerFactory.getLogger(getClass)
  private val backend = HttpURLConnectionBackend()

  def processQuery(request: ClaudeRequest): Future[String] = {
    logger.info("Starting Claude query processing")
    logger.debug(s"Processing request with input length: ${request.inputText.length}")

    val protoRequest = {
      logger.debug("Creating RequestToClaude with parameters")
      val temp = request.temperature.getOrElse(config.claude.defaultTemperature)
      val tokens = request.maxTokens.getOrElse(config.claude.defaultMaxTokens)

      logger.trace(s"Using temperature: $temp, max tokens: $tokens")
      RequestToClaude(
        inputText = request.inputText,
        parameters = Map(
          "temperature" -> temp.toString,
          "max_tokens" -> tokens.toString
        )
      )
    }

    for {
      base64Request <- {
        logger.debug("Encoding proto request to base64")
        Future.fromTry(protobufService.encodeToBase64(protoRequest))
      }

      apiRequest = {
        val requestId = UUID.randomUUID().toString
        logger.debug(s"Creating API Gateway request with ID: $requestId")

        ApiGatewayRequest(
          queryStringParameters = Map("query" -> base64Request),
          requestContext = RequestContext(requestId)
        )
      }

      response <- {
        logger.info(s"Sending request to Claude API Gateway: ${config.apiGatewayUrl}")
        Future {
          basicRequest
            .post(uri"${config.apiGatewayUrl}")
            .header("Content-Type", config.claude.headers("Content-Type"))
            .body(apiRequest.toJson.compactPrint)
            .response(asJson[ApiGatewayResponse])
            .send(backend)
            .body
            .fold(
              error => {
                logger.error("API Gateway request failed", new RuntimeException(error))
                throw new RuntimeException(error)
              },
              success => success
            )
        }
      }

      decodedResponse <- {
        logger.debug("Decoding API Gateway response from base64")
        Future.fromTry(
          protobufService.decodeFromBase64[ResponseFromClaude](response.body)
        )
      }
    } yield {
      logger.info("Successfully completed Claude query processing")
      logger.debug(s"Generated response length: ${decodedResponse.outputText.length}")
      decodedResponse.outputText
    }
  }
}