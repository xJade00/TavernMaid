package dev.xjade.tavern.maid.utilities.generators;

import com.typesafe.config.*;
import dev.xjade.tavern.maid.utilities.generators.config.ConfigGenerator;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Setup {
  private static final File CONFIG_DIR = new File("./config");
  private static final File EXAMPLE_CONFIG_DIR = new File("./example_configs");

  public static void main(String[] args) throws Exception {
    runGenerator(ConfigGenerator.class);

    mergeConfigs();

    waitForUserConfirmation();

    List<Class<?>> generators = findGeneratorClasses();
    for (Class<?> generator : generators) {
      runMain(generator);
    }

    runSetupScript();
  }

  private static void waitForUserConfirmation() {
    Scanner scanner = new Scanner(System.in);
    System.out.println(
        "\n📌 Configs have been regenerated! Please review and modify them in the 'config' folder.");
    System.out.println("➡️ Press [Enter] to continue once you have updated the configs...");

    scanner.nextLine(); // Waits until the user presses Enter
  }

  private static void runGenerator(Class<?> generatorClass) {
    if (generatorClass == null) {
      System.err.println("Generator class is null! Skipping...");
      return;
    }

    try {
      runMain(generatorClass);
    } catch (Exception e) {
      System.err.println("Failed to run " + generatorClass.getName() + ": " + e.getMessage());
    }
  }

  private static void runSetupScript() {
    File setupScript = new File("./setup.bat");

    if (!setupScript.exists()) {
      throw new RuntimeException(
          "setup.bat script not found! Checked " + setupScript.getAbsolutePath());
    }

    try {
      ProcessBuilder processBuilder = new ProcessBuilder(setupScript.getAbsolutePath());
      processBuilder.redirectErrorStream(true); // Merge stdout and stderr
      Process process = processBuilder.start();

      // Read and print output
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println("[SETUP] " + line);
        }
      }

      int exitCode = process.waitFor();
      System.out.println("setup.bat finished with exit code: " + exitCode);
    } catch (IOException | InterruptedException e) {
      System.err.println("Failed to run setup.bat: " + e.getMessage());
    }
  }

  private static List<Class<?>> findGeneratorClasses() {
    List<Class<?>> generatorClasses = new ArrayList<>();
    try {
      // Scan for all classes in the package (including subpackages)
      List<Class<?>> allClasses = getAllClassesInPackage(Setup.class.getPackageName());

      for (Class<?> cls : allClasses) {
        if (cls.isAnnotationPresent(Generator.class)
            && !cls.getSimpleName().equals("ConfigGenerator")
            && !cls.isInterface()) {
          generatorClasses.add(cls);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return generatorClasses;
  }

  /** Recursively finds all classes in the given package and subpackages. */
  private static List<Class<?>> getAllClassesInPackage(String packageName)
      throws IOException, ClassNotFoundException {
    List<Class<?>> classes = new ArrayList<>();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);

    while (resources.hasMoreElements()) {
      URL resource = resources.nextElement();
      File directory = new File(resource.getFile());

      if (directory.exists() && directory.isDirectory()) {
        File[] files = directory.listFiles();
        if (files != null) {
          for (File file : files) {
            if (file.getName().endsWith(".class")) {
              String className = packageName + '.' + file.getName().replace(".class", "");
              classes.add(Class.forName(className));
            } else if (file.isDirectory()) {
              // Recursively scan subpackages
              classes.addAll(getAllClassesInPackage(packageName + "." + file.getName()));
            }
          }
        }
      }
    }
    return classes;
  }

  private static void runMain(Class<?> clazz) {
    try {
      Method mainMethod = clazz.getMethod("main", String[].class);
      mainMethod.invoke(null, (Object) new String[] {});
      System.out.println("Ran " + clazz.getName());
    } catch (NoSuchMethodException e) {
      System.err.println("Class " + clazz.getName() + " does not have a valid main method.");
    } catch (IllegalAccessException e) {
      System.err.println("Main method in " + clazz.getName() + " is not accessible.");
    } catch (InvocationTargetException e) {
      System.err.println("Error while running " + clazz.getName() + ":");
      e.getCause().printStackTrace(); // This prints the actual root cause
    } catch (Exception e) {
      System.err.println(
          "Unexpected error while running " + clazz.getName() + ": " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void mergeConfigs() throws IOException {
    if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdir(); // Ensure config directory exists
    if (!EXAMPLE_CONFIG_DIR.exists()) return; // No example configs available

    for (File exampleFile : Objects.requireNonNull(EXAMPLE_CONFIG_DIR.listFiles())) {
      File existingConfig = new File(CONFIG_DIR, exampleFile.getName());

      if (exampleFile.getName().equals("config.conf")) {
        // Directly copy config.conf without merging
        Files.copy(
            exampleFile.toPath(), existingConfig.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Copied config.conf without modification.");
        continue;
      }

      // Load example config (new structure)
      Config exampleConfig = ConfigFactory.parseFile(exampleFile);

      // Load existing config (user-defined values)
      Config existingConfigData =
          existingConfig.exists() ? ConfigFactory.parseFile(existingConfig) : ConfigFactory.empty();

      // Step 1: **Remove old keys** (Keep only keys that exist in exampleConfig)
      Config cleanedConfig = cleanOldKeys(existingConfigData, exampleConfig);

      // Step 2: **Preserve user-defined values while adding missing keys**
      Config mergedConfig = preserveUserValues(cleanedConfig, existingConfigData, exampleConfig);

      // Step 3: **Render config with explicit string quoting and timestamp**
      String formattedConfig = renderWithQuotesAndTimestamp(mergedConfig);

      // Step 4: **Save the final merged config**
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(existingConfig))) {
        writer.write(formattedConfig);
      }
      System.out.println("Updated config: " + existingConfig.getName());
    }
  }

  private static String renderWithQuotesAndTimestamp(Config config) {
    StringBuilder result = new StringBuilder();

    // Format timestamp nicely: "Updated on February 17, 2025 at 12:34 PM"
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
    String formattedTimestamp = LocalDateTime.now().format(formatter);

    result.append("# Updated on ").append(formattedTimestamp).append("\n\n");

    for (Map.Entry<String, ConfigValue> entry : config.root().entrySet()) {
      String key = entry.getKey();
      ConfigValue value = entry.getValue();

      if (value.valueType() == ConfigValueType.STRING) {
        result.append(key).append(" = \"").append(value.unwrapped()).append("\"\n");
      } else {
        result.append(key).append(" = ").append(value.render()).append("\n");
      }
    }
    return result.toString();
  }

  /** **Removes old keys** that no longer exist in the example config. */
  private static Config cleanOldKeys(Config oldConfig, Config newConfig) {
    ConfigObject oldRoot = oldConfig.root();
    ConfigObject newRoot = newConfig.root();

    Map<String, ConfigValue> cleanedMap = new HashMap<>();
    for (Map.Entry<String, ConfigValue> entry : oldRoot.entrySet()) {
      if (newRoot.containsKey(entry.getKey())) {
        cleanedMap.put(entry.getKey(), entry.getValue());
      } else {
        System.out.println("Removing outdated config key: " + entry.getKey());
      }
    }

    return ConfigFactory.parseMap(cleanedMap);
  }

  /** **Preserves user values while adding missing keys from example config**. */
  private static Config preserveUserValues(
      Config cleanedConfig, Config existingConfig, Config exampleConfig) {
    ConfigObject cleanedRoot = cleanedConfig.root();
    ConfigObject existingRoot = existingConfig.root();
    ConfigObject exampleRoot = exampleConfig.root();

    Map<String, ConfigValue> finalConfigMap = new HashMap<>();

    // Step 1: **Keep user-defined values** (from existingConfig)
    for (Map.Entry<String, ConfigValue> entry : existingRoot.entrySet()) {
      finalConfigMap.put(entry.getKey(), entry.getValue());
    }

    // Step 2: **Ensure all example keys exist** (without overwriting user values)
    for (Map.Entry<String, ConfigValue> entry : exampleRoot.entrySet()) {
      finalConfigMap.putIfAbsent(entry.getKey(), entry.getValue()); // Only add if missing
    }

    return ConfigFactory.parseMap(finalConfigMap);
  }
}
