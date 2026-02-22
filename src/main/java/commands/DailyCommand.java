package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom; 
import java.util.HashMap;
import java.util.Map;

@Component
public class DailyCommand extends ListenerAdapter {

    // Note: These cooldowns reset if the bot restarts. 
    // We can move this to MongoDB later!
    private static final Map<Long, Long> dailyCooldowns = new HashMap<>();
    private static final long COOLDOWN = 24 * 60 * 60 * 1000; // 24 hours

    /**
     * This is the method your listener calls: DailyCommand.run(event);
     */
    public static void run(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        long now = System.currentTimeMillis();
        
        // 1. Cooldown Check
        if (dailyCooldowns.containsKey(userId)) {
            long lastClaim = dailyCooldowns.get(userId);
            if (now - lastClaim < COOLDOWN) {
                long remaining = COOLDOWN - (now - lastClaim);
                long hours = Duration.ofMillis(remaining).toHours();
                long minutes = Duration.ofMillis(remaining).toMinutes() % 60;

                event.getChannel().sendMessage(
                    "⏳ You've already harvested your tea today!\n" +
                    "Your next harvest is in **" + hours + "h " + minutes + "m**."
                ).queue();
                return;
            }
        }

        // 2. Updated Reward Logic: Average 200 (Range 100 to 300)
        int reward = ThreadLocalRandom.current().nextInt(100, 301);
        
        // Save to MongoDB
        Economy.addTealeafs(userId, reward);
        
        // Update cooldown
        dailyCooldowns.put(userId, now);

        event.getChannel().sendMessage(
            "🍵 **Daily Harvest!** You gathered **" + reward + " 🍃 Tealeafs**!\n" +
            "Total Stash: " + Economy.getBalance(userId) // Use getBalance if getBalanceString is missing
        ).queue();
    }
}