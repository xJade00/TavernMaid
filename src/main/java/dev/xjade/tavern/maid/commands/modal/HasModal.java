package dev.xjade.tavern.maid.commands.modal;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public interface HasModal {
  void handle(ModalInteractionEvent event, ReplyCallbackAction reply);
}
