package dev.xjade.tavern.maid.utilities.generators.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import dev.xjade.tavern.maid.config.BotConfig;
import dev.xjade.tavern.maid.utilities.generators.Generator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Generator
// Made with the help of AI. Cause it's complicated and not worth the time for what it does.
public class ConfigGenerator {
  private static final String OUTPUT_DIR = "./example_configs/";

  public static void generateConfigs(String packageName) {
    try {
      File directory = new File(OUTPUT_DIR);

      // Ensure the directory exists and clean old config files
      if (directory.exists()) {
        for (File file : directory.listFiles()) {
          if (file.isFile() && file.getName().endsWith(".conf")) {
            file.delete();
          }
        }
      } else {
        directory.mkdirs();
      }

      List<Class<?>> records = getPackageRecords(packageName);
      StringBuilder mainConfigContent = new StringBuilder();

      for (Class<?> record : records) {
        String fileName = record.getSimpleName() + ".conf";
        String filePath = OUTPUT_DIR + fileName;
        String hoconContent = generateHocon(record);
        saveToFile(filePath, hoconContent);

        // Add include statement to main config
        mainConfigContent.append("include \"").append(fileName).append("\"\n");
      }

      // Save main config file with includes
      saveToFile(OUTPUT_DIR + "config.conf", mainConfigContent.toString());

      System.out.println("Example configs generated in: " + OUTPUT_DIR);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static List<Class<?>> getPackageRecords(String packageName) {
    try {
      return ClassPathScanner.getClasses(packageName).stream()
          .filter(Class::isRecord)
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Failed to scan package: " + packageName, e);
    }
  }

  private static String generateHocon(Class<?> recordClass) {
    Config config = ConfigFactory.empty();
    StringBuilder hoconWithComments = new StringBuilder();

    for (RecordComponent component : recordClass.getRecordComponents()) {
      String name = component.getName();
      Class<?> type = component.getType();
      Object defaultValue = getDefaultForType(type);

      // Fetch @ConfigComment annotation if available
      String comment = getAnnotationComment(recordClass, name);
      if (comment != null) {
        hoconWithComments.append("# ").append(comment).append("\n");
      }

      // Append field assignment after the comment
      hoconWithComments.append(name).append(" = ").append(formatValue(defaultValue)).append("\n\n");

      config = config.withValue(name, ConfigValueFactory.fromAnyRef(defaultValue));
    }

    return hoconWithComments.toString();
  }

  private static String formatValue(Object value) {
    if (value instanceof String) {
      return "\"" + value + "\"";
    }
    if (value instanceof List<?> list) {
      return list.toString(); // assumes primitives, you could improve this for nested objects
    }
    if (value instanceof Map<?, ?> map) {
      StringBuilder sb = new StringBuilder("{\n");
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        Object k = entry.getKey();
        Object v = entry.getValue();
        sb.append("  ").append(k).append(" = ").append(formatValue(v)).append("\n");
      }
      sb.append("}");
      return sb.toString();
    }
    return value.toString();
  }


  private static String getAnnotationComment(Class<?> recordClass, String fieldName) {
    try {
      Field field = recordClass.getDeclaredField(fieldName);
      ConfigComment annotation = field.getAnnotation(ConfigComment.class);
      return annotation != null ? annotation.value() : null;
    } catch (NoSuchFieldException e) {
      return null;
    }
  }

  private static Object getDefaultForType(Class<?> type) {
    if (type == String.class) return "default_value";
    if (type == int.class || type == Integer.class) return 0;
    if (type == long.class || type == Long.class) return 0L;
    if (type == double.class || type == Double.class) return 0.0;
    if (type == boolean.class || type == Boolean.class) return false;
    if (List.class.isAssignableFrom(type)) return List.of();
    if (Map.class.isAssignableFrom(type)) return Map.of();
    return "unknown";
  }

  private static void saveToFile(String filePath, String content) {
    try (FileWriter writer = new FileWriter(filePath)) {
      writer.write(content);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    generateConfigs(BotConfig.class.getPackageName());
  }
}
