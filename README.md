# Distributed LLM Inference Engine

**Het Rajesh Nagda** | [LinkedIn](https://www.linkedin.com/in/hetnagda20/) | [Portfolio](https://hetnagda20.github.io/het-website/)

A cloud-native, distributed system that orchestrates multi-turn conversations between a locally hosted LLM (Ollama/Llama3) and a cloud-hosted LLM (AWS Bedrock/Claude) via a microservice architecture built with Scala, Akka HTTP, gRPC, and AWS.

---

## Architecture Overview

```
User Request (cURL / Postman)
        ↓
Local Conversational Agent  ←──────────────-┐
        ↓                                   │
EC2 Akka HTTP Microservice                  │
        ↓                                   │
  Protobuf Serialization                    │
        ↓                                   │
AWS API Gateway → Lambda Function           │
        ↓                                   │
  AWS Bedrock (Claude)                      │
        ↓                                   │
  Protobuf Deserialization                  │
        ↓                                   │
EC2 Microservice → Conversational Agent     │
        ↓                                   │
  Ollama (Llama3 local) ───────────────────-┘
        ↓
 Repeat for N turns → CSV Export
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Scala 2.13 |
| HTTP Server | Akka HTTP |
| Serialization | Protobuf / gRPC |
| Cloud Compute | AWS EC2, AWS Lambda |
| LLM (Cloud) | AWS Bedrock (Claude / Titan) |
| LLM (Local) | Ollama (Llama3) |
| Build Tool | sbt |
| Containerization | Docker |
| Logging | Logback (SLF4J) |
| Testing | ScalaTest |

---

## Project Structure

```
├── Local_Conversational_Agent/     # Akka HTTP server orchestrating multi-turn dialogue
│   ├── src/main/scala/
│   │   ├── api/DialogueRoutes.scala
│   │   ├── services/DialogueProcessor.scala
│   │   ├── models/
│   │   └── server/Server.scala
│   └── build.sbt
│
├── EC2_server/                     # Microservice deployed on AWS EC2
│   ├── src/main/scala/
│   │   ├── routes/ClaudeRoutes.scala
│   │   ├── services/ClaudeService.scala
│   │   ├── services/ProtobufService.scala
│   │   └── domain/Models.scala
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── build.sbt
│
└── Lambda_Scala/                   # AWS Lambda handler for Bedrock inference
    ├── src/main/scala/
    │   ├── handler/ClaudeLambdaHandler.scala
    │   ├── services/ClaudeService.scala
    │   └── services/MessageTransformService.scala
    └── build.sbt
```

---

## Prerequisites

- **Java 11+** and **sbt 1.6+**
- **Ollama** installed locally with `llama3` pulled (`ollama pull llama3`)
- **AWS CLI** configured with appropriate IAM permissions
- **Docker** (for containerized EC2 deployment)
- AWS Lambda function deployed with the assembled JAR
- AWS API Gateway endpoint configured to trigger Lambda

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/HetNagda/distributed-llm-inference-engine.git
cd distributed-llm-inference-engine
```

### 2. Deploy the Lambda Function

```bash
cd Lambda_Scala
sbt clean compile assembly
# Upload target/scala-2.13/lambda-assembly-*.jar to S3
# Deploy to AWS Lambda via Console or CLI
```

### 3. Start the EC2 Server

```bash
cd EC2_server
# Configure src/main/resources/application.conf with your API Gateway URL
sbt clean compile run
# Or via Docker:
docker-compose up --build
```

### 4. Start the Local Conversational Agent

```bash
cd Local_Conversational_Agent
sbt clean compile run
```

---

## API Reference

### Generate Multi-Turn Dialogue

Initiates a conversation between Llama3 (local) and Claude via Bedrock (cloud), exported to CSV.

```bash
curl -X POST http://localhost:8081/api/dialogue \
-H "Content-Type: application/json" \
-d '{
    "prompt": "Do you think Mumbai is the best city to live in?",
    "exportPath": "dialogue_output.csv",
    "maxTokens": 300,
    "maxTurns": 4
}'
```

### Single Response from Ollama (Local)

```bash
curl -X POST http://localhost:8081/api/ollama \
-H "Content-Type: application/json" \
-d '{
    "prompt": "What is your opinion about Mumbai?",
    "maxTokens": 200
}'
```

### Single Response from AWS Bedrock (Cloud)

```bash
curl -X POST http://localhost:8081/api/bedrock \
-H "Content-Type: application/json" \
-d '{
    "prompt": "Tell me about Mumbai",
    "maxTokens": 200
}'
```

---

## Configuration

Edit `src/main/resources/application.conf` in each module to customize:

```hocon
service {
  host = "0.0.0.0"
  port = 8081
  apiGatewayUrl = "https://<your-api-gateway-id>.execute-api.<region>.amazonaws.com/prod"
}

claude {
  defaultTemperature = 0.7
  defaultMaxTokens = 500
}
```

---

## Running Tests

```bash
sbt test
```

Test coverage includes:
- Protobuf serialization/deserialization roundtrips
- Akka HTTP route validation
- Dialogue service multi-turn orchestration
- Lambda handler input/output contract
- Configuration loading

---

## Sample Dialogue Output (`dialogue_output.csv`)

```csv
timestamp,speaker,message,response_time_ms
2024-12-01T01:57:43,Claude,"There are many factors that contribute to livability...",8135
2024-12-01T01:57:43,Llama,"I completely agree — the notion of a 'best' city is subjective...",8135
2024-12-01T01:57:50,Claude,"You're right. I should not have overgeneralized...",7135
2024-12-01T01:57:50,Llama,"Recognizing bias is a wonderful quality to possess...",7135
```

---

## Deployment

### Docker (EC2)

```bash
cd EC2_server
docker build -t ec2-inference-server .
docker run -p 8080:8080 ec2-inference-server
```

### AWS Lambda

1. Run `sbt clean compile assembly` inside `Lambda_Scala/`
2. Upload the JAR to an S3 bucket in the **same region** as your Lambda function
3. Set the Lambda handler to `com.HetNagda.handler.ClaudeLambdaHandler`
4. Attach an IAM role with `AmazonBedrockFullAccess` and `AWSLambdaBasicExecutionRole`

---

## Key Performance Highlights

- Sub-200ms latency for Protobuf-encoded requests between EC2 and Lambda
- Configurable conversation depth (turns) and token limits per request
- Structured CSV export of full dialogue sessions with timestamps and response times
- Logback-based logging across all three modules for observability

---

## Limitations

- Requires Ollama installed locally with Llama3 available
- Lambda IAM role must have Bedrock access in the correct AWS region
- The Lambda JAR must be uploaded to an S3 bucket in the same region as the Lambda function
- Protobuf schema changes require recompilation of all three modules

---
## Related Repos

| Part | Description | Link |
|---|---|---|
| Part 1 | MapReduce Tokenization Pipeline | [LLM-Tokenization-MapReduce](https://github.com/HetNagda20/LLM-Tokenization-MapReduce) |
| Part 2 | Spark + DeepLearning4J Training | [LLM-Spark-Training](https://github.com/HetNagda20/LLM-Spark-Training) |
| Part 3 | Akka HTTP + AWS Lambda Inference Engine | You are here |

## License

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) — feel free to fork and build on top of this project.

![](https://hits.sh/github.com/HetNagda20/Distributed-LLM-Inference-Engine.svg)
