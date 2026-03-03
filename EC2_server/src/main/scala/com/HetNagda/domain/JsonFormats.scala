package com.HetNagda.domain

import spray.json.DefaultJsonProtocol
import org.slf4j.LoggerFactory

// Defines JSON serialization formats for domain models used in the Claude service
object JsonFormats extends DefaultJsonProtocol {
  private val logger = LoggerFactory.getLogger(getClass)

  implicit val modelRequestContextFormat = jsonFormat1(RequestContext)
  implicit val claudeRequestFormat = jsonFormat3(ClaudeRequest)
  implicit val apiGatewayRequestFormat = jsonFormat2(ApiGatewayRequest)
  implicit val apiGatewayResponseFormat = jsonFormat3(ApiGatewayResponse)
  implicit val claudeResponseFormat = jsonFormat1(ClaudeResponse)
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)

  logger.debug("Initialized JSON format definitions for Claude service domain models")
}