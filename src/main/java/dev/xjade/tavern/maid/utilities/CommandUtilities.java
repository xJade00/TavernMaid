package dev.xjade.tavern.maid.utilities;

import dev.xjade.tavern.maid.commands.ButtonCommand;
import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandDiscovery;
import dev.xjade.tavern.maid.commands.CommandInfo;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class CommandUtilities {

  public static List<CommandDiscovery> discoverCommands(
      Class<? extends Annotation> annotationClass, CommandContext ctx) {
    List<CommandDiscovery> discoveredCommands = new ArrayList<>();

    // Scan the classpath for all classes annotated with the specified annotation
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated));

    Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(annotationClass);

    for (Class<?> clazz : commandClasses) {
      try {
        // Instantiate the class
        Object commandInstance = clazz.getDeclaredConstructor().newInstance();

        // Inject CommandContext into 'ctx' field if it exists
        injectContextField(clazz, commandInstance, ctx);

        // Retrieve @CommandInfo metadata
        CommandInfo commandInfo = clazz.getAnnotation(CommandInfo.class);
        if (commandInfo == null) {
          throw new RuntimeException("CommandInfo must be present on all commands");
        }

        ButtonCommand buttonCommand = clazz.getAnnotation(ButtonCommand.class);

        // Add discovered command
        discoveredCommands.add(new CommandDiscovery(commandInstance, commandInfo, buttonCommand));
        System.out.println("Discovered command: " + commandInfo.name());

      } catch (Exception e) {
        System.err.println("Failed to process " + clazz.getName() + ": " + e.getMessage());
      }
    }

    return discoveredCommands;
  }

  public static <T extends Annotation> List<T> discoverAll(Class<T> clazzToFind) {
    List<T> commandInfos = new ArrayList<>();

    // Scan the classpath for all classes annotated with @CommandInfo
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated));

    Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(clazzToFind);

    for (Class<?> clazz : commandClasses) {
      T commandInfo = clazz.getAnnotation(clazzToFind);
      if (commandInfo != null) {
        commandInfos.add(commandInfo);
      }
    }

    return commandInfos;
  }

  private static void injectContextField(Class<?> clazz, Object instance, CommandContext ctx) {
    try {
      Field ctxField = clazz.getDeclaredField("ctx");
      ctxField.setAccessible(true);
      ctxField.set(instance, ctx);
    } catch (NoSuchFieldException ignored) {
      // If the field doesn't exist, just ignore it
    } catch (IllegalAccessException e) {
      System.err.println(
          "Failed to inject context into " + clazz.getName() + ": " + e.getMessage());
    }
  }
}
