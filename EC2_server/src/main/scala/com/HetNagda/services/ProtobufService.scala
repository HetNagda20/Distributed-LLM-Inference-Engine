package com.HetNagda.services

import scalapb.GeneratedMessage
import java.util.Base64
import scala.util.Try
import org.slf4j.LoggerFactory

class ProtobufService {
  private val logger = LoggerFactory.getLogger(getClass)

  def decodeFromBase64[T <: GeneratedMessage](base64String: String)(implicit companion: scalapb.GeneratedMessageCompanion[T]): Try[T] = {
    logger.info("Starting protobuf base64 decoding")
    logger.debug(s"Decoding string of length: ${base64String.length}")

    Try {
      val decodedBytes = Base64.getDecoder.decode(base64String)
      logger.debug(s"Decoded ${decodedBytes.length} bytes")

      val result = companion.parseFrom(decodedBytes)
      logger.info(s"Successfully parsed protobuf message: ${companion.getClass.getSimpleName}")
      result
    }.recoverWith { case error =>
      logger.error("Failed to decode protobuf message", error)
      Try(throw error)
    }
  }

  def encodeToBase64(message: GeneratedMessage): Try[String] = {
    logger.info("Starting protobuf base64 encoding")
    logger.debug(s"Encoding message type: ${message.getClass.getSimpleName}")

    Try {
      val messageBytes = message.toByteArray
      logger.debug(s"Serialized to ${messageBytes.length} bytes")

      val encodedString = Base64.getEncoder.encodeToString(messageBytes)
      logger.info("Successfully encoded protobuf message to base64")
      encodedString
    }.recoverWith { case error =>
      logger.error("Failed to encode protobuf message", error)
      Try(throw error)
    }
  }
}