package dev.langchain4j.workshop;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialToolCall;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolExecution;

import jakarta.websocket.Session;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Processes token streams from AI models and handles various callback events.
 * This class encapsulates the logic for handling partial responses, tool executions,
 * and other streaming events from the LangChain4j TokenStream.
 */
public class WebSocketTokenStreamProcessor {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketTokenStreamProcessor.class);

    private final Session session;

    public WebSocketTokenStreamProcessor(Session session) {
        this.session = session;
    }

    /**
     * Processes the token stream with all callback handlers configured.
     * 
     * @param tokenStream the token stream to process
     * @return a CompletableFuture that completes when the response is fully received
     */
    public void process(TokenStream tokenStream) {
        CompletableFuture<ChatResponse> futureResponse = new CompletableFuture<>();

        try {
            // Process the tokens in the stream
            tokenStream
                .onPartialResponse((String partialResponse) -> processPartialResponse(partialResponse, futureResponse))
                .onPartialThinking((PartialThinking partialThinking) -> processPartialThinking(partialThinking, futureResponse))
                .onIntermediateResponse((ChatResponse intermediateResponse) -> processIntermediateResponse(intermediateResponse, futureResponse))
                .onPartialToolCall((PartialToolCall partialToolCall) -> processPartialToolCall(partialToolCall, futureResponse))
                .beforeToolExecution((BeforeToolExecution beforeToolExecution) -> processBeforeToolExecution(beforeToolExecution, futureResponse))
                .onToolExecuted((ToolExecution toolExecution) -> processToolExecuted(toolExecution, futureResponse))
                .onCompleteResponse((ChatResponse response) -> processCompleteResponse(response, futureResponse))
                .onError((Throwable error) -> processError(error, futureResponse))
                .start();

            // Wait for the response to complete
            futureResponse.join();
        } catch (Exception e) {
            logger.error("Error processing message", e);
            try {
                session.getBasicRemote().sendText("Error processing your message: " + e.getMessage());
                futureResponse.completeExceptionally(e);
            } catch (IOException ioException) {
                logger.error("Error sending error message to client", ioException);
            }
        }
    }

    private void processPartialResponse(String partialResponse, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Partial response: {}", partialResponse);
        try {
            session.getBasicRemote().sendText(partialResponse);
        } catch (IOException e) {
            logger.error("Error processing partial response", e);
            futureResponse.completeExceptionally(e);
        }
    }

    private void processPartialThinking(PartialThinking partialThinking, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Partial thinking: {}", partialThinking);
    }

    private void processIntermediateResponse(ChatResponse intermediateResponse, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Intermediate response: {}", intermediateResponse);
    }

    private void processPartialToolCall(PartialToolCall partialToolCall, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Partial tool call: {}", partialToolCall); 
    }

    private void processBeforeToolExecution(BeforeToolExecution beforeToolExecution, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Before tool execution: {}", beforeToolExecution);
    }

    private void processToolExecuted(ToolExecution toolExecution, CompletableFuture<ChatResponse> futureResponse) {
        logger.debug("Tool executed: {}", toolExecution);
    }

    private void processCompleteResponse(ChatResponse chatResponse, CompletableFuture<ChatResponse> futureResponse) {

        logger.debug("Complete response: {}", chatResponse);

        logger.info("Chat Id: {}", chatResponse.id());
        logger.info("Model Name: {}", chatResponse.modelName());
        logger.info("Input Token Count: {}", chatResponse.tokenUsage().inputTokenCount());
        logger.info("Output Token Count: {}", chatResponse.tokenUsage().outputTokenCount());
        logger.info("Total Token Count: {}", chatResponse.tokenUsage().totalTokenCount());

        //  { text = "Hello! How can I help you today?", thinking = null, toolExecutionRequests = [], attributes = {} }, metadata = ChatResponseMetadata{id='null', modelName='gpt-oss:20b', tokenUsage=TokenUsage { inputTokenCount = 68, outputTokenCount = 35, totalTokenCount = 103 }, finishReason=STOP} }
        futureResponse.complete(chatResponse);
    }

    private void processError(Throwable error, CompletableFuture<ChatResponse> futureResponse) {
        logger.error("Error processing message", error);
        try {
            session.getBasicRemote().sendText("Error processing your message: " + error.getMessage());
            futureResponse.completeExceptionally(error);
        } catch (IOException ioException) {
            logger.error("Error sending error message to client", ioException);
            futureResponse.completeExceptionally(ioException);
        }
    }
}
