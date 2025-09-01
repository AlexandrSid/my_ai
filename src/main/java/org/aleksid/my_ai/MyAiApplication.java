package org.aleksid.my_ai;

import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.model.PostgresChatMemory;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class MyAiApplication {

    private final ChatRepository chatRepository;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultAdvisors(getAdvisor()).build();
//        return builder.defaultOptions(
//                ChatOptions.builder()
//                        .topP(0.9)
//                        .topK(40)
//                        .temperature(0.7)
//                        .frequencyPenalty(1.1)
//                        .build())
//                .build();
    }

    private Advisor getAdvisor() {
        return MessageChatMemoryAdvisor.builder(getChatMemory()).build();
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
