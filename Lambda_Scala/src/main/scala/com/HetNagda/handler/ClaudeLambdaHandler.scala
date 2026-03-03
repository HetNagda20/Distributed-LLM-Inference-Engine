package com.HetNagda.handler

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.{APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent}

import com.HetNagda.services.{ClaudeService, MessageTransformService}

import scala.jdk.CollectionConverters._
import com.HetNagda.config.ServiceConfiguration
import com.HetNagda.proto.lambda.RequestToClaude

import scala.util.{Failure, Success, Try}
import java.util.Base64

// AWS Lambda handler for processing API requests that we sent through the
class ClaudeLambdaHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent] {

  private val messageService = new MessageTransformService()
  private val claudeService = new ClaudeService(ServiceConfiguration.claudeSettings)

  override def handleRequest(
                              apiRequest: APIGatewayProxyRequestEvent,
                              lambdaContext: Context
                            ): APIGatewayProxyResponseEvent = {

    val lambdaLogger = lambdaContext.getLogger

    try {
      val encodedInput = Option(apiRequest)
        .flatMap(req => Option(req.getQueryStringParameters))
        .flatMap(params => Option(params.get("query")))
        .getOrElse(throw new IllegalArgumentException("Missing required 'query' parameter"))

      (for {
        userRequest <- messageService.base64ToProto[RequestToClaude](encodedInput)
        claudeResponse <- claudeService.processRequest(userRequest)
        encodedResponse <- Try(Base64.getEncoder.encodeToString(claudeResponse.toByteArray))
      } yield encodedResponse) match {
        case Success(processedResponse) =>
          new APIGatewayProxyResponseEvent()
            .withStatusCode(200)
            .withBody(processedResponse)
            .withHeaders(Map(
              "Content-Type" -> "application/x-protobuf",
              "X-Request-ID" -> Option(apiRequest.getRequestContext)
                .map(_.getRequestId)
                .getOrElse("unknown")
            ).asJava)

        case Failure(error) =>
          lambdaLogger.log(s"Error processing request: ${error.getMessage}")
          new APIGatewayProxyResponseEvent()
            .withStatusCode(400)
            .withBody(s"Error: ${error.getMessage}")
      }

    } catch {
      case e: IllegalArgumentException =>
        lambdaLogger.log(s"Bad Request: ${e.getMessage}")
        new APIGatewayProxyResponseEvent()
          .withStatusCode(400)
          .withBody(s"Bad Request: ${e.getMessage}")

      case e: Exception =>
        lambdaLogger.log(s"Error processing request: ${e.getMessage}")
        e.printStackTrace()
        new APIGatewayProxyResponseEvent()
          .withStatusCode(500)
          .withBody(s"Internal Server Error: ${e.getMessage}")
    }
  }
}