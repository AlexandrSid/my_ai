package org.aleksid.my_ai.advisor.expansion;

import lombok.Builder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.api.OllamaOptions;

import java.util.Map;

@Builder
public class ExpansionQueryAdvisor implements BaseAdvisor {

    private final static PromptTemplate EXPANSION_TEMPLATE = PromptTemplate.builder().template("""
            Rephrase the user query:
            Replace any second-person references in Russian (ты, тебе, тебя, твой, твоя, твоё, твои, тобой, тобою) 
            with third-person references about Евгений Борисов.
            If the query has no second-person references, return it unchanged.
            If the query is already in the third person, return it unchanged.
            Original query: "{query}"
            Rephrased query (only the query itself, no explanations):
            """).build();

    public static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";
    public static final String ORIGINAL_QUESTION = "ORIGINAL_QUESTION";
    public static final String EXPANSION_RATION = "EXPANSION_RATION";

    private final int order;
    private final ChatClient chatClient;

    public static ExpansionQueryAdvisorBuilder builder(ChatModel chatModel) {
        return new ExpansionQueryAdvisorBuilder()
                .chatClient(ChatClient
                        .builder(chatModel)
                        .defaultOptions(OllamaOptions
                                .builder()
                                .temperature(0.0)
                                .topK(1)
                                .topP(0.1)
                                .repeatPenalty(1.0)
                                .build())
                        .build());
    }

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userQuestion = chatClientRequest.prompt().getUserMessage().getText();
        String extendedQuery = EXPANSION_TEMPLATE.render(Map.of("query", userQuestion));
        String enrichedQuestion = chatClient.prompt().user(extendedQuery).call().content();

        double expansionRatio = enrichedQuestion.length() / (double) userQuestion.length();
        return chatClientRequest.mutate()
                .context(ORIGINAL_QUESTION, userQuestion)
                .context(ENRICHED_QUESTION, enrichedQuestion)
                .context(EXPANSION_RATION, expansionRatio)
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
