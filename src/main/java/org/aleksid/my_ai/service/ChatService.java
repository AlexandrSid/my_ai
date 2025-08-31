package org.aleksid.my_ai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.model.Chat;
import org.aleksid.my_ai.model.ChatEntry;
import org.aleksid.my_ai.model.Role;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
