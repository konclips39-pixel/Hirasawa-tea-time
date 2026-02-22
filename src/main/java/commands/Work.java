package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import economy.Economy;
import com.yuihara.Items;
import com.yuihara.Item;
import java.awt.Color;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Work {

    public static void execute(MessageReceivedEvent event) {
        Random r = new Random();
        long userId = event.getAuthor().getIdLong();
        
        // --- 1. Fetch Stats directly from the Static Economy Layer ---
        int currentLevel = Economy.getLevel(userId);
        long lastWork = Economy.getLastWorkTime(userId);
        long currentTime = System.currentTimeMillis();

        // --- 2. Cooldown Calculation ---
        long baseCooldown = TimeUnit.MINUTES.toMillis(30);
        long reduction = (currentLevel > 50) ? TimeUnit.MINUTES.toMillis(3) : 0;
        long finalCooldown = baseCooldown - reduction;

        if (currentTime - lastWork < finalCooldown) {
            long timeLeft = finalCooldown - (currentTime - lastWork);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeft);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeft) % 60;
            
            event.getChannel().sendMessage(String.format("⏳ **Shift Overload!** Stamina recovers in **%d:%02d**.", 
                minutes, seconds)).queue();
            return;
        }

        // --- 3. Pay & XP Logic ---
        double shiftQuality = 0.8 + (r.nextDouble() * 0.7); 
        int basePay = (int) ((r.nextInt(31) + 20) * shiftQuality);
        int totalPay = basePay + (currentLevel * 15);
        int xpGained = 15 + (currentLevel / 2);

        // --- 4. Database Persistence ---
        Economy.addTealeafs(userId, totalPay);
        Economy.addXP(userId, xpGained);
        Economy.setLastWorkTime(userId, currentTime);

        // --- 5. Narrative Building ---
        String job = getJobByLevel(currentLevel, r);
        String qualityText = getQualityDescription(shiftQuality);

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("💼 Professional Shift: " + job)
            .setColor(shiftQuality > 1.3 ? Color.GREEN : new Color(46, 204, 113))
            .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
            .setDescription(String.format(
                "**Performance:** %s\n" +
                "💰 **Earnings:** %d 🍃 Tealeafs\n" +
                "✨ **Experience:** +%d XP\n", 
                qualityText, totalPay, xpGained));

        // --- 6. Loot Handling ---
        if (r.nextInt(100) < 8) { 
            handleLootDrop(userId, eb, r);
        }

        eb.setFooter("Level " + Economy.getLevel(userId) + " | Efficiency: " + (int)(shiftQuality * 100) + "%");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private static void handleLootDrop(long userId, EmbedBuilder eb, Random r) {
        // Safe check for the item registry to avoid null pointers
        Map<String, Item> registry = Items.getItemRegistry();
        if (registry == null || registry.isEmpty()) return;

        // Convert registry values to a List specifically of type <Item>
        List<Item> allItems = new ArrayList<>(registry.values());
        Item found = allItems.get(r.nextInt(allItems.size()));
        
        // Update database via our fixed Economy method
        Economy.addToInventory(userId, found.getId());

        boolean isLegendary = found.getPrice() > 5000;
        String lootMsg = isLegendary 
            ? "\n⭐ **LEGENDARY FIND!** You discovered a **" + found.getName() + "**!" 
            : "\n📦 **Item Found:** You picked up a **" + found.getName() + "**.";
            
        eb.appendDescription(lootMsg);
        if (isLegendary) eb.setColor(Color.ORANGE);
    }

    private static String getQualityDescription(double q) {
        if (q > 1.4) return "⭐ **Flawless Execution**";
        if (q > 1.1) return "✅ **Highly Efficient**";
        if (q > 0.9) return "👍 **Standard Performance**";
        return "⚠️ **Subpar Effort**";
    }

    private static String getJobByLevel(int level, Random r) {
        String[] low = {"Tea Picker", "Garden Sweeper", "Pot Washer"};
        String[] mid = {"Brew Master", "Caravan Guard", "Shop Manager"};
        String[] high = {"Imperial Sommelier", "Tea Lord", "Dynasty Advisor"};

        if (level < 15) return low[r.nextInt(low.length)];
        if (level < 40) return mid[r.nextInt(mid.length)];
        return high[r.nextInt(high.length)];
    }
}