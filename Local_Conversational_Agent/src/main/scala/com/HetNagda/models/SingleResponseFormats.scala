package com.HetNagda.models

import org.slf4j.LoggerFactory
import play.api.libs.json._

// Model for configuring and handling single AI response requests
case class SingleResponseRequest(
                                  prompt: String,
                                  maxTokens: Option[Int] = None,
                                  temperature: Option[Double] = None
                                )

// Container for AI response with execution metrics
case class SingleResponseResponse(
                                   response: String,
                                   latencyMs: Long
                                 )

// JSON handling and validation for single response interactions
object SingleResponseFormats {
  private val logger = LoggerFactory.getLogger(getClass)

  implicit val requestFormat: Format[SingleResponseRequest] = Json.format[SingleResponseRequest]
  implicit val responseFormat: Format[SingleResponseResponse] = Json.format[SingleResponseResponse]

  def validateTemperature(temp: Double): Boolean = {
    logger.debug("Validating temperature value: {}", temp)
    val isValid = temp >= 0.0 && temp <= 1.0
    if (!isValid) logger.warn("Invalid temperature value received: {}", temp)
    isValid
  }

  def validateTokens(tokens: Int): Boolean = {
    logger.debug("Validating token count: {}", tokens)
    val isValid = tokens > 0
    if (!isValid) logger.warn("Invalid token count received: {}", tokens)
    isValid
  }
}