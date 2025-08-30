package org.aleksid.my_ai.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.model.Chat;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

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
}
