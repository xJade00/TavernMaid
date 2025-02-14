package dev.xjade.tavern.maid.database;

import com.google.gson.Gson;
import org.jooq.JSONB;

public class JsonbConverter {
  private static final Gson gson = new Gson();

  /** Convert JSONB to a Java object of type T. */
  public static <T> T jsonbToObject(JSONB jsonb, Class<T> clazz) {
    if (jsonb == null) return null;
    try {
      return gson.fromJson(jsonb.data(), clazz);
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert JSONB to " + clazz.getSimpleName(), e);
    }
  }

  /** Convert a Java object to JSONB. */
  public static <T> JSONB objectToJsonb(T object) {
    if (object == null) return null;
    try {
      return JSONB.jsonb(gson.toJson(object));
    } catch (Exception e) {
      throw new RuntimeException("Failed to convert object to JSONB", e);
    }
  }
}
