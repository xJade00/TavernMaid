package dev.xjade.tavern.maid.commands.impl;

import dev.xjade.tavern.maid.commands.*;
import dev.xjade.tavern.maid.commands.button.ButtonCommand;
import dev.xjade.tavern.maid.commands.button.ButtonState;
import dev.xjade.tavern.maid.commands.button.ButtonStateHolder;
import dev.xjade.tavern.maid.commands.button.HasButton;
import dev.xjade.tavern.maid.commands.context.ContextCommand;
import dev.xjade.tavern.maid.commands.context.UserContext;
import dev.xjade.tavern.maid.logging.Logger;
import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@ContextCommand
@ButtonCommand(prefix = "bingo-add-role-")
@CommandInfo(
    type = Command.Type.USER,
    permission = Permission.KEY,
    name = "Add bingo role",
    description = "Adds a special bingo role")
public class AddBingoStaffRole implements UserContext, HasButton {
  private CommandContext ctx;

  private static final String PREFIX;

  static {
    PREFIX = AddBingoStaffRole.class.getAnnotation(ButtonCommand.class).prefix();
  }

  private static final String STAFF = PREFIX + "staff";
  private static final String CAPTAIN = PREFIX + "captain";
  private static final String DEPUTY = PREFIX + "deputy";
  private static final String PLANNER = PREFIX + "planner";
  private static final String CANCEL = PREFIX + "cancel";

  @Override
  public void handle(UserContextInteractionEvent event, ReplyCallbackAction reply) {
    String id = "|" + UUID.randomUUID();
    EmbedBuilder builder = new EmbedBuilder();
    builder.setColor(Logger.AQUA);
    builder.setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl());
    builder.setTitle("Bingo Role");
    builder.setDescription("Assigning " + event.getTarget().getName() + " a bingo role.");
    builder.setTimestamp(Instant.now());
    reply
        .setEphemeral(true)
        .setEmbeds(builder.build())
        .addActionRow(
            Button.success(STAFF + id, "Staff"),
            Button.success(PLANNER + id, "Planner"),
            Button.primary(CAPTAIN + id, "Captain"),
            Button.primary(DEPUTY + id, "Deputy"),
            Button.danger(CANCEL + id, "Cancel"))
        .flatMap(InteractionHook::retrieveOriginal)
        .queue(msg -> ButtonStateHolder.create(id, Map.of("target", event.getTarget().getId())));
  }

  @Override
  public void handle(
      ButtonInteractionEvent event,
      ReplyCallbackAction reply,
      String sanitizedComponent,
      ButtonState data) {
    event.getMessage().delete().queue();
    EmbedBuilder builder = new EmbedBuilder();
    builder.setAuthor(event.getUser().getName(), null, event.getUser().getAvatarUrl());
    builder.setTitle("Bingo Role");
    builder.setTimestamp(Instant.now());

    if (sanitizedComponent.equals(CANCEL)) {
      builder.setColor(Color.RED);
      builder.setDescription("Cancelled!");
      reply.setEmbeds(builder.build()).setEphemeral(true).queue();
      return;
    }

    long targetId = data.l("target");
    Member target = event.getGuild().retrieveMemberById(targetId).complete();
    String name = target.getUser().getName();

    builder.setColor(Logger.AQUA);
    builder.setDescription(
        String.format("Assigned role %s to %s!", event.getComponent().getLabel(), name));
    reply.setEmbeds(builder.build()).setEphemeral(true).queue();
    String bingoRole;
    switch (event.getComponent().getLabel()) {
      case "Staff" -> bingoRole = "Bingo Staff";
      case "Captain", "Deputy" -> bingoRole = "Bingo Captain/Deputy";
      case "Planner" -> bingoRole = "Bingo Planner";
      default ->
          throw new RuntimeException("Label was not expected: " + event.getComponent().getLabel());
    }

    Role role = event.getGuild().getRolesByName(bingoRole, true).getFirst();
    event.getGuild().modifyMemberRoles(target, List.of(role), List.of()).queue();
  }
}
