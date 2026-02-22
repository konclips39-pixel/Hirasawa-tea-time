package commands;

import com.yuihara.Item;
import com.yuihara.Items;
import economy.Economy;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Inventory extends ListenerAdapter {

    /**
     * The main display command for Discord
     */
    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        
        // 1. Fetch raw IDs from MongoDB via your Economy Bridge
        List<String> rawItemIds = extracted(userId);

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🎒 " + event.getAuthor().getName() + "'s Tea Basket")
                .setColor(new Color(110, 80, 50)) // Elegant Wood Brown
                .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now());

        if (rawItemIds == null || rawItemIds.isEmpty()) {
            eb.setDescription("*Your basket is currently empty. Visit the shop to collect some leaves!*");
        } else {
            // 2. Group items by ID to count them (e.g., "Tea" -> 5)
            Map<String, Integer> counts = new HashMap<>();
            for (String id : rawItemIds) {
                counts.put(id, counts.getOrDefault(id, 0) + 1);
            }

            // 3. Build Category Strings
            StringBuilder gear = new StringBuilder();
            StringBuilder consumables = new StringBuilder();
            StringBuilder materials = new StringBuilder();

            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                Item item = Items.getItem(entry.getKey());
                if (item == null) continue; // Skip items that no longer exist in code

                String line = String.format("• **%s** x%d (ID: `%s`)\n", 
                        item.getName(), entry.getValue(), item.getId());

                // Advanced Categorization Logic
                if (item.getAttack() > 0 || item.getDefense() > 0) {
                    gear.append(line);
                } else if (item.getName().toLowerCase().contains("tea") || item.getName().toLowerCase().contains("pot")) {
                    consumables.append(line);
                } else {
                    materials.append(line);
                }
            }

            // 4. Populate Embed Fields only if they aren't empty
            if (gear.length() > 0) eb.addField("⚔️ Equipment", gear.toString(), false);
            if (consumables.length() > 0) eb.addField("🍵 Consumables", consumables.toString(), false);
            if (materials.length() > 0) eb.addField("📦 Miscellaneous", materials.toString(), false);
            
            eb.setFooter("Balance: " + Economy.getTealeafs(userId) + " 🍃 | Total Items: " + rawItemIds.size());
        }

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private List<String> extracted(long userId) {
		return null;
	}

	@SuppressWarnings("unchecked")
    public static List<String> getOwnedItems(long userId) {
        // This calls the bridge to your MongoDB collection
        Object inventoryObj = Economy.getUserInventory(userId);
        if (inventoryObj instanceof List) {
            return (List<String>) inventoryObj;
        }
        return List.of(); // Return empty list if null
    }

    public static void addItem(long userId, String itemId) {
        Economy.addToInventory(userId, itemId);
        System.out.println("[DB] Granted item " + itemId + " to " + userId);
    }

    public static boolean removeItem(long userId, String itemId) {
        return Economy.removeFromInventory(userId, itemId);
    }
}