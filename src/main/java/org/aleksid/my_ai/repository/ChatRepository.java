package org.aleksid.my_ai.repository;

import org.aleksid.my_ai.model.Chat;
import org.aleksid.my_ai.model.ChatEntry;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long>, ChatMemoryRepository {
    @Override
    default List<String> findConversationIds() {
        return findAll().stream()
                .map(Chat::getId)
                .map(String::valueOf)
                .toList();
    }

    @Override
    default List<Message> findByConversationId(String conversationId) {
        if (conversationId.equals(ChatMemory.DEFAULT_CONVERSATION_ID)) {
            return new ArrayList<>();
        }
        Chat chat = findById(Long.valueOf(conversationId)).orElseThrow();
        return chat.getHistory().stream()
                .map(ChatEntry::toMessage)
                .toList();
    }

    @Override
    default void saveAll(String conversationId, List<Message> messages) {
        Chat chat = findById(Long.valueOf(conversationId)).orElseThrow();
        List<ChatEntry> chatEntries = messages.stream()
                .map(ChatEntry::fromMessage)
                .toList();
        chat.addEntries(chatEntries);
        save(chat);
    }

    @Override
    default void deleteByConversationId(String conversationId) {
        //not implemented NEVER
    }
}
