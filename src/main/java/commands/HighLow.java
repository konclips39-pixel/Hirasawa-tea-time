package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.awt.Color;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HighLow extends ListenerAdapter {

    // Store the secret number and hint for each user session
    private static final ConcurrentHashMap<Long, GameData> sessions = new ConcurrentHashMap<>();

    private static class GameData {
        int hint;
        int secret;
        GameData(int hint, int secret) {
            this.hint = hint;
            this.secret = secret;
        }
    }

    public void handleCommand(MessageReceivedEvent event) {
        Random r = new Random();
        long userId = event.getAuthor().getIdLong();

        int hint = r.nextInt(100) + 1; // 1-100 for more challenge
        int secret = r.nextInt(100) + 1;

        // Ensure they aren't the same number
        while (secret == hint) {
            secret = r.nextInt(100) + 1;
        }

        sessions.put(userId, new GameData(hint, secret));

        // Logic check for the "Thinking Question"
        String advice = (hint < 30) ? "The odds favor **Higher**." : 
                        (hint > 70) ? "The odds favor **Lower**." : 
                        "This is a tough one. Trust your gut.";

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🔮 Tea Master's Intuition")
            .setColor(Color.CYAN)
            .setThumbnail("https://i.imgur.com/mUvK7eS.png") // Cool crystal ball or tea icon
            .setDescription("The hint number is **" + hint + "**.\n" +
                            "Is the secret number **Higher** or **Lower** than " + hint + "?")
            .addField("Intuition Note", "💡 " + advice, false)
            .setFooter("Winner gets 150 🍃 Tealeafs! | Range: 1-100");

        event.getChannel().sendMessageEmbeds(eb.build())
            .setActionRow(
                Button.primary("hl_higher_" + userId, "Higher ⬆️"),
                Button.danger("hl_lower_" + userId, "Lower ⬇️")
            ).queue();
    }

    // You would handle the ButtonInteractionEvent in your main listener
    // and call a result method like this:
    public static void processResult(long userId, String choice, net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent event) {
        GameData data = sessions.remove(userId);
        if (data == null) {
            event.reply("Session expired! Start a new game.").setEphemeral(true).queue();
            return;
        }

        boolean won = (choice.equals("higher") && data.secret > data.hint) ||
                      (choice.equals("lower") && data.secret < data.hint);

        EmbedBuilder eb = new EmbedBuilder();
        if (won) {
            int reward = 150;
            Economy.addTealeafs(userId, reward);
            eb.setTitle("✅ Correct Intuition!")
              .setColor(Color.GREEN)
              .setDescription("The secret number was **" + data.secret + "**.\n" +
                              "You correctly guessed it was " + choice + " than " + data.hint + "!")
              .addField("Reward", "+ " + reward + " 🍃 Tealeafs", false);
        } else {
            eb.setTitle("❌ Miscalculated...")
              .setColor(Color.RED)
              .setDescription("The secret number was actually **" + data.secret + "**.\n" +
                              "Better luck next time, apprentice.");
        }

        event.editMessageEmbeds(eb.build()).setComponents().queue();
    }

	public static Object run(MessageReceivedEvent event) {
		return null;
	}
}