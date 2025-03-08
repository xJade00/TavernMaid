package dev.xjade.tavern.maid.events;

import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.utilities.CommandUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

@Dependent
public class GuildEvents extends ListenerAdapter {
  private List<CommandInfo> commandInfo;
  @Inject private BotConfig botConfig;

  @PostConstruct
  public void init() {
    commandInfo = CommandUtilities.discoverAll(CommandInfo.class);
  }

  @Override
  public void onGuildReady(@NotNull GuildReadyEvent event) {
    if (event.getGuild().getIdLong() != botConfig.devServer()) return;

    List<CommandData> commands = new ArrayList<>();
    for (CommandInfo info : commandInfo) {
      if (info.type() != Command.Type.SLASH) {
        commands.add(Commands.context(info.type(), info.name()));
      }
    }
    event.getGuild().updateCommands().addCommands(commands).queue();
  }
}
