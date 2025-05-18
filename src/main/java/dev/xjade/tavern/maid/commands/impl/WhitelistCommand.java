package dev.xjade.tavern.maid.commands.impl;

import static dev.xjade.tavern.generated.jooq.Tables.CONFIG_OVERRIDES;
import static dev.xjade.tavern.maid.database.ConfigOverrideKeys.WHITELIST;

import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.commands.Permission;
import dev.xjade.tavern.maid.commands.context.ContextCommand;
import dev.xjade.tavern.maid.commands.context.UserContext;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.WhitelistModel;
import dev.xjade.tavern.maid.logging.LoggingCategory;
import dev.xjade.tavern.maid.logging.LoggingEntry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ContextCommand
@CommandInfo(
    type = Command.Type.USER,
    permission = Permission.KEY,
    name = "Whitelist User to Bot",
    description = "Whitelists a user to use the bot that is outside of the normal role.")
public class WhitelistCommand implements UserContext {

  private CommandContext ctx;

  @Override
  public void handle(UserContextInteractionEvent event, ReplyCallbackAction reply) {
    final long guildId = event.getGuild().getIdLong();

    if (ctx.botConfig().owners().contains(event.getUser().getIdLong())) {
      ctx.logger().debug("WhitelistCommand tried to whitelist a hardcoded owner.", guildId);
      reply.setContent("You can not whitelist a hard-coded owner.").setEphemeral(true).queue();
      return;
    }

    Set<Long> ids =
        ctx
            .dsl()
            .selectFrom(CONFIG_OVERRIDES)
            .where(CONFIG_OVERRIDES.SERVER.eq(guildId))
            .and(CONFIG_OVERRIDES.KEY.eq(WHITELIST.name()))
            .stream()
            .map(ConfigOverridesRecord::getValue)
            .findFirst()
            .map(json -> JsonbConverter.jsonbToObject(json, WhitelistModel.class))
            .map(WhitelistModel::ids)
            .orElse(new HashSet<>());

    if (ids.contains(event.getTarget().getIdLong())) {
      ctx.logger()
          .debug(
              "WhitelistCommand tried to whitelist someone who is already whitelisted.", guildId);
      reply.setContent("That person is already whitelisted!").setEphemeral(true).queue();
      return;
    }

    if (event.getTarget().isBot()) {
      ctx.logger().debug("WhitelistCommand tried to whitelist a bot.", guildId);
      reply.setContent("You can't whitelist a bot!").setEphemeral(true).queue();
    }

    ids.add(event.getTarget().getIdLong());
    reply
        .setContent("Whitelisted " + event.getTarget().getEffectiveName() + ".")
        .setEphemeral(true)
        .queue();

    ctx.dsl()
        .insertInto(CONFIG_OVERRIDES)
        .set(CONFIG_OVERRIDES.KEY, WHITELIST.name())
        .set(CONFIG_OVERRIDES.SERVER, guildId)
        .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(new WhitelistModel(ids)))
        .onConflict(CONFIG_OVERRIDES.KEY, CONFIG_OVERRIDES.SERVER)
        .doUpdate()
        .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(new WhitelistModel(ids)))
        .where(CONFIG_OVERRIDES.KEY.eq(WHITELIST.name()))
        .and(CONFIG_OVERRIDES.SERVER.eq(guildId))
        .execute();

    ctx.logger()
        .info(
            LoggingCategory.WHITELIST,
            LoggingEntry.from(event.getUser(), Map.of("Added user", event.getTarget().getName())),
            guildId);
  }
}
