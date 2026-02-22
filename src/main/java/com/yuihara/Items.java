package com.yuihara;

import java.util.HashMap;
import java.util.Map;
import java.util.List; // Added this!
import java.awt.Color;

import commands.Inventory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Items {

    public enum Rarity {
        COMMON(Color.GRAY), 
        UNCOMMON(Color.GREEN), 
        RARE(Color.BLUE), 
        EPIC(new Color(148, 0, 211)), 
        LEGENDARY(Color.ORANGE);

        private final Color color;
        Rarity(Color color) { this.color = color; }
        public Color getColor() { return color; }
    }

    private static final Map<String, Item> registry = new HashMap<>();

    static {
        add(new Item("wood_stick", "Wooden Stick", "A literal stick.", 2, 0, 10, true, Rarity.COMMON));
        add(new Item("rusty_dagger", "Rusty Dagger", "Better than nothing.", 8, 0, 250, true, Rarity.COMMON));
        add(new Item("steel_longsword", "Steel Longsword", "A reliable blade.", 25, 0, 5000, true, Rarity.UNCOMMON));
        add(new Item("katana", "Shadow Katana", "Fast and deadly.", 55, 0, 15000, true, Rarity.RARE));
        add(new Item("void_reaver", "Void Reaver", "Tears through reality.", 150, 0, 100000, true, Rarity.LEGENDARY));

        add(new Item("leather_tunic", "Leather Tunic", "Basic protection.", 0, 5, 500, false, Rarity.COMMON));
        add(new Item("iron_plate", "Iron Platemail", "Heavy and sturdy.", 0, 20, 6000, false, Rarity.UNCOMMON));
        add(new Item("knight_armor", "Paladin Armor", "Blessed by light.", 0, 45, 25000, false, Rarity.EPIC));
        add(new Item("dragon_scale", "Dragon Scale", "Immune to heat.", 10, 80, 80000, false, Rarity.LEGENDARY));
    }

    private static void add(Item item) {
        registry.put(item.getId().toLowerCase(), item);
        registry.put(item.getName().toLowerCase().replace(" ", "_"), item);
    }

    public static Item getItem(String input) {
        if (input == null) return null;
        return registry.get(input.trim().toLowerCase().replace(" ", "_"));
    }

    public static Map<String, Item> getItemRegistry() {
        return registry;
    }

    public static void showInventory(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        
        // This was the line causing the error
        List<String> userItems = Inventory.getOwnedItems(userId); 

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🎒 " + event.getAuthor().getName() + "'s Inventory")
            .setColor(Color.CYAN);

        if (userItems == null || userItems.isEmpty()) {
            eb.setDescription("Your inventory is empty. Use `,shop` to buy something!");
        } else {
            StringBuilder content = new StringBuilder();
            for (String itemId : userItems) {
                Item item = getItem(itemId);
                if (item != null) {
                    content.append("**").append(item.getName()).append("** (").append(item.getRarity()).append(")\n");
                    content.append("└ `").append(item.getId()).append("` | ATK: ").append(item.getAttack()).append(" DEF: ").append(item.getDefense()).append("\n\n");
                }
            }
            eb.setDescription(content.toString());
        }

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
}