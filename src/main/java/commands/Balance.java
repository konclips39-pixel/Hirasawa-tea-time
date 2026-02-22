package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.EmbedBuilder;
import economy.Economy;
import java.awt.Color;

@Component
public class Balance extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();

        // 1. Get the balance using our new "Tealeaf" method
        String tealeafBalance = Economy.getBalanceString(userId);

        // 2. Build the "Tea-themed" Embed
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🍵 Your Tea Stash")
            .setColor(new Color(119, 221, 119)) // A nice tea green color
            .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
            .setDescription("You currently have " + tealeafBalance + " in your basket.")
            .setFooter("Keep gathering to level up!");

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}