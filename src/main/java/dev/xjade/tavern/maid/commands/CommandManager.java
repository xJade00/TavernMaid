package dev.xjade.tavern.maid.commands;

import dev.xjade.tavern.maid.commands.button.ButtonState;
import dev.xjade.tavern.maid.commands.button.ButtonStateHolder;
import dev.xjade.tavern.maid.commands.button.HasButton;
import dev.xjade.tavern.maid.commands.context.ContextCommand;
import dev.xjade.tavern.maid.commands.context.UserContext;
import dev.xjade.tavern.maid.logging.Logger;
import dev.xjade.tavern.maid.logging.LoggingCategory;
import dev.xjade.tavern.maid.logging.LoggingEntry;
import dev.xjade.tavern.maid.utilities.CommandUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;

@Dependent
public class CommandManager extends ListenerAdapter {
  @Inject private CommandContext ctx;
  @Inject private Logger logger;
  private List<CommandDiscovery> discoveries = List.of();

  @PostConstruct
  public void setup() {
    this.discoveries = CommandUtilities.discoverCommands(ContextCommand.class, ctx);
  }

  @Override
  public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
    if (!(event.getChannel() instanceof MessageChannel)) {
      return;
    }
    logger.debug(
        "User context command activated.\nName: %s\nTarget: %s (%s)\nUser: %s (%s)\nChannel: %s (%s)",
        event.getGuild().getIdLong(),
        event.getName(),
        event.getTarget().getName(),
        event.getTarget().getIdLong(),
        event.getUser().getName(),
        event.getUser().getIdLong(),
        event.getChannel().getName(),
        event.getChannel().getIdLong());
    for (CommandDiscovery discovery : this.discoveries) {
      if (discovery.instance() instanceof UserContext context) {
        try {
          if (discovery.info().name().equals(event.getName())) {
            context.handle(event, event.deferReply());
          }
        } catch (Exception ex) {
          logger.error(
              LoggingCategory.CRITICAL_ERROR,
              LoggingEntry.from(
                  event.getUser(),
                  Map.of("Message", ex.getMessage(), "Class", context.getClass().getSimpleName())),
              event.getGuild().getIdLong());
        }
      }
    }
  }

  @Override
  public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
    ReplyCallbackAction defer = event.deferReply();
    for (CommandDiscovery discovery : this.discoveries) {
      if (discovery.buttonCommand() == null) continue;

      if (event.getComponentId().startsWith(discovery.buttonCommand().prefix())) {
        var inst = (HasButton) discovery.instance();
        String[] sanitization = event.getComponentId().split("\\|");
        ButtonState data = ButtonStateHolder.get("|" + sanitization[1]);
        if (data == null) {
          event.getMessage().delete().queue();
          event
              .reply("This button has timed out. Please try the command again.")
              .setEphemeral(true)
              .queue();
          return;
        }
        inst.handle(event, defer, sanitization[0], data);
      }
    }
  }
}
