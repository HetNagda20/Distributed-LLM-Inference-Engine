package com.HetNagda.config

// Configuration class for AWS Bedrock service initialization
case class ClaudeConfig(
                         awsRegionName: String,
                         claudeModelIdentifier: String
                       )

object ServiceConfiguration {
  val claudeSettings = ClaudeConfig(
    awsRegionName = sys.env.getOrElse("AWS_REGION", "us-east-1"),
    claudeModelIdentifier = sys.env.getOrElse("BEDROCK_MODEL_ID", "anthropic.claude-v2")
  )
}