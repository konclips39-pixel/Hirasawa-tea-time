package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.util.Random;
import java.util.Arrays;

@Component
public class Dice extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event) {
        Random r = new Random();
        int[] rolls = new int[5];
        for (int i = 0; i < 5; i++) rolls[i] = r.nextInt(6) + 1;

        long userId = event.getAuthor().getIdLong();
        String result = "🎲 You rolled: **" + Arrays.toString(rolls) + "**";
        
        // Check if all dice match (Jackpot)
        boolean allMatch = Arrays.stream(rolls).allMatch(n -> n == rolls[0]);
        
        if (allMatch) {
            int jackpot = 5000;
            // UPDATED: Save to MongoDB
            Economy.addTealeafs(userId, jackpot);
            
            event.getChannel().sendMessage(result + "\n😱 **JACKPOT!** All dice match! You won **" + jackpot + " 🍃 Tealeafs**!").queue();
        } else {
            int win = r.nextInt(100) + 20;
            // UPDATED: Save to MongoDB
            Economy.addTealeafs(userId, win);
            
            event.getChannel().sendMessage(result + "\nSmall win: You got **" + win + " 🍃 Tealeafs**.").queue();
        }
    }

	public static Object run(MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
}