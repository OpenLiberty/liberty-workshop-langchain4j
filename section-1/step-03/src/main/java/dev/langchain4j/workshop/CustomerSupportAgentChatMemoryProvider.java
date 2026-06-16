package dev.langchain4j.workshop;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides a ChatMemory per session (identified by @MemoryId).
 * Each browser tab has its own conversation history.
 * Keeps a reference to allow inspection (debug).
 */
@ApplicationScoped
@Named("customer-support-agent-memory")
public class CustomerSupportAgentChatMemoryProvider implements ChatMemoryProvider {

    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    @Override
    public ChatMemory get(Object memoryId) {
        return memories.computeIfAbsent(memoryId, id ->
            MessageWindowChatMemory.builder()
                .id(id)
                .maxMessages(20)
                .build()
        );
    }
}
