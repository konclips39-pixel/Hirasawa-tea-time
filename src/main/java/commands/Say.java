package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.util.List;

@Component
public class Say extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event, String content) {
        // 1. Permission Check
        if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().sendMessage("❌ You need `Manage Messages` to use the broadcast system.").queue();
            return;
        }

        if (content.isEmpty()) {
            sendHelp(event);
            return;
        }

        // 2. Identify Target Channel (Optional mention at start or end)
        TextChannel targetChannel = event.getChannel().asTextChannel();
        List<TextChannel> mentionedChannels = event.getMessage().getMentions().getChannels(TextChannel.class);
        
        if (!mentionedChannels.isEmpty()) {
            targetChannel = mentionedChannels.get(0);
            // Remove the channel mention from the content string
            content = content.replace(targetChannel.getAsMention(), "").trim();
        }

        // 3. Advanced Feature: Embed Mode
        // If message starts with "embed|", parse it as an embed
        if (content.toLowerCase().startsWith("embed|")) {
            sendEmbed(targetChannel, content.substring(6));
        } else {
            // Standard Text Mode
            targetChannel.sendMessage(content).queue();
        }

        // 4. Cleanup
        event.getMessage().delete().queue();
    }

    private void sendEmbed(TextChannel channel, String data) {
        String[] parts = data.split("\\|");
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(46, 204, 113)); // Default Tealeaf Green

        // Simple syntax: Title|Description|HexColor(Optional)
        if (parts.length >= 1) eb.setTitle(parts[0]);
        if (parts.length >= 2) eb.setDescription(parts[1]);
        if (parts.length >= 3) {
            try { eb.setColor(Color.decode(parts[2])); } catch (Exception ignored) {}
        }

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    private void sendHelp(MessageReceivedEvent event) {
        event.getChannel().sendMessage("📝 **Broadcast Usage:**\n" +
                "`,say <message> [#channel]` - Send plain text\n" +
                "`,say embed|Title|Description|#HexColor [#channel]` - Send an announcement").queue();
    }
}