package dev.xjade.tavern.maid.events;

import static dev.xjade.tavern.generated.jooq.Tables.CONFIG_OVERRIDES;
import static dev.xjade.tavern.maid.database.ConfigOverrideKeys.FILE_UPLOAD;

import dev.xjade.tavern.generated.jooq.tables.records.ConfigOverridesRecord;
import dev.xjade.tavern.maid.database.JsonbConverter;
import dev.xjade.tavern.maid.database.models.FileUploadBlockerModel;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

@Dependent
public class MessageEvents extends ListenerAdapter {

  @Inject private DSLContext ctx;

  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent event) {
    final FileUploadBlockerModel model =
        ctx
            .selectFrom(CONFIG_OVERRIDES)
            .where(CONFIG_OVERRIDES.SERVER.eq(event.getGuild().getIdLong()))
            .and(CONFIG_OVERRIDES.KEY.eq(FILE_UPLOAD.name()))
            .stream()
            .map(ConfigOverridesRecord::getValue)
            .findFirst()
            .map(json -> JsonbConverter.jsonbToObject(json, FileUploadBlockerModel.class))
            .orElse(new FileUploadBlockerModel(false, List.of(), 0L));

    // Return early if this feature is disabled.
    if (!model.enabled()) return;
    if (event.getMessage().getAttachments().isEmpty()) return;

    boolean canPostDueToRole = false;
    if (model.minRole() > 0) {
      int pos = event.getGuild().getRoleById(model.minRole()).getPosition();
      int highest =
          event.getMember().getRoles().stream()
              .map(Role::getPosition)
              .mapToInt(i -> i)
              .max()
              .orElse(-1);
      canPostDueToRole = highest >= pos;
    }

    // Return early if they can post it because they have the minimum role
    if (canPostDueToRole) return;

    boolean canPostDueToTypes = true;
    if (!model.allowedExtensions().isEmpty()) {
      for (Message.Attachment attachment : event.getMessage().getAttachments()) {
        if (!model.allowedExtensions().contains(attachment.getFileExtension())) {
          canPostDueToTypes = false;
        }
      }
    }

    // Return early if it's exclusively allowed types.
    if (canPostDueToTypes) return;

    event.getMessage().delete().queue();
  }
}
