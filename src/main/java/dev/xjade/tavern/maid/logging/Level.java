package dev.xjade.tavern.maid.logging;

/** Levels for logging purposes. */
public enum Level {
  /** Debug, only turned on to find the most important stuff */
  DEBUG,
  /** Error is for any errors that aren't critical to application safety */
  ERROR,
  /** Info is for general information that is important */
  INFO,
  /** Critical is for things that are critical to the application. */
  CRITICAL;

  /**
   * Requests to see if the log is currently set to log in short-term storage.
   *
   * @param requested The requested {@link Level}.
   * @return If the log should be sent to short-term storage.
   */
  public boolean canLog(Level requested) {
    return this.compareTo(requested) <= 0;
  }

  /**
   * A static version of {@link Level#canLog(Level)}.
   *
   * @param current The current level the bot is on.
   * @param requested The requested level.
   * @return If the log should be sent to short-term storage.
   */
  public static boolean compare(Level current, Level requested) {
    return current.canLog(requested);
  }

  public int order() {
    Level[] levels = values();
    for (int i = 0; i < levels.length; i++) {
      if (levels[i] == this) {
        return i;
      }
    }
    return -1;
  }
}
