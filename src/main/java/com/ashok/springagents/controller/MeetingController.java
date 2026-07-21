package com.ashok.springagents.controller;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ashok.springagents.model.MeetingSummary;

@RestController
public class MeetingController {

    private final ChatClient chatClient;

    public MeetingController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @PostMapping("/api/meeting/extract")
    public MeetingSummary extract(@RequestBody Map<String, String> request) {
        return chatClient.prompt()
                .user("Extract the structured data from these meeting notes:\n\n"
                        + request.get("notes"))
                .call()
                .entity(MeetingSummary.class);
    }
}
