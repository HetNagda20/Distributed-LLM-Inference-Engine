package com.HetNagda

import com.HetNagda.domain.{ClaudeRequest, ClaudeResponse, ErrorResponse}
import com.HetNagda.proto.lambda.RequestToClaude
import com.HetNagda.services.ProtobufService
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class BasicServiceTests extends AnyWordSpec with Matchers {

  "ProtobufService" should {
    val service = new ProtobufService()

    "successfully encode a protobuf message to base64" in {
      // Create a simple protobuf message
      val request = RequestToClaude(
        inputText = "Hello World",
        parameters = Map("temp" -> "0.7")
      )

      // Try to encode it
      val result = service.encodeToBase64(request)

      // Verify encoding succeeded
      result.isSuccess shouldBe true
      result.get should not be empty
    }

    "successfully decode a valid base64 string to protobuf" in {
      // Create and encode a message
      val originalRequest = RequestToClaude(
        inputText = "Test message",
        parameters = Map("temp" -> "0.5")
      )

      val encoded = service.encodeToBase64(originalRequest)
      encoded.isSuccess shouldBe true

      // Try to decode it back
      val decoded = service.decodeFromBase64[RequestToClaude](encoded.get)

      // Verify decoding succeeded and content matches
      decoded.isSuccess shouldBe true
      decoded.get.inputText shouldBe "Test message"
      decoded.get.parameters shouldBe Map("temp" -> "0.5")
    }
  }

  "Domain Models" should {
    "correctly create and access ClaudeRequest fields" in {
      val request = ClaudeRequest(
        inputText = "Test input",
        temperature = Some(0.7),
        maxTokens = Some(100)
      )

      request.inputText shouldBe "Test input"
      request.temperature shouldBe Some(0.7)
      request.maxTokens shouldBe Some(100)
    }

    "correctly create ClaudeRequest with default values" in {
      val request = ClaudeRequest("Test input")

      request.inputText shouldBe "Test input"
      request.temperature shouldBe None
      request.maxTokens shouldBe None
    }

    "correctly create and access ClaudeResponse" in {
      val response = ClaudeResponse("Test response")
      response.generatedText shouldBe "Test response"
    }

    "correctly create and access ErrorResponse" in {
      val error = ErrorResponse("Test error message")
      error.errorMessage shouldBe "Test error message"
    }
  }
}