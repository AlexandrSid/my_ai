package org.aleksid.my_ai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.aleksid.my_ai.model.Chat;
import org.aleksid.my_ai.model.ChatEntry;
import org.aleksid.my_ai.model.PostgresChatMemory;
import org.aleksid.my_ai.model.Role;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.aleksid.my_ai.model.Role.ASSISTANT;
import static org.aleksid.my_ai.model.Role.USER;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final ChatClient chatClient;

    @Autowired
    private ChatService selfProxy;

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }

    public Chat findById(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow();
    }

    @Transactional
    public Chat createNewChat(String title) {
        Chat chat = Chat.builder().title(title).build();
        return chatRepository.save(chat);
    }

    @Transactional
    public void deleteChat(Long chatId) {
        chatRepository.deleteById(chatId);
    }

    @Transactional
    public void proceedInteraction(Long chatId, String prompt) {
        selfProxy.addChatEntry(chatId, prompt, USER);
        String answer = chatClient.prompt().user(prompt).call().content();
        selfProxy.addChatEntry(chatId, answer, ASSISTANT);
    }

    @Transactional
    public void addChatEntry(Long chatId, String prompt, Role role) {
        Chat chat = chatRepository.findById(chatId).orElseThrow();
        ChatEntry entry = ChatEntry.builder().role(role).content(prompt).build();
        chat.addEntry(entry);
    }

    public SseEmitter proceedInteractionWithStreaming(Long chatId, String userPrompt) {
        final StringBuilder answerAccumulator = new StringBuilder();

        SseEmitter sseEmitter = new SseEmitter(0L);
        chatClient
                .prompt().user(userPrompt)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .chatResponse()
                .subscribe(chatResponse -> processToken(chatResponse, sseEmitter, answerAccumulator),
                        sseEmitter::completeWithError);

        return sseEmitter;
    }


    @SneakyThrows
    private static void processToken(ChatResponse chatResponse, SseEmitter sseEmitter, StringBuilder answerAccumulator) {
        var token = chatResponse.getResult().getOutput();
        answerAccumulator.append(token.getText());
        sseEmitter.send(token);
    }
}
