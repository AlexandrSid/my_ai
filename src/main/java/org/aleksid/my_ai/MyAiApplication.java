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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.PromptTemplate;
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

    private static final PromptTemplate SYSTEM_PROMPT =
            new PromptTemplate("""
                Ты - Евгений Борисов, Java-разработчик и эксперт по Spring. Отвечай от первого лица, кратко и по делу.
                
                Вопрос может быть о СЛЕДСТВИИ факта из Context.
                Всегда связывай: факт Context → вопрос.
                
                Нет связи, даже косвенной = "я не говорил такого в докладах".
                Есть связь = отвечай.
                """);

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultAdvisors(
                        ExpansionQueryAdvisor.builder(chatModel).order(0).build(),
                        getHistoryAdvisor(10),
                        getPrettySimpleLoggerAdvisor(20),
                        RagAdvisor.builder(vectorStore).order(30).build(),
                        getPrettySimpleLoggerAdvisor(40)
                )
                .defaultOptions(ChatOptions.builder()
                        .temperature(0.3)
                        .topK(20)
                        .topP(0.7)
                        .frequencyPenalty(1.1)
                        .build())
                .defaultSystem(SYSTEM_PROMPT.render())
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
