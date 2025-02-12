package dev.xjade.tavern.maid.config;

import dev.xjade.tavern.maid.logging.Level;
import dev.xjade.tavern.maid.utilities.generators.config.ConfigComment;

/** Logging information. See below for parameter comments. */
public record LoggingConfig(
    @ConfigComment("The default level to log to. This can be overridden.") String defaultLevel) {
  public Level getDefaultLevel() {
    return Level.valueOf(defaultLevel);
  }

  // TODO: Add getLevel() when a Database is implemented.

}
