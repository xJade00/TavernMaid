package dev.xjade.tavern.maid.commands.button;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public interface HasButton {
  void handle(
      ButtonInteractionEvent event,
      ReplyCallbackAction reply,
      String sanitizedComponent,
      ButtonState data);
}
