package com.HetNagda.services

import com.HetNagda.config.ClaudeConfig
import com.HetNagda.proto.lambda.{RequestToClaude, ResponseFromClaude}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.write
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest

import scala.util.Try

// Service class to handle interactions with AWS Bedrock Claude model
class ClaudeService(settings: ClaudeConfig) {
  implicit val jsonFormats = DefaultFormats

  private val awsHttpClient = UrlConnectionHttpClient.builder().build()

  private val claudeClient = BedrockRuntimeClient.builder()
    .region(Region.of(settings.awsRegionName))
    .httpClient(awsHttpClient)
    .build()

  def processRequest(userRequest: RequestToClaude): Try[ResponseFromClaude] = Try {
    val claudePrompt = Map(
      "prompt" -> s"\n\nHuman: ${userRequest.inputText}\n\nAssistant:",
      "max_tokens_to_sample" -> userRequest.parameters.getOrElse("max_tokens", "150").toInt,
      "temperature" -> userRequest.parameters.getOrElse("temperature", "0.7").toDouble,
      "top_p" -> 1,
      "stop_sequences" -> List("\n\nHuman:"),
      "anthropic_version" -> "bedrock-2023-05-31"
    )

    val serializedRequest = write(claudePrompt)
    println(s"Sending request to Claude: $serializedRequest")

    val claudeApiRequest = InvokeModelRequest.builder()
      .modelId(settings.claudeModelIdentifier)
      .contentType("application/json")
      .accept("application/json")
      .body(SdkBytes.fromUtf8String(serializedRequest))
      .build()

    val claudeResponse = claudeClient.invokeModel(claudeApiRequest)
    val responseContent = claudeResponse.body().asUtf8String()
    println(s"Received response from Claude: $responseContent")

    val parsedResponse = parse(responseContent)
    val generatedText = (parsedResponse \ "completion").extract[String].trim

    ResponseFromClaude(
      outputText = generatedText,
      confidenceScores = Map.empty,
      tokens = List.empty
    )
  }

  def cleanup(): Unit = {
    claudeClient.close()
    awsHttpClient.close()
  }
}