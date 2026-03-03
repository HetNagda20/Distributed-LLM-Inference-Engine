package com.HetNagda.models

import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import play.api.libs.json._

// Primary request model for AI interactions with configurable parameters
case class AIModelRequest(
                           content: String,
                           creativityFactor: Double,
                           responseLength: Int
                         )

// Container for AI model's generated response
case class AIModelResponse(
                            content: String
                          )

// Represents a single turn in the dialogue process, including performance metrics
case class DialogueIteration(
                              timestamp: LocalDateTime,
                              userInput: String,
                              primaryResponse: String,
                              secondaryResponse: String,
                              latencyMs: Long
                            )

// Provides JSON serialization/deserialization for the model classes
object ModelFormats {
  private val logger = LoggerFactory.getLogger(getClass)

  implicit val aiModelRequestFormat: Format[AIModelRequest] = Json.format[AIModelRequest]
  implicit val aiModelResponseFormat: Format[AIModelResponse] = Json.format[AIModelResponse]
  implicit val dialogueIterationFormat: Format[DialogueIteration] = Json.format[DialogueIteration]
}