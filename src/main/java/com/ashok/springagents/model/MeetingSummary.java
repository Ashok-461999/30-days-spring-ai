package com.ashok.springagents.model;

import java.util.List;

public record MeetingSummary(
        String title,
        List<ActionItem> actionItems,
        List<String> decisions) {}