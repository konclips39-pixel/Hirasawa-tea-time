package commands; 

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import economy.Economy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Component
public class Beg extends ListenerAdapter {
    private final Map<Long, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = TimeUnit.MINUTES.toMillis(5); 
    private final Random random = new Random();

    // Changed method name to handle the event properly if using a listener
    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        long currentTime = System.currentTimeMillis();
        
        // 1. Get current balance using your new Tealeaf method
        int userTealeafs = Economy.getTealeafs(userId); 

        // 2. UPDATED LIMIT: Check Tealeafs instead of coins
        if (userTealeafs > 500) {
            event.getChannel().sendMessage("You have " + userTealeafs + " 🍃 Tealeafs. You aren't broke enough to beg! Go work for more.")
                 .queue();
            return;
        }

        // 3. THE COOLDOWN CHECK
        if (cooldowns.containsKey(userId)) {
            long lastUsed = cooldowns.get(userId);
            if (currentTime - lastUsed < COOLDOWN_TIME) {
                long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(COOLDOWN_TIME - (currentTime - lastUsed));
                event.getChannel().sendMessage("Slow down! Try again in " + secondsLeft + "s.").queue();
                return;
            }
        }

        // 4. THE REWARD (Randomized between 10 and 50)
        int rewardAmount = random.nextInt(41) + 10; 
        
        // Update Economy using the new Tealeaf method
        Economy.addTealeafs(userId, rewardAmount);
        
        // Update cooldown and send message
        cooldowns.put(userId, currentTime);
        event.getChannel().sendMessage("A kind stranger felt bad for you and gave you " + rewardAmount + " 🍃 **Tealeafs**!").queue();
    }

	public Object onCommand(MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
}