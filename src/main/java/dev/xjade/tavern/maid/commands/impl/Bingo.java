package dev.xjade.tavern.maid.commands.impl;

import com.github.kevinsawicki.http.HttpRequest;
import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.commands.Permission;
import dev.xjade.tavern.maid.commands.button.ButtonCommand;
import dev.xjade.tavern.maid.commands.button.ButtonState;
import dev.xjade.tavern.maid.commands.button.HasButton;
import dev.xjade.tavern.maid.commands.modal.HasModal;
import dev.xjade.tavern.maid.commands.modal.ModalCommand;
import dev.xjade.tavern.maid.commands.slash.SlashCommand;
import dev.xjade.tavern.maid.commands.slash.SubCommand;
import dev.xjade.tavern.maid.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

@SlashCommand
@ButtonCommand(prefix = "bingo-signup-", permanent = true)
@ModalCommand(name = "bingo-signup")
@CommandInfo(
    name = "bingo",
    description = "Bingo setup related commands.",
    permission = Permission.WHITELISTED)
public class Bingo implements HasButton, HasModal {

  private CommandContext ctx;

  public static boolean started = false;
  public static List<String> names = new ArrayList<>();

  @SubCommand(name = "start", permission = Permission.WHITELISTED)
  public void start() {
    started = true;
  }

  @SubCommand(name = "end", permission = Permission.WHITELISTED)
  public void end() {
    started = false;
  }

  @SubCommand(name = "signup", permission = Permission.WHITELISTED)
  public void signup(SlashCommandInteractionEvent event) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.setTitle("Bingo Signup");
    builder.setColor(Logger.AQUA);
    builder.setDescription(
        "If there is currently an active bingo, you can sign up using the button below!\nIf you can not make it, you may also cancel your signup below.");
    event
        .replyEmbeds(builder.build())
        .addActionRow(
            Button.success("bingo-signup-signup", "Signup!"),
            Button.danger("bingo-signup-remove", "Remove signup"))
        .queue();
  }

  @Override
  public void handle(
      ButtonInteractionEvent event,
      ReplyCallbackAction reply,
      String sanitizedComponent,
      ButtonState data) {

    TextInput.Builder rsnBuilder =
        TextInput.create("rsn", "RSN", TextInputStyle.SHORT)
            .setPlaceholder("Your RSN.")
            // TODO: Add auto RSN
            //   .setValue(rsn)
            .setMaxLength(12);

    TextInput timezone = TextInput.create("timezone", "Timezone", TextInputStyle.SHORT).build();

    TextInput reasoning =
        TextInput.create("reasoning", "Extra Info", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Any additional info you want captains to know.")
            .setRequired(false)
            .build();

    event
        .replyModal(
            Modal.create("bingo-signup", "Bingo Signup")
                .addComponents(
                    ActionRow.of(rsnBuilder.build()),
                    ActionRow.of(timezone),
                    ActionRow.of(reasoning))
                .build())
        .queue();
  }

  @Override
  public void handle(ModalInteractionEvent event, ReplyCallbackAction reply) {
    reply.setEphemeral(true).setContent("Thanks for signing up!").queue();
    var channel = event.getChannel().asTextChannel();
    String url = channel.retrieveWebhooks().map(List::getFirst).complete().getUrl();

    String body =
        String.format(
            """
                        {
                          "content": "%s\\n%s\\n%s",
                          "username": "%s",
                          "avatar_url": "%s"
                        }
                        """,
            event.getValue("rsn").getAsString(),
            event.getValue("timezone").getAsString(),
            event.getValue("reasoning").getAsString(),
            event.getMember().getEffectiveName(),
            event.getMember().getEffectiveAvatarUrl());

    String bodyy =
        HttpRequest.post(url)
            .contentType("application/json")
            .accept("application/json")
            .send(body)
            .body();

    System.out.println(bodyy);
  }

  private boolean isSignedUp(long id) {
    //   ctx
    //            .dsl()
    //            .selectFrom()
    return false;
  }
}
