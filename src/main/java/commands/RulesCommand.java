package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.time.Instant;

@Component
public class RulesCommand extends ListenerAdapter {

    /**
     * This is the method your listener calls. 
     * It is now static and contains the actual sending logic.
     */
    public static void sendRules(MessageReceivedEvent event) {
        // 1. Check for Admin Permissions
        if (event.getMember() == null || !event.getMember().hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ Only Admins can post the official rules.").queue();
            return;
        }

        // 2. Send the Embed and Buttons
        event.getChannel().sendMessageEmbeds(getRulesEmbed())
            .setActionRow(
                Button.success("rules_verify", "I have read the rules! ✅"),
                Button.link("https://discord.com/terms", "Official Discord ToS 📜")
            )
            .queue();
        
        // 3. Cleanup: Delete the trigger message (,rules)
        event.getMessage().delete().queue();
    }

    public static MessageEmbed getRulesEmbed() {
        return new EmbedBuilder()
            .setTitle("🍵 [ HIRASAWA TEA TIME | SERVER RULES ]")
            .setColor(new Color(255, 85, 85)) 
            .setThumbnail("https://media.tenor.com/M6Lw1O3fH88AAAAC/yui-k-on.gif")
            .setDescription("Welcome to the club! Please read and adhere to the guidelines below.\n" +
                    "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
            
            // Rule 1 now contains the embedded link
            .addField("📜 [ 1. Discord Terms of Service ]", 
                "Members must follow all [Discord TOS](https://discord.com/terms). No distribution of illegal content or gore.", false)
            
            .addField("🗂️ [ 2. Channel Organization ]", "Keep conversations in their designated areas. Use #Spam for high-frequency messaging.", false)
            .addField("🤝 [ 3. Respect & Conduct ]", "No bullying or harassment. We maintain a zero-tolerance policy for targeted attacks.", false)
            .addField("💬 [ 4. Language & Boundaries ]", "Edgy humor is permitted, but not for harassment. Stop immediately if asked.", false)
            .addField("🩹 [ 5. Sensitive Topics ]", "Heavy topics must stay in designated channels and use spoilers (||text||).", false)
            .addField("🌈 [ 6. Inclusive Environment ]", "This is an LGBTQ+ friendly community. Bigotry is not tolerated.", false)
            .addField("🇬🇧 [ 7. English Only ]", "Public channels must remain in English for moderation safety.", false)
            .addField("🔞 [ 8. SFW General Channels ]", "Main hubs stay SFW. No sexually explicit material or fetishes.", false)
            .addField("⚖️ [ 9. Staff Authority ]", "Moderators reserve the right to intervene. Staff decisions are final.", false)
            .addField("🆘 [ 10. Security & Support ]", "Ping staff if you feel unsafe or see a violation.", false)
            
            .setFooter("Tea Time Staff", null)
            .setTimestamp(Instant.now())
            .build();
    }
}