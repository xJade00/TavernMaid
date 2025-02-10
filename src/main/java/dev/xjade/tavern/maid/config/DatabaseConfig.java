package dev.xjade.tavern.maid.config;

import dev.xjade.tavern.maid.config.generator.ConfigComment;

/** Database information. See below for parameter comments. */
public record DatabaseConfig(
    @ConfigComment("The JDBC URL.") String url,
    @ConfigComment("The username of the database user.") String username,
    @ConfigComment("The password of the database user.") String password) {}
