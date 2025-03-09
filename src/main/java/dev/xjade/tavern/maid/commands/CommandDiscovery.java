package dev.xjade.tavern.maid.commands;

import dev.xjade.tavern.maid.commands.button.ButtonCommand;

public record CommandDiscovery(Object instance, CommandInfo info, ButtonCommand buttonCommand) {}
