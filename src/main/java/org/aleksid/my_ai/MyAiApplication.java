package org.aleksid.my_ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyAiApplication {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
//        return builder.defaultOptions(
//                ChatOptions.builder()
//                        .topP(0.9)
//                        .topK(40)
//                        .temperature(0.7)
//                        .frequencyPenalty(1.1)
//                        .build())
//                .build();
    }

    public static void main(String[] args) {
        ChatClient chatClient = SpringApplication.run(MyAiApplication.class, args).getBean(ChatClient.class);
        System.out.println(chatClient.prompt().user("Дай первую строчку богемской рапсодии").call().content());
    }

}
