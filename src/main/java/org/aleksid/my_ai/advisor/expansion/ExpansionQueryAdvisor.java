package org.aleksid.my_ai.advisor.expansion;

import lombok.Builder;
import lombok.Getter;
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
            Instruction: Expand the search query by adding the most relevant terms.
           
            SPECIALIZATION: SPRING FRAMEWORK
            Spring bean lifecycle: constructor → BeanPostProcessor → PostConstruct → proxy → ContextListener
            Technologies: Dynamic Proxy, CGLib, reflection, annotations, XML configuration
            Components: BeanFactory, ApplicationContext, BeanDefinition, MBean, JMX
            Patterns: dependency injection, AOP, profiling, method interception

            RULES:
            Keep ALL words from the original question
            Add UP TO FIVE of the most important terms
            Choose the most specific and relevant words directly related to the topic of the question
            Do not insert terms from unrelated domains (e.g., don’t add IT terms to medical questions)
            The result must be a simple list of words separated by spaces

            SELECTION STRATEGY:
            Priority: domain-specific terms from the same field as the question
            Avoid general words
            Focus on key concepts

            EXAMPLES:
            "what is spring" → "what is spring framework Java"
            "how to create a file" → "how to create a file document program"
            "what are you allergic to" → "what are you allergic to immunity symptoms pollen reaction"

            Question: {question}
            Expanded query:
            """).build();

    public static final String ENRICHED_QUESTION = "ENRICHED_QUESTION";
    public static final String ORIGINAL_QUESTION = "ORIGINAL_QUESTION";
    public static final String EXPANSION_RATION = "EXPANSION_RATION";

    @Getter
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
        String extendedQuery = EXPANSION_TEMPLATE.render(Map.of("question", userQuestion));
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
}
