package dev.xjade.tavern.maid.logging;

import static dev.xjade.tavern.generated.jooq.Tables.*;

import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.Bot;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.config.LoggingConfig;
import dev.xjade.tavern.maid.database.ConfigOverrideKeys;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.LoggingChannelsModel;
import dev.xjade.tavern.maid.database.models.OverrideLoggingLevelModel;
import dev.xjade.tavern.maid.utilities.DebugEncoder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

  /**
   * Gets the error/critical channel for logging.
   *
   * @return The dev server logging channel.
   */
  private TextChannel getCriticalErrorChannel() {
    Guild guild = bot.getJda().getGuildById(botConfig.devServer());
    return guild.getTextChannelById(loggingConfig.errorCriticalChannel());
  }

  public void debug(String description, long server, Object... formats) {
    String formatted = String.format(description, formats);
    dsl.insertInto(DEBUG)
        .set(DEBUG.DESCRIPTION_COMPRESSED, DebugEncoder.compressZstd(formatted))
        .set(DEBUG.SERVER, server)
        .set(DEBUG.TIME, LocalDateTime.now())
        .execute();
  }

  public void info(LoggingCategory category, LoggingEntry entry, long server) {
    log(Level.INFO, category.getDescription(), category, entry, server);
  }

  public void error(LoggingCategory category, LoggingEntry entry, long server) {
    log(Level.ERROR, category.getDescription(), category, entry, server);
  }

  public void critical(LoggingCategory category, LoggingEntry entry, long server) {
    log(Level.CRITICAL, category.getDescription(), category, entry, server);
  }

  private void log(
      Level requested,
      String description,
      LoggingCategory category,
      LoggingEntry entry,
      long server) {
    if (requested == Level.DEBUG) {
      debug(description, server);
      return;
    }
    Level current = getLevel(server);
    dsl.insertInto(LOGGING)
        .set(LOGGING.CATEGORY, category.name())
        .set(LOGGING.DESCRIPTION, description)
        .set(LOGGING.TIME, LocalDateTime.now())
        .set(LOGGING.VARIABLES, JsonbConverter.objectToJsonb(entry))
        .set(LOGGING.SERVER, server)
        .execute();

    List<MessageChannel> channels = getChannel(server);

    EmbedBuilder builder = new EmbedBuilder();
    builder.setFooter(requested.name());
    builder.setAuthor(entry.author(), null, entry.authorUrl());
    builder.setTitle(category.getFriendly());
    StringBuilder embedDescription =
        new StringBuilder(
            description + "\n\nFound the following information about this action:\n\n");
    for (Map.Entry<String, String> e : entry.variables().entrySet()) {
      embedDescription.append(e.getKey()).append(": **").append(e.getValue()).append("**\n");
    }
    builder.setDescription(embedDescription.toString());
    builder.setTimestamp(Instant.now());
    if (requested == Level.CRITICAL) {
      // Red
      builder.setColor(16711680);
    } else {
      // #249999
      builder.setColor(2398617);
    }

    if (requested == Level.CRITICAL || requested == Level.ERROR) {
      getCriticalErrorChannel().sendMessageEmbeds(builder.build()).queue();
    }

    if (!current.canLog(requested)) {
      System.out.println("Can't log. Current is " + current + " requested is " + requested);
      return;
    }

    for (MessageChannel channel : channels) {
      channel.sendMessageEmbeds(builder.build()).queue();
    }
  }
}
