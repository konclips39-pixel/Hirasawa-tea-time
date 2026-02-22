package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.util.Map;
import economy.Economy;
import com.yuihara.Item;

@Component
public class Shop extends ListenerAdapter {

    public static void showShop(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🍵 The Imperial Tea Bazaar")
            .setDescription("Welcome, traveler. Trade your gathered leaves for powerful artifacts.\n" +
                            "Use `,buy <item_id>` to purchase.")
            .setColor(new Color(241, 196, 15)) // Gold
            .setThumbnail("https://i.imgur.com/39A8v7L.png");

        // We pull the registry from your Items class
        Map<String, Item> registry = Items.getItemRegistry();
        
        StringBuilder weapons = new StringBuilder();
        StringBuilder armor = new StringBuilder();
        StringBuilder items = new StringBuilder();

        registry.forEach((id, item) -> {
            String entry = String.format("**%s** (`%s`)\n┕ 🍃 **%d** | %s\n\n", 
                item.getName(), id, item.getPrice(), 
                item.isWeapon() ? "ATK: +" + item.getAttack() : "DEF: +" + item.getDefense());

            if (item.isWeapon()) weapons.append(entry);
            else if (item.getDefense() > 0) armor.append(entry);
            else items.append(entry);
        });

        if (weapons.length() > 0) eb.addField("⚔️ Weapons", weapons.toString(), false);
        if (armor.length() > 0) eb.addField("🛡️ Protective Gear", armor.toString(), false);
        if (items.length() > 0) eb.addField("🧪 Provisions", items.toString(), false);

        eb.setFooter("Your Balance: " + Economy.getTealeafs(userId) + " 🍃", event.getAuthor().getEffectiveAvatarUrl());

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}