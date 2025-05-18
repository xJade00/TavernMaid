package dev.xjade.tavern.maid.commands.impl;

import static dev.xjade.tavern.generated.jooq.Tables.CONFIG_OVERRIDES;
import static dev.xjade.tavern.maid.database.ConfigOverrideKeys.FILE_UPLOAD;

import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.commands.CommandContext;
import dev.xjade.tavern.maid.commands.CommandInfo;
import dev.xjade.tavern.maid.commands.Permission;
import dev.xjade.tavern.maid.commands.slash.Option;
import dev.xjade.tavern.maid.commands.slash.SlashCommand;
import dev.xjade.tavern.maid.commands.slash.SubCommand;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.FileUploadBlockerModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jooq.DSLContext;

@SlashCommand
@CommandInfo(
    name = "fileblock",
    description = "Block unwanted file uploading.",
    permission = Permission.KEY)
public class UploadFileBlockerCommand {
  private CommandContext ctx;

  @SubCommand(name = "toggle", permission = Permission.KEY)
  public void toggle(SlashCommandInteractionEvent event) {
    final FileUploadBlockerModel model = getModel(event.getGuild().getIdLong(), ctx.dsl());
    final FileUploadBlockerModel updatedModel =
        new FileUploadBlockerModel(!model.enabled(), model.allowedExtensions(), model.minRole());

    ctx.dsl()
        .update(CONFIG_OVERRIDES)
        .set(CONFIG_OVERRIDES.KEY, FILE_UPLOAD.name())
        .set(CONFIG_OVERRIDES.SERVER, event.getGuild().getIdLong())
        .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(updatedModel))
        .execute();

    event
        .reply(
            "Toggled file upload blocking. It is now: **"
                + (updatedModel.enabled() ? "Enabled" : "Disabled")
                + "**")
        .setEphemeral(true)
        .queue();
  }

  @SubCommand(name = "role", permission = Permission.KEY)
  public void role(
      SlashCommandInteractionEvent event,
      @Option(
              name = "role",
              description = "The minimum role to bypass this block.",
              required = true)
          Role role) {
    final FileUploadBlockerModel model = getModel(event.getGuild().getIdLong(), ctx.dsl());
    final FileUploadBlockerModel updatedModel =
        new FileUploadBlockerModel(model.enabled(), model.allowedExtensions(), role.getIdLong());

    ctx.dsl()
        .update(CONFIG_OVERRIDES)
        .set(CONFIG_OVERRIDES.KEY, FILE_UPLOAD.name())
        .set(CONFIG_OVERRIDES.SERVER, event.getGuild().getIdLong())
        .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(updatedModel))
        .execute();
    event
        .reply("Updated minimum role. It is now: **" + (role.getName()) + "**")
        .setEphemeral(true)
        .queue();
  }

  @SubCommand(name = "types", permission = Permission.KEY)
  public void allowedTypes(
      SlashCommandInteractionEvent event,
      @Option(
              name = "types",
              description = "The allowed filetypes, comma separated",
              required = true)
          String types) {
    final FileUploadBlockerModel model = getModel(event.getGuild().getIdLong(), ctx.dsl());
    final FileUploadBlockerModel updatedModel =
        new FileUploadBlockerModel(
            model.enabled(), Arrays.asList(types.split(", ?")), model.minRole());

    ctx.dsl()
        .update(CONFIG_OVERRIDES)
        .set(CONFIG_OVERRIDES.KEY, FILE_UPLOAD.name())
        .set(CONFIG_OVERRIDES.SERVER, event.getGuild().getIdLong())
        .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(updatedModel))
        .execute();
    event.reply("Updated allowed file types.").setEphemeral(true).queue();
  }

  private static FileUploadBlockerModel getModel(long guild, DSLContext dsl) {
    final Optional<FileUploadBlockerModel> model =
        dsl
            .selectFrom(CONFIG_OVERRIDES)
            .where(CONFIG_OVERRIDES.SERVER.eq(guild))
            .and(CONFIG_OVERRIDES.KEY.eq(FILE_UPLOAD.name()))
            .stream()
            .map(ConfigOverridesRecord::getValue)
            .findFirst()
            .map(json -> JsonbConverter.jsonbToObject(json, FileUploadBlockerModel.class));
    final FileUploadBlockerModel empty = new FileUploadBlockerModel(false, new ArrayList<>(), 0L);
    if (model.isEmpty()) {
      dsl.insertInto(CONFIG_OVERRIDES)
          .set(CONFIG_OVERRIDES.KEY, FILE_UPLOAD.name())
          .set(CONFIG_OVERRIDES.SERVER, guild)
          .set(CONFIG_OVERRIDES.VALUE, JsonbConverter.objectToJsonb(empty))
          .execute();
      return empty;
    }

    return model.get();
  }
}
