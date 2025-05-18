package dev.xjade.tavern.maid.injection;

import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.config.DatabaseConfig;
import dev.xjade.tavern.maid.config.LoggingConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

/** Providers. Will split if need be. */
@ApplicationScoped
public class GenericProviders {

  private Config loadConfig(String name) {
    File config = new File(String.format("./config/%s.conf", name));
    if (!config.exists()) {
      throw new RuntimeException("Config file " + config.getName() + " not found. Please create.");
    }
    return ConfigFactory.parseFile(config).resolve();
  }

  @Produces
  public BotConfig loadBotConfig() {
    Config config = loadConfig("BotConfig");
    Map<String, Long> importantIds =
        config.getConfig("ids").entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, e -> config.getConfig("ids").getLong(e.getKey())));
    return new BotConfig(
        config.getString("token"),
        config.getLongList("owners"),
        config.getLong("devServer"),
        config.getLong("dev"),
        importantIds);
  }

  @Produces
  public DatabaseConfig loadDatabaseConfig() {
    Config config = loadConfig("DatabaseConfig");
    return new DatabaseConfig(
        config.getString("url"), config.getString("username"), config.getString("password"));
  }

  @Produces
  public LoggingConfig loadLoggingConfig() {
    Config config = loadConfig("LoggingConfig");
    return new LoggingConfig(
        config.getString("defaultLevel"), config.getLong("errorCriticalChannel"));
  }

  @Produces
  public Gson loadGson() {
    return new Gson();
  }
}
