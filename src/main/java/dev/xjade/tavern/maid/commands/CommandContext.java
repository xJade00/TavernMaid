package dev.xjade.tavern.maid.commands;

import dev.xjade.tavern.maid.Bot;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.config.LoggingConfig;
import dev.xjade.tavern.maid.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jooq.DSLContext;

@ApplicationScoped
public class CommandContext {
  @Inject private Logger logger;
  @Inject private BotConfig botConfig;
  @Inject private LoggingConfig loggingConfig;
  @Inject private Bot bot;
  @Inject private DSLContext dsl;

  public Logger logger() {
    return this.logger;
  }

  public BotConfig botConfig() {
    return this.botConfig;
  }

  public LoggingConfig loggingConfig() {
    return this.loggingConfig;
  }

  public Bot bot() {
    return this.bot;
  }

  public DSLContext dsl() {
    return this.dsl;
  }
}
