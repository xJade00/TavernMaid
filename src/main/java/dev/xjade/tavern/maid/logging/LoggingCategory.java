package dev.xjade.tavern.maid.logging;

public enum LoggingCategory {
  NORMAL_ERROR("Error Occurred", "A generic error has occurred"),
  CRITICAL_ERROR("Critical Error Occurred", "A critical unexpected error has occurred"),
  WHITELIST("Whitelist", "The whitelist has been changed.");

  private final String friendly;
  private final String description;

  LoggingCategory(final String friendly, final String description) {
    this.friendly = friendly;
    this.description = description;
  }

  public String getFriendly() {
    return this.friendly;
  }

  public String getDescription() {
    return this.description;
  }
}
