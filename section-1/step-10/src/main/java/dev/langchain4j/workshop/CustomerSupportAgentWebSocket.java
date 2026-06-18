package dev.langchain4j.workshop;

import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.inject.Inject;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServerEndpoint("/customer-support-agent")
public class CustomerSupportAgentWebSocket {
    private static final Logger logger = LoggerFactory.getLogger(CustomerSupportAgentWebSocket.class);

    @Inject
    private CustomerSupportAgent customerSupportAgent;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        session.getBasicRemote().sendText("Welcome to Miles of Smiles! How can I help you today?");

    }

    @OnMessage
    public String onMessage(String message, Session session) throws IOException {
        try {
            return customerSupportAgent.chat(session.getId(), message);
        } catch (InputGuardrailException e) {
            logger.error("Error calling the LLM: {}", e.getMessage());
            return "Sorry, I am unable to process your request at the moment. It's not something I'm allowed to do.";
        } catch (Exception e) {
            logger.error("Error calling the LLM: {}", e.getMessage(), e);
            return "I ran into some problems. Please try again.";
        }
    }
}
