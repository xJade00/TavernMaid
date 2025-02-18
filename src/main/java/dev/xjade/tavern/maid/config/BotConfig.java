package dev.xjade.tavern.maid.config;

import dev.xjade.tavern.maid.utilities.generators.config.ConfigComment;
import java.util.List;

/** Represents vital information for the bot to run. See below for parameter comments. */
public record BotConfig(
    @ConfigComment("Discord authentication token") String token,
    @ConfigComment("Hard owners, access can't be removed.") List<Long> owners,
    @ConfigComment("Dev server ID.") long devServer,
    @ConfigComment("Dev ID.") long dev) {}
