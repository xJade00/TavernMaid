package dev.xjade.tavern.maid.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.dv8tion.jda.api.interactions.commands.Command;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CommandInfo {
  String description();

  String name();

  Permission permission() default Permission.ANY;

  Command.Type type() default Command.Type.SLASH;
}
