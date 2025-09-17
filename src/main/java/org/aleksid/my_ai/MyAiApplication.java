package org.aleksid.my_ai;

import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.advisor.expansion.ExpansionQueryAdvisor;
import org.aleksid.my_ai.advisor.rag.RagAdvisor;
import org.aleksid.my_ai.model.PostgresChatMemory;
import org.aleksid.my_ai.repository.ChatRepository;
import org.aleksid.my_ai.util.PrintUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class MyAiApplication {

    private final ChatRepository chatRepository;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;

    private static final PromptTemplate MY_PROMPT_TEMPLATE =
            new PromptTemplate("""
                    {query}
                                        
                    Информация из контекста приведена ниже и окружена линиями ---------------------
                                        
                    ---------------------
                    {question_answer_context}
                    ---------------------
                                        
                    Основываясь на контексте и предоставленной истории, а не на собственных знаниях,
                    ответь на комментарий пользователя. Если ответа нет в контексте, сообщи пользователю,
                    что ты не можешь ответить на вопрос.
                                        
                    """);

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(10),
                        getPrettySimpleLoggerAdvisor(20),
//                        getRagAdvisor(30),
                        RagAdvisor.builder(vectorStore).order(30).build(),
                        getPrettySimpleLoggerAdvisor(40)
                )
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .topK(20)
                        .topP(0.7)
                        .frequencyPenalty(1.1)
                        .build())
                .build();
    }

    private Advisor getPrettySimpleLoggerAdvisor(int order) {
        return SimpleLoggerAdvisor
                .builder()
                .requestToString(request -> "Request:\n" + PrintUtils.prettyPrint(request.toString()))
                .responseToString(response -> "Response:\n" + PrintUtils.prettyPrint(response.getResult().toString()))
                .order(order)
                .build();
    }

    private Advisor getHistoryAdvisor(int order) {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).order(order).build();
    }

    private Advisor getRagAdvisor(int order) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(
                        SearchRequest.builder().topK(4).similarityThreshold(0.6).build()
                ).order(order)
                .build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .chatMemoryRepository(chatRepository)
                .maxMessages(8)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(MyAiApplication.class, args);
    }

}
