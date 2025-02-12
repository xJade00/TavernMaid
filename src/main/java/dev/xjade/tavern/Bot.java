package dev.xjade.tavern;

import dev.xjade.tavern.maid.config.BotConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

@ApplicationScoped
public class Bot {
  private JDA jda;
  @Inject private BotConfig config;

  public void start() throws InterruptedException {
    jda = JDABuilder.createDefault(config.token()).build();
    jda.awaitReady();
  }
}
