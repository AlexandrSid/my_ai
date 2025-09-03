package org.aleksid.my_ai.model;

import lombok.Builder;
import org.aleksid.my_ai.repository.ChatRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

@Builder
public class PostgresChatMemory implements ChatMemory {
    private int maxMessages;
    private ChatRepository chatMemoryRepository;

    @Override
    public void add(String conversationId, List<Message> messages) {
        Chat chat = chatMemoryRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        List<ChatEntry> chatEntries = messages.stream().map(ChatEntry::fromMessage).toList();
        chat.addEntries(chatEntries);
        chatMemoryRepository.save(chat);
    }

    @Override
    public List<Message> get(String conversationId) {
        Chat chat = chatMemoryRepository.findById(Long.valueOf(conversationId)).orElseThrow();
        long messagesToSkip = Math.max(0, chat.getHistory().size() - maxMessages);
        return chat.getHistory().stream()
                .skip(messagesToSkip)
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        //not implemented NEVER
    }
}
