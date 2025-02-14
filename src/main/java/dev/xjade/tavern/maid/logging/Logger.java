package dev.xjade.tavern.maid.logging;

import static dev.xjade.tavern.generated.jooq.Tables.CONFIG_OVERRIDES;
import static dev.xjade.tavern.generated.jooq.Tables.LOGGING;

import dev.xjade.tavern.Bot;
import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.config.LoggingConfig;
import dev.xjade.tavern.maid.database.ConfigOverrideKeys;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.LoggingChannelsModel;
import dev.xjade.tavern.maid.database.models.OverrideLoggingLevelModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.jooq.DSLContext;

@Singleton
public class Logger {

  @Inject private BotConfig botConfig;
  @Inject private LoggingConfig loggingConfig;
  @Inject private DSLContext dsl;
  @Inject private Bot bot;

  private Level getLevel(long server) {
    String level =
        dsl.selectFrom(CONFIG_OVERRIDES)
            .where(CONFIG_OVERRIDES.SERVER.eq(server))
            .and(CONFIG_OVERRIDES.KEY.eq(ConfigOverrideKeys.OVERRIDE_LOGGING_LEVEL.name()))
            .fetchOptional()
            .map(ConfigOverridesRecord::getValue)
            .map(json -> JsonbConverter.jsonbToObject(json, OverrideLoggingLevelModel.class))
            .map(OverrideLoggingLevelModel::level)
            .orElse(loggingConfig.defaultLevel());

    return Level.valueOf(level);
  }

  private List<MessageChannel> getChannel(long server) {
    return dsl.selectFrom(CONFIG_OVERRIDES)
        .where(CONFIG_OVERRIDES.SERVER.eq(server))
        .and(CONFIG_OVERRIDES.KEY.eq(ConfigOverrideKeys.LOGGING_CHANNELS.name()))
        .fetchOptional()
        .map(ConfigOverridesRecord::getValue)
        .map(json -> JsonbConverter.jsonbToObject(json, LoggingChannelsModel.class))
        .map(LoggingChannelsModel::channels)
        .map(
            channelIds ->
                channelIds.stream()
                    .map(id -> bot.getJda().getGuildChannelById(ChannelType.TEXT, id))
                    .map(c -> (MessageChannel) c)
                    .toList())
        .orElse(List.of());
  }

  public void log(
      Level requested,
      String description,
      LoggingCategory category,
      LoggingEntry entry,
      long server) {
    System.out.println("Trying to log");
    Level current = getLevel(server);
    dsl.insertInto(LOGGING)
        .set(LOGGING.CATEGORY, category.name())
        .set(LOGGING.DESCRIPTION, description)
        .set(LOGGING.VARIABLES, JsonbConverter.objectToJsonb(entry))
        .set(LOGGING.SERVER, server)
        .execute();

    // We want to log it in the DB, but not in discord
    if (!current.canLog(requested)) {
      System.out.println("Can't log. Current is " + current + " and requested is " + requested);
      return;
    }

    List<MessageChannel> channels = getChannel(server);

    EmbedBuilder builder = new EmbedBuilder();
    builder.setAuthor(entry.author(), entry.authorUrl());
    builder.setTitle(category.getFriendly());
    StringBuilder embedDescription =
        new StringBuilder(description + "\nFound the following formation about this action:\n\n");
    for (Map.Entry<String, String> e : entry.variables().entrySet()) {
      embedDescription.append(e.getKey()).append(": **").append(e.getValue()).append("**\n");
    }
    builder.setDescription(embedDescription.toString());
    builder.setTimestamp(LocalDateTime.now());

    for (MessageChannel channel : channels) {
      channel.sendMessageEmbeds(builder.build()).queue();
    }
  }
}
