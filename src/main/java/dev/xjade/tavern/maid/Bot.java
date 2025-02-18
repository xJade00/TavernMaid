package dev.xjade.tavern.maid;

import dev.xjade.tavern.maid.commands.context.ContextManager;
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
  @Inject private ContextManager contextManager;
  @Inject private GuildEvents guildEvents;

  public void start() throws InterruptedException {
    jda =
        JDABuilder.createDefault(config.token())
            .addEventListeners(contextManager, guildEvents)
            .build();
    jda.awaitReady();
  }

  public JDA getJda() {
    return this.jda;
  }
}
