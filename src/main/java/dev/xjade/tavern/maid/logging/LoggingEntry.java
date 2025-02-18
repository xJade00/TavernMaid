package dev.xjade.tavern.maid.logging;

import java.util.Map;
import net.dv8tion.jda.api.entities.User;

public record LoggingEntry(String author, String authorUrl, Map<String, String> variables) {
  public static LoggingEntry from(User user, Map<String, String> variables) {
    return new LoggingEntry(user.getName(), user.getAvatarUrl(), variables);
  }
}
