package org.aleksid.my_ai.advisor.rag;

import lombok.Builder;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.aleksid.my_ai.advisor.expansion.ExpansionQueryAdvisor.ENRICHED_QUESTION;

@Builder
public class RagAdvisor implements BaseAdvisor {

    private final static PromptTemplate FINAL_PROMPT_TEMPLATE = PromptTemplate.builder().template("""
            Context: {context}
            Question: {question}
            """).build();

    private VectorStore vectorStore;
    private final int order;

    public static RagAdvisorBuilder builder(VectorStore vectorStore) {
        return new RagAdvisorBuilder().vectorStore(vectorStore);
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String originalUserQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String queryToRag = chatClientRequest.context().getOrDefault(ENRICHED_QUESTION, originalUserQuestion).toString();

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(queryToRag)
                        .topK(4)
                        .similarityThreshold(0.6)
                        .build());
        if (documents == null || documents.isEmpty()) {
            return chatClientRequest;
        }
        String llmContext = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining(System.lineSeparator()));

        String finalPrompt = FINAL_PROMPT_TEMPLATE.render(
                Map.of("context", llmContext, "question", originalUserQuestion));
        return chatClientRequest
                .mutate()
                .prompt(chatClientRequest
                        .prompt()
                        .augmentUserMessage(finalPrompt))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return this.order;
    }
}
