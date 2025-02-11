package dev.xjade.tavern.maid.config;

import dev.xjade.tavern.maid.utilities.generators.config.ConfigComment;
import java.util.List;

/** Represents vital information for the bot to run. See below for parameter comments. */
public record BotConfig(
    @ConfigComment("Discord authentication token") String token,
    @ConfigComment("Hard owners, access can't be removed.") List<Long> owners,
    @ConfigComment("The main server ID.") long server) {
  /**
   * Returns whether a user with a specified ID is a hard owner. A hard owner is specified as
   * someone with access that can't be removed.
   *
   * @param id The ID of the user.
   * @return If the user is a hard owner.
   */
  public boolean isHardOwner(long id) {
    return owners.contains(id);
  }

  // TODO: Add a getOwners command when Database is made
}
