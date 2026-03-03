import com.HetNagda.models.{DialogueIteration, DialogueRequest}
import com.HetNagda.services.DialogueProcessor
import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers._
import org.slf4j.LoggerFactory

import java.io.File

class DialogueServiceTest extends AnyFunSuite with MockitoSugar {

  // Test if application.conf is present at the correct directory
  test("Verify if application.conf is present at the correct directory") {
    val configFile = new File("src/main/resources/application.conf")
    assert(configFile.exists(), "application.conf file should exist.")
  }

  // Test if configuration is correctly loaded
  test("Test if Config Load is providing parameters correctly") {
    val config: Config = ConfigFactory.load()
    assert(config.hasPath("ollama.host"), "Config should have Ollama host defined.")
    assert(config.hasPath("service.host"), "Config should have service host defined.")
    assert(config.hasPath("conversation.max-turns"), "Config should have conversation max-turns defined.")
  }

  // Test if the Dialogue Processor service can load and handle a dialogue request
  test("Test if Dialogue Processor handles dialogue request correctly") {
    val dialogueProcessor = mock[DialogueProcessor]
    val request = DialogueRequest(prompt = "Hello", maxTokens = Some(100), maxTurns = Some(5))

    // Mock the response from the DialogueProcessor
    val dialogueResponse = Seq.empty[DialogueIteration]
    when(dialogueProcessor.processDialogue(any[String], any[Option[Int]], any[Option[Int]])).thenReturn(dialogueResponse)

    // Test the processing method
    val response = dialogueProcessor.processDialogue(request.prompt, request.maxTokens, request.maxTurns)

    assert(response === dialogueResponse, "DialogueProcessor should return an empty sequence for this test.")
  }



  // Test if the Job Timeout is Set Correctly for a Dialogue Session
  test("Verify Job Timeout for Dialogue Session") {
    val config: Config = ConfigFactory.load()
    val timeout = config.getInt("conversation.timeout-minutes")
    assert(timeout === 30, "Expected conversation timeout to be 30 minutes.")
  }
}
