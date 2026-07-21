package com.ashok.springagents.controller;

import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.ashok.springagents.model.SummarizeRequest;
import com.ashok.springagents.model.SummarizeResponse;

import jakarta.validation.Valid;

@RestController
public class PromptController {

    private final ChatClient chatClient;
    private final PromptTemplate summarizeTemplate;

    public PromptController(ChatClient.Builder builder,
                            @Value("classpath:prompts/summarize.st") Resource summarizePrompt) {
        this.chatClient = builder.build();
        this.summarizeTemplate = new PromptTemplate(summarizePrompt);
    }

    @PostMapping("/api/summarize")
    public SummarizeResponse summarize(@Valid @RequestBody SummarizeRequest req) {
        Prompt prompt = summarizeTemplate.create(Map.of(
                "input", req.text(),
                "sentenceCount", req.sentenceCount(),
                "audience", req.audience()));

        String reply = chatClient.prompt(prompt).call().content();
        return new SummarizeResponse(reply);
    }
}