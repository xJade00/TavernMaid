package dev.xjade.tavern.maid.commands;

import dev.xjade.tavern.maid.commands.button.ButtonState;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public interface HasButton {
  void handle(
      ButtonInteractionEvent event,
      ReplyCallbackAction reply,
      String sanitizedComponent,
      ButtonState data);
}
