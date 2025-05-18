package dev.xjade.tavern.maid.commands;

import dev.xjade.tavern.maid.commands.button.ButtonCommand;
import java.lang.reflect.Method;
import java.util.Map;

public record CommandDiscovery(
    Object instance,
    CommandInfo info,
    ButtonCommand buttonCommand,
    Method baseMethod,
    Map<String, Method> subCommands) {}
