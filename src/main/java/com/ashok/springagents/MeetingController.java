package com.ashok.springagents;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeetingController {


    public record ActionItem(String task, String owner, String deadline) {}

    public record MeetingSummary(
            String title,
            List<ActionItem> actionItems,
            List<String> decisions) {}

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
                .entity(MeetingSummary.class);  // <- typed object, not a String
    }
}