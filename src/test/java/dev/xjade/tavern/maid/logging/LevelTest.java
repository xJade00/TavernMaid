package dev.xjade.tavern.maid.logging;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class LevelTest {

  @Test
  public void canLogTest() {
    Level[] levels = Level.values();
    for (Level current : levels) {
      for (Level requested : levels) {
        boolean expected = requested.order() >= current.order();
        assertEquals(
            expected,
            current.canLog(requested),
            String.format("Error. Current [" + current + "]. Requested: [" + requested + "]"));
      }
    }
  }

  @Test
  public void compareTest() {
    Level[] levels = Level.values();
    for (Level current : levels) {
      for (Level requested : levels) {
        boolean expected = requested.order() >= current.order();
        assertEquals(
            expected,
            Level.compare(current, requested),
            String.format("Error. Current [" + current + "]. Requested: [" + requested + "]"));
      }
    }
  }

  @Test
  public void orderTest() {
    assertEquals(
        0, Level.DEBUG.order(), "A level was added before debug. This should never happen.");
    assertEquals(
        Level.values().length - 1,
        Level.CRITICAL.order(),
        "A level was added after CRITICAL. This should never happen.");
  }
}
