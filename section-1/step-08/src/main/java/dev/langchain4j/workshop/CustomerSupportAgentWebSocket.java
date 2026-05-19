package dev.langchain4j.workshop;

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

@ServerEndpoint("/customer-support-agent")
public class CustomerSupportAgentWebSocket {

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
        // Get the token stream from the agent
        TokenStream tokenStream = customerSupportAgent.chat(message);

        // // Use the WebSocketTokenStreamProcessor to handle the stream
        WebSocketTokenStreamProcessor processor = new WebSocketTokenStreamProcessor(session);
        processor.process(tokenStream);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        sessions.remove(session);
    }
}
