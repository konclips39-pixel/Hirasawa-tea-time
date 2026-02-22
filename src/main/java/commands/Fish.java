package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

@Component
public class Fish extends ListenerAdapter {

    // We'll use a Map to track cooldowns in memory for now. 
    // (If you want them to survive bot restarts, we can add a 'lastFish' field to EconomyUser later!)
    private final Map<Long, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = TimeUnit.MINUTES.toMillis(10); 
    private final Random random = new Random();

    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        long currentTime = System.currentTimeMillis();

        // 1. Cooldown Check
        if (cooldowns.containsKey(userId)) {
            long lastFish = cooldowns.get(userId);
            if (currentTime - lastFish < COOLDOWN_TIME) {
                long timeLeft = COOLDOWN_TIME - (currentTime - lastFish);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60;
                
                event.getChannel().sendMessage(String.format("🎣 The fish are spooked! Wait **%dm %ds** before casting again.", minutes, seconds)).queue();
                return;
            }
        }

        // 2. Fishing Logic (Random Loot Table)
        int roll = random.nextInt(100);
        String fishName;
        int reward;

        if (roll < 5) { // 5% Legenday
            fishName = "✨ **Golden Oolong Koi**";
            reward = random.nextInt(500) + 500;
        } else if (roll < 20) { // 15% Rare
            fishName = "🍵 **Matcha Bass**";
            reward = random.nextInt(150) + 100;
        } else if (roll < 50) { // 30% Uncommon
            fishName = "🍃 **Jasmine Carp**";
            reward = random.nextInt(50) + 50;
        } else { // 50% Common
            fishName = "🐟 **Regular Minnow**";
            reward = random.nextInt(20) + 10;
        }

        // 3. Update Database (MongoDB) via Economy Bridge
        Economy.addTealeafs(userId, reward);
        
        // 4. Save Cooldown
        cooldowns.put(userId, currentTime);

        // 5. Send Message
        event.getChannel().sendMessage(String.format("🎣 You cast your line and caught a %s! You earned **%d 🍃 Tealeafs**.", fishName, reward)).queue();
    }

	public static Object run(MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
}