package org.aleksid.my_ai;

import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.model.PostgresChatMemory;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
        return builder.defaultAdvisors(getHistoryAdvisor(), getRagAdvisor()).build();
//        return builder.defaultOptions(
//                ChatOptions.builder()
//                        .topP(0.9)
//                        .topK(40)
//                        .temperature(0.7)
//                        .frequencyPenalty(1.1)
//                        .build())
//                .build();
    }

    private Advisor getHistoryAdvisor() {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).build();
    }

    private Advisor getRagAdvisor() {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(MY_PROMPT_TEMPLATE)
                .searchRequest(
                        SearchRequest.builder().topK(4).build()
                )
                .build();
    }

    private ChatMemory getChatMemory() {
        return PostgresChatMemory.builder()
                .chatMemoryRepository(chatRepository)
                .maxMessages(2)
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(MyAiApplication.class, args);
    }

}
