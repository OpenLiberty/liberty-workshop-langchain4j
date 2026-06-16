package dev.langchain4j.workshop;

import dev.langchain4j.guardrail.InputGuardrailException;
import dev.langchain4j.service.TokenStream;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/customer-support-agent")
public class CustomerSupportAgentWebSocket {
    private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAgentWebSocket.class);

    // Thread-safe set to store all active sessions
    private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @Inject
    private CustomerSupportAgent customerSupportAgent;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        sessions.add(session);
        session.getBasicRemote().sendText("Welcome to Miles of Smiles! How can I help you today?");
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        try {
            // Get the token stream from the agent
            TokenStream tokenStream = customerSupportAgent.chat(session.getId(), message);

            // // Use the WebSocketTokenStreamProcessor to handle the stream
            WebSocketTokenStreamProcessor processor = new WebSocketTokenStreamProcessor(session);
            processor.process(tokenStream);
        } catch (InputGuardrailException e) {
            logger.error("Error calling the LLM: {}", e.getMessage());
            session.getBasicRemote().sendText("Sorry, I am unable to process your request at the moment. It's not something I'm allowed to do.");
        } catch (Exception e) {
            logger.error("Error calling the LLM: {}", e.getMessage(), e);
            session.getBasicRemote().sendText("I ran into some problems. Please try again.");
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        sessions.remove(session);
    }
}
