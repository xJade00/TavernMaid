package dev.xjade.tavern.maid.commands.slash;

import dev.xjade.tavern.maid.commands.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SubCommand {
  String name();

  Permission permission();
}
