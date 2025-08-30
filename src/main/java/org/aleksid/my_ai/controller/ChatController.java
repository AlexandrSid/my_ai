package org.aleksid.my_ai.controller;

import lombok.RequiredArgsConstructor;
import org.aleksid.my_ai.model.Chat;
import org.aleksid.my_ai.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping()
    public String mainPage(Model model) {
        model.addAttribute("chats", chatService.getAllChats());
        return "chat";
    }

    @GetMapping("/{chatId}")
    public String showChat(@PathVariable Long chatId, Model model) {
        model.addAttribute("chats", chatService.getAllChats());
        model.addAttribute("chat", chatService.findById(chatId));
        return "chat";
    }

    @PostMapping("/new")
    public String newChat(@RequestParam String title, Model model) {
        Chat newChat = chatService.createNewChat(title);
        model.addAttribute("chat", newChat);
        return "redirect:/chat/" + newChat.getId();
    }

    @PostMapping("{chatId}/delete")
    public String deleteChat(@PathVariable Long chatId) {
        chatService.deleteChat(chatId);
        return "redirect:/chat";
    }
}
