package dev.xjade.tavern.maid.commands;

import static dev.xjade.tavern.generated.jooq.Tables.CONFIG_OVERRIDES;
import static dev.xjade.tavern.maid.database.ConfigOverrideKeys.WHITELIST;

import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.commands.button.ButtonState;
import dev.xjade.tavern.maid.commands.button.ButtonStateHolder;
import dev.xjade.tavern.maid.commands.button.HasButton;
import dev.xjade.tavern.maid.commands.context.ContextCommand;
import dev.xjade.tavern.maid.commands.context.UserContext;
import dev.xjade.tavern.maid.commands.modal.HasModal;
import dev.xjade.tavern.maid.commands.slash.Option;
import dev.xjade.tavern.maid.commands.slash.SlashCommand;
import dev.xjade.tavern.maid.commands.slash.SubCommand;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.WhitelistModel;
import dev.xjade.tavern.maid.logging.Logger;
import dev.xjade.tavern.maid.logging.LoggingCategory;
import dev.xjade.tavern.maid.logging.LoggingEntry;
import dev.xjade.tavern.maid.utilities.CommandUtilities;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

@Dependent
public class CommandManager extends ListenerAdapter {
    @Inject
    private CommandContext ctx;
    @Inject
    private Logger logger;
    @Inject
    private BotConfig botConfig;
    private List<CommandDiscovery> discoveries = new ArrayList<>();

    @PostConstruct
    public void setup() {
        discoveries.addAll(CommandUtilities.discoverCommands(ContextCommand.class, ctx));
        discoveries.addAll(CommandUtilities.discoverCommands(SlashCommand.class, ctx));
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
                ButtonState data = null;
                if (!discovery.buttonCommand().permanent()) {
                    data = ButtonStateHolder.get("|" + sanitization[1]);
                    if (data == null) {
                        event.getMessage().delete().queue();
                        event
                                .reply("This button has timed out. Please try the command again.")
                                .setEphemeral(true)
                                .queue();
                        return;
                    }
                }
                inst.handle(event, defer, sanitization[0], data);
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        ReplyCallbackAction defer = event.deferReply();
        for (CommandDiscovery discovery : this.discoveries) {
            if (!discovery.info().name().equals("bingo")) continue;
            var inst = (HasModal) discovery.instance();
            inst.handle(event, defer);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Optional<CommandDiscovery> discoveryOpt =
                discoveries.stream().filter(d -> d.info().name().equals(commandName)).findFirst();

        if (discoveryOpt.isEmpty()) return;
        CommandDiscovery discovery = discoveryOpt.get();
        if (!canInteract(discovery.info().permission(), event.getMember(), botConfig, ctx.dsl(), event.getGuild())) {
            event.reply("Sorry, you do not have the permission to use this command! It requires " + discovery.info().permission())
                    .setEphemeral(true)
                    .queue();
            return;
        }
        Object instance = discovery.instance();

        try {
            if (event.getSubcommandName() != null) {
                Method subMethod = discovery.subCommands().get(event.getSubcommandName());
                SubCommand subInfo = subMethod.getAnnotation(SubCommand.class);
                if (!canInteract(subInfo.permission(), event.getMember(), botConfig, ctx.dsl(), event.getGuild())) {
                    event.reply("Sorry, you do not have the permission to use this sub-command! It requires " + subInfo.permission())
                            .setEphemeral(true)
                            .queue();
                    return;
                }

                invokeWithOptions(subMethod, instance, event);
            } else if (discovery.baseMethod() != null) {
                invokeWithOptions(discovery.baseMethod(), instance, event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeWithOptions(Method method, Object instance, SlashCommandInteractionEvent event)
            throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(SlashCommandInteractionEvent.class)) {
                args[i] = event;
            } else {
                args[i] = extractOption(event, parameters[i]);
            }
        }

        method.invoke(instance, args);
    }

    private Object extractOption(SlashCommandInteractionEvent event, Parameter parameter) {
        Option option = parameter.getAnnotation(Option.class);
        if (option == null) return null; // No option annotation, skip

        String optionName = option.name();
        Class<?> type = parameter.getType();
        OptionMapping optionMapping = event.getOption(optionName);

        if (optionMapping == null) return null; // Option not provided by user

        // Infer correct return type
        if (type.equals(String.class)) return optionMapping.getAsString();
        if (type.equals(Integer.class)) return optionMapping.getAsInt();
        if (type.equals(Long.class)) return optionMapping.getAsLong();
        if (type.equals(Boolean.class)) return optionMapping.getAsBoolean();
        if (type.equals(Double.class) || type.equals(Float.class)) return optionMapping.getAsDouble();
        if (type.equals(User.class)) return optionMapping.getAsUser();
        if (type.equals(Member.class)) return optionMapping.getAsMember();
        if (type.equals(GuildChannel.class)) return optionMapping.getAsChannel();
        if (type.equals(Role.class)) return optionMapping.getAsRole();
        if (type.equals(IMentionable.class)) return optionMapping.getAsMentionable();
        if (type.equals(Message.Attachment.class)) return optionMapping.getAsAttachment();

        throw new IllegalArgumentException("Unsupported option type: " + type.getSimpleName());
    }

    public static boolean canInteract(
            Permission permission, Member member, BotConfig cfg, DSLContext dsl, Guild guild) {
        // If anyone can return early
        if (permission == Permission.ANY) return true;

        // Hardcoded owners of the bot. Can interact with anything.
        if (cfg.owners().contains(member.getIdLong())) return true;
        Set<Long> ids =
                dsl
                        .selectFrom(CONFIG_OVERRIDES)
                        .where(CONFIG_OVERRIDES.SERVER.eq(guild.getIdLong()))
                        .and(CONFIG_OVERRIDES.KEY.eq(WHITELIST.name()))
                        .stream()
                        .map(ConfigOverridesRecord::getValue)
                        .findFirst()
                        .map(json -> JsonbConverter.jsonbToObject(json, WhitelistModel.class))
                        .map(WhitelistModel::ids)
                        .orElse(new HashSet<>());

        // Everyone starts with this
        Permission userPermission = Permission.ANY;

        // Determine the user's permission first.
        if (ids.contains(member.getIdLong())) userPermission = Permission.WHITELISTED;
        if (member.getRoles().stream().map(Role::getIdLong).toList().contains(cfg.ids().get(BotConfig.KEY)))
            userPermission = Permission.KEY;
         if(member.getIdLong() == 915539071168823346L) userPermission = Permission.DEV;

        return userPermission.ordinal() >= permission.ordinal();
    }
}
