package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import economy.Economy;
import com.yuihara.Item;
import com.yuihara.Items.Rarity; // Import the Rarity we defined in the other file

@Component
public class Items extends ListenerAdapter {

    // 1. The Item Registry: Use String keys for IDs
    private static final Map<String, Item> ITEM_REGISTRY = new HashMap<>();

    static {
        // Weapons: ID, Name, Description, Power, Defense, Price, isWeapon, Rarity
        add(new Item("wood_stick", "Wooden Stick", "A literal stick.", 2, 0, 10, true, Rarity.COMMON));
        add(new Item("steel_longsword", "Steel Longsword", "A sharp blade forged in the capital.", 25, 0, 5000, true, Rarity.UNCOMMON));
        add(new Item("katana", "Shadow Katana", "Fast and deadly.", 55, 0, 15000, true, Rarity.RARE));
        add(new Item("void_reaver", "Void Reaver", "Tears through reality.", 150, 0, 100000, true, Rarity.LEGENDARY));
        
        // Armor
        add(new Item("leather_tunic", "Leather Tunic", "Basic protection.", 0, 5, 500, false, Rarity.COMMON));
        add(new Item("dragon_scale", "Dragon Scale", "Immune to heat.", 10, 80, 80000, false, Rarity.LEGENDARY));
    }

    private static void add(Item item) {
        ITEM_REGISTRY.put(item.getId().toLowerCase(), item);
    }

    public static void execute(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();

        // Fetch data from Economy
        String weaponId = Economy.getEquippedWeapon(userId);
        String armorId = Economy.getEquippedArmor(userId);
        int hp = Economy.getHealth(userId);
        int level = Economy.getLevel(userId);
        int xp = Economy.getXP(userId);

        Item weapon = getItem(weaponId);
        Item armor = getItem(armorId);

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🛡️ Hero Profile: " + event.getAuthor().getName())
            .setColor(new Color(46, 204, 113))
            .setThumbnail(event.getAuthor().getEffectiveAvatarUrl());

        // Dynamic Equipment Strings
        String weaponDisplay = (weapon != null) 
            ? "**" + weapon.getName() + "** [" + weapon.getRarity() + "]\n*+" + weapon.getDamage() + " ATK*" 
            : "*None equipped*";

        String armorDisplay = (armor != null) 
            ? "**" + armor.getName() + "** [" + armor.getRarity() + "]\n*+" + armor.getDefense() + " DEF*" 
            : "*None equipped*";

        eb.addField("⚔️ Main Hand", weaponDisplay, true);
        eb.addField("🛡️ Body Gear", armorDisplay, true);
        eb.addField("❤️ Vitality", renderHealthBar(hp), false);
        eb.setFooter("Level: " + level + " | XP: " + xp + "/" + (level * 100));

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public static Item getItem(String id) {
        if (id == null) return null;
        return ITEM_REGISTRY.get(id.toLowerCase());
    }

    // FIX: This must return the actual registry so Work.java doesn't crash!
    public static Map<String, Item> getItemRegistry() {
        return ITEM_REGISTRY;
    }

    private static String renderHealthBar(int hp) {
        int totalBars = 10;
        int filledBars = Math.max(0, Math.min(totalBars, hp / 10));
        StringBuilder bar = new StringBuilder("`[");
        for (int i = 0; i < totalBars; i++) {
            bar.append(i < filledBars ? "■" : " ");
        }
        return bar.append("]` **").append(hp).append(" HP**").toString();
    }

	public static Object showInventory(MessageReceivedEvent event) {
		return null;
	}
}