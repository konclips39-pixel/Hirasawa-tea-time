package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.EmbedBuilder;
import economy.Economy;
import java.awt.Color;
import java.time.Instant;

@Component
public class AdminTake extends ListenerAdapter {

    // Your confirmed Discord ID
    private static final long OWNER_ID = 935181639561150464L;

    public void handleCommand(MessageReceivedEvent event, String args) {
        // 1. Security Check
        if (event.getAuthor().getIdLong() != OWNER_ID) {
            event.getChannel().sendMessage("⛔ **Access Denied.** System adjustments are owner-only.").queue();
            return;
        }

        // 2. Parse Arguments (,admin-take @user 5000)
        String[] split = args.split("\\s+");
        if (split.length < 2 || event.getMessage().getMentions().getUsers().isEmpty()) {
            event.getChannel().sendMessage("❌ **Usage:** `,admin-take @user <amount>`").queue();
            return;
        }

        try {
            long targetId = event.getMessage().getMentions().getUsers().get(0).getIdLong();
            int amount = Integer.parseInt(split[1]);

            // 3. Update Economy - Now using your Tealeafs logic
            // We pass -amount to subtract from their total
            Economy.addTealeafs(targetId, -amount);

            // 4. Send Confirmation
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🧨 Admin Tealeaf Removal")
                .setColor(Color.RED)
                .setDescription("Successfully removed **" + amount + " 🍃 Tealeafs** from <@" + targetId + ">'s basket.")
                .setFooter("System Balance Corrected")
                .setTimestamp(Instant.now());

            event.getChannel().sendMessageEmbeds(eb.build()).queue();

        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("⚠️ **Invalid Amount:** Please enter a whole number.").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("⚠️ **Error:** Failed to modify user tealeafs.").queue();
        }
    }

	public static Object execute(MessageReceivedEvent event, String args) {
		// TODO Auto-generated method stub
		return null;
	}
}