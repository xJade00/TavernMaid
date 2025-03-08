package dev.xjade.tavern.maid.commands.button;

import java.util.Map;
import java.util.function.Function;

public class ButtonState {
  private final Map<String, String> state;

  protected ButtonState(final Map<String, String> state) {
    this.state = state;
  }

  public void update(String key, Object value) {
    state.put(key, value.toString());
  }

  private <T> T retrieve(String key, Function<String, T> converter) {
    return converter.apply(state.get(key));
  }

  public boolean bool(String key) {
    return retrieve(key, Boolean::parseBoolean);
  }

  public byte byt(String key) {
    return retrieve(key, Byte::parseByte);
  }

  public char ch(String key) {
    return retrieve(key, s -> s.charAt(0));
  }

  public short s(String key) {
    return retrieve(key, Short::parseShort);
  }

  public int i(String key) {
    return retrieve(key, Integer::parseInt);
  }

  public long l(String key) {
    return retrieve(key, Long::parseLong);
  }

  public float f(String key) {
    return retrieve(key, Float::parseFloat);
  }

  public double d(String key) {
    return retrieve(key, Double::parseDouble);
  }

  public String str(String key) {
    return retrieve(key, s -> s);
  }

  @Override
  public String toString() {
    return "ButtonState{" + "state=" + state + '}';
  }
}
