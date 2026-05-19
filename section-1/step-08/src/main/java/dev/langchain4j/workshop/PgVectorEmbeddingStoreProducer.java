package dev.langchain4j.liberty.workshop.producers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * CDI producer for creating and configuring the EmbeddingService instance.
 */
@ApplicationScoped
public class PgVectorEmbeddingStoreProducer {
    private static final Logger logger = LoggerFactory.getLogger(PgVectorEmbeddingStoreProducer.class);

    /**
     * Produces an EmbeddingStore.
     *
     * @return EmbeddingStore<TextSegment>
     *          The EmbeddingStore
     */
    @Produces
    public EmbeddingStore<TextSegment> produceEmbeddingStore(
        @ConfigProperty(name = "vector.store.hostname") String hostname,
        @ConfigProperty(name = "vector.store.port") int port,
        @ConfigProperty(name = "vector.store.database") String database,
        @ConfigProperty(name = "vector.store.username") String username,
        @ConfigProperty(name = "vector.store.password") String password,
        @ConfigProperty(name = "vector.store.dimension") int dimension
    ) {
        logger.info("Creating PgVector EmbeddingStore");

        return PgVectorEmbeddingStore.builder()
            .host(hostname)
            .port(port)
            .database(database)
            .user(username)
            .password(password)
            .table("embeddings")
            .dimension(dimension)
            .build();
    }
}
