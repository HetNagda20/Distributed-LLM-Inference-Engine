package com.HetNagda.services

import scalapb.GeneratedMessage
import java.util.Base64
import scala.util.Try
import scalapb.json4s.JsonFormat

// Service class for handling protobuf message encoding and decoding
class MessageTransformService {
  def base64ToProto[T <: GeneratedMessage](encodedString: String)(implicit companion: scalapb.GeneratedMessageCompanion[T]): Try[T] = Try {
    val rawBytes = Base64.getDecoder.decode(encodedString)
    companion.parseFrom(rawBytes)
  }

  def protoToBase64(protoMessage: GeneratedMessage): Try[String] = Try {
    Base64.getEncoder.encodeToString(protoMessage.toByteArray)
  }

  def parseJson[T <: GeneratedMessage](jsonString: String)(implicit companion: scalapb.GeneratedMessageCompanion[T]): Try[T] = Try {
    JsonFormat.fromJsonString[T](jsonString)(companion)
  }

  def toJsonString(protoMessage: GeneratedMessage): Try[String] = Try {
    JsonFormat.toJsonString(protoMessage)
  }
}