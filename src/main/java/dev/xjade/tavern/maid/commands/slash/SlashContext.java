package dev.xjade.tavern.maid.commands.slash;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SlashContext {
  default void base(SlashCommandInteractionEvent event) {
    event
        .reply(
            "The base for this command has not been setup. \n"
                + "Please contact Jade if you think this is an error.")
        .setEphemeral(true)
        .queue();
  }
}
