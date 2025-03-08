package dev.xjade.tavern.maid.commands.button;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ButtonStateHolder {

  private static final Map<String, ButtonState> stateHolder = new HashMap<>();

  private ButtonStateHolder() {}

  public static void create(String identifier, Map<String, Object> initialState) {
    String sanitized = identifier.replace("|", "");
    Map<String, String> converted = new HashMap<>();
    for (Map.Entry<String, Object> entry : initialState.entrySet()) {
      converted.put(entry.getKey(), entry.getValue().toString());
    }
    stateHolder.put(identifier, new ButtonState(converted));

    try (ScheduledExecutorService future = Executors.newSingleThreadScheduledExecutor()) {
      future.schedule(() -> timeout(sanitized), 5, TimeUnit.MINUTES);
    }
  }

  public static ButtonState get(String identifier) {
    return stateHolder.get(identifier);
  }

  private static void timeout(String identifier) {
    stateHolder.remove(identifier);
  }
}
