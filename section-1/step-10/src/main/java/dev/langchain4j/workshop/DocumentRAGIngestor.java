package dev.langchain4j.workshop;

import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class DocumentRAGIngestor {
    private static final Logger logger = LoggerFactory.getLogger(DocumentRAGIngestor.class);

    @Produces
    private EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    @Inject
    private EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    @ConfigProperty(name = "customer-support-agent.rag.docs.dir")
    private String ragDocsDir;

    @Inject
    @ConfigProperty(name = "customer-support-agent.rag.max-segment-size")
    private int maxSegmentSize;

    @Inject
    @ConfigProperty(name = "customer-support-agent.rag.max-overlap-size")
    private int maxOverlapSize;

    public void ingest(@Observes @Initialized(ApplicationScoped.class) Object pointless) throws URISyntaxException {
        long start = System.currentTimeMillis();

        // Create the embedding store ingestor
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(maxSegmentSize, maxOverlapSize))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        // Load the document(s) from the configured location and ingest them
        List<Document> docs = ClassPathDocumentLoader.loadDocuments(ragDocsDir, new TextDocumentParser());
        ingestor.ingest(docs);

        logger.info("Ingested {} docs in {}ms", docs.size(), System.currentTimeMillis() - start);
    }
}
