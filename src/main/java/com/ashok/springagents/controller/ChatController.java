package com.ashok.springagents.controller;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;


    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("You are a concise, helpful assistant.")
                .build();
    }

    @PostMapping("/api/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String reply = chatClient.prompt()
                .user(request.get("message"))
                .call()
                .content();

        return Map.of("reply", reply);
    }
}
