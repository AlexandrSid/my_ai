package org.aleksid.my_ai.model;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PostgresChatMemory implements ChatMemory {

    private final ChatRepository chatRepository;

    @Override
    @Transactional
    public void add(String conversationId, List<Message> messages) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        List<ChatEntry> chatEntries = messages.stream().map(ChatEntry::fromMessage).toList();
        chat.addEntries(chatEntries);
    }

    @Override
    @Transactional
    public List<Message> get(String conversationId) {
        Chat chat = chatRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream().map(ChatEntry::toMessage).toList();
    }

    @Override
    public void clear(String conversationId) {
        //not implemented
    }
}
