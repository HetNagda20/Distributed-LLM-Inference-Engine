package com.HetNagda.models

import play.api.libs.json._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

case class DialogueRequest(
                             prompt: String,
                             exportPath: Option[String] = None,
                             maxTokens: Option[Int] = None,
                             maxTurns: Option[Int] = None  // Added maxTurns parameter
                           )

object DialogueRequest {
  implicit val format: Format[DialogueRequest] = Json.format[DialogueRequest]
}

case class DialogueResponse(
                             iterationCount: Int,
                             meanProcessingTime: Long,
                             dialogue: Seq[DialogueIteration]
                           )

object DialogueFormats {
  private val timestampFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  implicit val timestampHandler: Format[LocalDateTime] = new Format[LocalDateTime] {
    def reads(json: JsValue): JsResult[LocalDateTime] =
      json.validate[String].map(LocalDateTime.parse(_, timestampFormat))
    def writes(timestamp: LocalDateTime): JsValue =
      JsString(timestamp.format(timestampFormat))
  }

  implicit val dialogueIterationFormat: Format[DialogueIteration] = Json.format[DialogueIteration]
  implicit val dialogueRequestFormat: Format[DialogueRequest] = Json.format[DialogueRequest]
  implicit val dialogueResponseFormat: Format[DialogueResponse] = Json.format[DialogueResponse]
}