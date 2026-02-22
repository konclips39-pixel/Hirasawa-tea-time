package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class Eightball extends ListenerAdapter {

    private static final String[] responses = {
        "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes definitely.",
        "The tea leaves are blurry, try again.", "Ask again later.", "Better not tell you now.",
        "Don't count on it.", "My sources say no.", "Outlook not so good."
    };

    public void handleCommand(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        
        // Split to check if there is a question after the command
        String[] parts = message.split("\\s+", 2);
        
        if (parts.length < 2) {
            event.getChannel().sendMessage("🎱 | You have to ask a question! Try `,8ball am i winning?`").queue();
            return;
        }

        // Randomly pick a response
        Random rand = new Random();
        String answer = responses[rand.nextInt(responses.length)];
        
        event.getChannel().sendMessage("🎱 | " + answer).queue();
    }

    // Fixed the stub for you
    public static String getRandomResponse() {
        Random rand = new Random();
        return responses[rand.nextInt(responses.length)];
    }
}