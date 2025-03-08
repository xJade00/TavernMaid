package dev.xjade.tavern.maid;

import dev.xjade.tavern.maid.commands.CommandManager;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.events.GuildEvents;
import dev.xjade.tavern.maid.logging.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@ApplicationScoped
public class Bot {
  private JDA jda;
  @Inject private BotConfig config;
  @Inject private Logger logger;
  @Inject private CommandManager commandManager;
  @Inject private GuildEvents guildEvents;

  public void start() throws InterruptedException {
    jda =
        JDABuilder.createDefault(config.token())
            .addEventListeners(commandManager, guildEvents)
            .build();
    jda.awaitReady();
  }

  public JDA getJda() {
    return this.jda;
  }
}
