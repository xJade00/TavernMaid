package dev.xjade.tavern.maid.commands.context;

import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandDiscovery;
import dev.xjade.tavern.maid.logging.Logger;
import dev.xjade.tavern.maid.logging.LoggingCategory;
import dev.xjade.tavern.maid.logging.LoggingEntry;
import dev.xjade.tavern.maid.utilities.CommandUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@Dependent
public class ContextManager extends ListenerAdapter {
  @Inject private CommandContext ctx;
  @Inject private Logger logger;
  private List<CommandDiscovery> discoveries = List.of();

  @PostConstruct
  public void setup() {
    this.discoveries = CommandUtilities.discoverCommands(ContextCommand.class, ctx);
  }

  @Override
  public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
    for (CommandDiscovery discovery : this.discoveries) {
      if (discovery.instance() instanceof UserContext context) {
        try {
          context.handle(event, event.deferReply());
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
}
