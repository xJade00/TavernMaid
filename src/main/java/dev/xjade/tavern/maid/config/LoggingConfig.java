package dev.xjade.tavern.maid.config;

import dev.xjade.tavern.maid.logging.Level;
import dev.xjade.tavern.maid.utilities.generators.config.ConfigComment;
import org.jetbrains.annotations.NotNull;

/** Logging information. See below for parameter comments. */
public record LoggingConfig(
    @ConfigComment("The default level to log to. This can be overridden.") String defaultLevel,
    @ConfigComment("The channel for error/critical logs to also go to.")
        long errorCriticalChannel) {

  @NotNull
  public Level getDefaultLevel() {
    if (defaultLevel == null) return Level.DEBUG;
    return Level.valueOf(defaultLevel);
  }
}
