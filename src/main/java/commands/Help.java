package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.time.Instant;

@Component
public class Help extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🍵 Hirasawa Tea Time | Integrated Manual")
            .setColor(new Color(101, 67, 33)) 
            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())
            .setAuthor("System Version 3.2.0-FLASH", null, event.getJDA().getSelfUser().getAvatarUrl())
            .setDescription("Welcome to the **Imperial Tea Gardens**. This manual contains all protocols for navigating the economy, RPG systems, and social guidelines.\n\n" +
                            "**📑 Quick Start Guide**\n" +
                            "• Earn **Tealeafs (🍃)** via `,job work` or `,slots`.\n" +
                            "• Check your current status with `,stats` or `,inv`.\n" +
                            "• Equip better gear from the `,shop` to increase **Combat Power**.\n" +
                            "• Compete with others on the `,leaderboard`.\n\n" +
                            "**Status:** `System Online` | **Latency:** `" + event.getJDA().getGatewayIntents().size() + " Intents`")
            .addField("📋 General", "Basic utilities and server info.", true)
            .addField("🍃 Economy", "Trade, Gamble, and Earn.", true)
            .addField("⚔️ RPG", "Combat, Gear, and Levels.", true)
            .setFooter("Requested by " + event.getAuthor().getName() + " • Navigate using buttons below")
            .setTimestamp(Instant.now());

        // We add the buttons. We'll check for Admin permission to show the Staff button.
        boolean isAdmin = event.getMember().hasPermission(Permission.ADMINISTRATOR);

        event.getChannel().sendMessageEmbeds(eb.build())
            .setActionRow(
                Button.primary("help_gen", "General"),
                Button.success("help_eco", "Economy"),
                Button.danger("help_rpg", "RPG"),
                isAdmin ? Button.secondary("help_mod", "Staff Admin") : Button.secondary("help_mod", "Staff").asDisabled()
            ).queue();
    }
}