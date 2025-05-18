package dev.xjade.tavern.maid.events;

import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandDiscovery;
import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.commands.slash.Option;
import dev.xjade.tavern.maid.commands.slash.SlashCommand;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.utilities.CommandUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.jetbrains.annotations.NotNull;

@Dependent
public class GuildEvents extends ListenerAdapter {
  private List<CommandInfo> commandInfo;
  private final List<SlashCommandData> slashCommandData = new ArrayList<>();
  @Inject private BotConfig botConfig;

  @PostConstruct
  public void init() {
    List<CommandDiscovery> slashCommands =
        CommandUtilities.discoverCommands(SlashCommand.class, new CommandContext());
    commandInfo = CommandUtilities.discoverAll(CommandInfo.class);

    for (CommandDiscovery discovery : slashCommands) {
      SlashCommandData commandData =
          Commands.slash(discovery.info().name(), discovery.info().description());

      // Add options for the base command
      if (discovery.baseMethod() != null) {
        for (Parameter param : discovery.baseMethod().getParameters()) {
          if (param.isAnnotationPresent(Option.class)) {
            Option opt = param.getAnnotation(Option.class);
            OptionData option =
                new OptionData(
                    inferOptionType(param.getType()),
                    opt.name(),
                    opt.description(),
                    opt.required());
            commandData.addOptions(option);
          }
        }
      }

      // Add subcommands
      for (Map.Entry<String, Method> entry : discovery.subCommands().entrySet()) {
        SubcommandData subcommand = new SubcommandData(entry.getKey(), "Subcommand");
        for (Parameter param : entry.getValue().getParameters()) {
          if (param.isAnnotationPresent(Option.class)) {
            Option opt = param.getAnnotation(Option.class);
            OptionData option =
                new OptionData(
                    inferOptionType(param.getType()),
                    opt.name(),
                    opt.description(),
                    opt.required());
            subcommand.addOptions(option);
          }
        }
        commandData.addSubcommands(subcommand);
      }

      slashCommandData.add(commandData);
    }
  }

  private OptionType inferOptionType(Class<?> type) {
    if (type.equals(String.class)) return OptionType.STRING;
    if (type.equals(Integer.class) || type.equals(Long.class)) return OptionType.INTEGER;
    if (type.equals(Boolean.class)) return OptionType.BOOLEAN;
    if (type.equals(Double.class) || type.equals(Float.class)) return OptionType.NUMBER;
    if (type.equals(User.class)) return OptionType.USER;
    if (type.equals(Member.class)) return OptionType.USER;
    if (type.equals(GuildChannel.class)) return OptionType.CHANNEL;
    if (type.equals(Role.class)) return OptionType.ROLE;
    if (type.equals(IMentionable.class)) return OptionType.MENTIONABLE;
    if (type.equals(Message.Attachment.class)) return OptionType.ATTACHMENT;

    throw new IllegalArgumentException("Unsupported option type: " + type.getSimpleName());
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

    commands.addAll(slashCommandData);
    event.getGuild().updateCommands().addCommands(commands).queue();
  }
}
