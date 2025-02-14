package dev.xjade.tavern;

import dev.xjade.tavern.maid.config.BotConfig;
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

  public void start() throws InterruptedException {
    jda = JDABuilder.createDefault(config.token()).build();
    jda.awaitReady();
  }

  public JDA getJda() {
    return this.jda;
  }
}
