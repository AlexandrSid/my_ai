package org.aleksid.my_ai.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum Role {
    USER("user") {
        @Override
        public Message createSpecifiedMessage(String content) {
            return new UserMessage(content);
        }
    },
    ASSISTANT("assistant") {
        @Override
        public Message createSpecifiedMessage(String content) {
            return new AssistantMessage(content);
        }
    },
    SYSTEM("system") {
        @Override
        public Message createSpecifiedMessage(String content) {
            return new SystemMessage(content);
        }
    };

    private final String role;

    public static Role getRole(String roleName) {
        return Arrays.stream(Role.values()).filter(role -> role.role.equalsIgnoreCase(roleName)).findFirst().orElseThrow();
    }

    abstract Message createSpecifiedMessage(String content);
}
