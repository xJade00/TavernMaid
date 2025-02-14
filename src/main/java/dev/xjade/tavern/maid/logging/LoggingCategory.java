package dev.xjade.tavern.maid.logging;

public enum LoggingCategory {
  TESTING("Original Testing"),
  TESTING_TWO("Second Testing");

  private final String friendly;

  LoggingCategory(final String friendly) {
    this.friendly = friendly;
  }

  public String getFriendly() {
    return this.friendly;
  }
}
