package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.EmbedBuilder;
import economy.Economy;
import java.awt.Color;
import java.time.Instant;

@Component
public class AdminGive extends ListenerAdapter {

    // Replace this with your actual Discord User ID
    private static final long OWNER_ID = 935181639561150464L;

    // This method handles the command logic
    public void handleCommand(MessageReceivedEvent event, String args) {
        // 1. Security Check
        if (event.getAuthor().getIdLong() != OWNER_ID) {
            event.getChannel().sendMessage("⛔ **Access Denied.** Only the Bot Owner can use this.").queue();
            return;
        }

        // 2. Parse Arguments (,admin-give @user 5000)
        String[] split = args.split("\\s+");
        if (split.length < 2 || event.getMessage().getMentions().getUsers().isEmpty()) {
            event.getChannel().sendMessage("❌ **Usage:** `,admin-give @user <amount>`").queue();
            return;
        }

        try {
            long targetId = event.getMessage().getMentions().getUsers().get(0).getIdLong();
            int amount = Integer.parseInt(split[1]);

            // 3. Update Economy - Changed from addBalance to addTealeafs
            Economy.addTealeafs(targetId, amount);

            // 4. Send Confirmation
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🍃 Admin Tealeaf Injection")
                .setColor(Color.MAGENTA)
                .setDescription("Successfully added **" + amount + " 🍃 Tealeafs** to <@" + targetId + ">'s basket.")
                .setFooter("Authorized System Action")
                .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(eb.build()).queue();

        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("⚠️ **Invalid Amount:** Please enter a whole number.").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("⚠️ **Error:** Failed to update the user's tealeafs.").queue();
        }
    }
}