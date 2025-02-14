package dev.xjade.tavern.maid.logging;

import java.util.Map;

public record LoggingEntry(String author, String authorUrl, Map<String, String> variables) {}
