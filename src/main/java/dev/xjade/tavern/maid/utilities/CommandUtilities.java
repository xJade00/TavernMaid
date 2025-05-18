package dev.xjade.tavern.maid.utilities;

import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandDiscovery;
import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.commands.button.ButtonCommand;
import dev.xjade.tavern.maid.commands.slash.SubCommand;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.build.*;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class CommandUtilities {

  public static List<CommandDiscovery> discoverCommands(
      Class<? extends Annotation> annotationClass, CommandContext ctx) {

    List<CommandDiscovery> discoveredCommands = new ArrayList<>();

    // Scan classpath for classes annotated with @SlashCommand
    Reflections reflections =
        new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.TypesAnnotated));

    Set<Class<?>> commandClasses = reflections.getTypesAnnotatedWith(annotationClass);

    for (Class<?> clazz : commandClasses) {
      try {
        // Instantiate the command class
        Object commandInstance = clazz.getDeclaredConstructor().newInstance();

        // Inject CommandContext if needed
        injectContextField(clazz, commandInstance, ctx);

        // Retrieve @CommandInfo metadata
        CommandInfo commandInfo = clazz.getAnnotation(CommandInfo.class);
        if (commandInfo == null) {
          throw new RuntimeException("CommandInfo must be present on all commands");
        }

        // Retrieve ButtonCommand (optional)
        ButtonCommand buttonCommand = clazz.getAnnotation(ButtonCommand.class);

        // Find base method (if present)
        Method baseMethod =
            Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals("base"))
                .findFirst()
                .orElse(null);

        // Find subcommands
        Map<String, Method> subcommands = new HashMap<>();
        for (Method method : clazz.getDeclaredMethods()) {
          if (method.isAnnotationPresent(SubCommand.class)) {
            SubCommand subInfo = method.getAnnotation(SubCommand.class);
            subcommands.put(subInfo.name(), method);
          }
        }

        // Add discovered command
        discoveredCommands.add(
            new CommandDiscovery(
                commandInstance, commandInfo, buttonCommand, baseMethod, subcommands));
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
