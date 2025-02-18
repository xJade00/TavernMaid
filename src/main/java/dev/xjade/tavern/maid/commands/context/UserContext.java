package dev.xjade.tavern.maid.commands.context;

import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

/** Represents a user context command */
public interface UserContext {
  void handle(UserContextInteractionEvent event, ReplyCallbackAction reply);
}
