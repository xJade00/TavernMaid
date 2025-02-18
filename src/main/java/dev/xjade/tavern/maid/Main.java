package dev.xjade.tavern.maid;

import jakarta.enterprise.inject.se.SeContainer;
import jakarta.enterprise.inject.se.SeContainerInitializer;

public class Main {
  public static void main(String[] args) throws InterruptedException {
    try (SeContainer container = SeContainerInitializer.newInstance().initialize()) {
      Bot bot = container.select(Bot.class).get();
      bot.start();

      bot.getJda().awaitShutdown();
    }
  }
}
