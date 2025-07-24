package org.lear.aibotservice.utilities;

import lombok.extern.slf4j.Slf4j;
import org.lear.aibotservice.models.RAGResponse;
import org.lear.aibotservice.services.RAGService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RAGStartupRunner implements ApplicationRunner {

    private final RAGService ragService;

    public RAGStartupRunner(RAGService ragService) {
        this.ragService = ragService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Starting RAG system initialization...");


        String[] exampleQuestions = {
                "What machines are available?",
                "Which steps require manual tracking?",
                "Show me production line information"
        };

        Thread.sleep(5000);

        for (String question : exampleQuestions) {
            log.info("Testing query: {}", question);
            try {
                RAGResponse response = ragService.ask(question);
                log.info("Response confidence: {}", response.getConfidence());
                log.info("Sources found: {}", response.getSources().size());
            } catch (Exception e) {
                log.warn("Query failed: {}", e.getMessage());
            }
        }

        log.info("RAG system ready!");
    }
}
