package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import com.yuihara.Item;
import java.awt.Color;
import java.time.Instant;

@Component
public class Stats extends ListenerAdapter {

    public void handleStats(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        
        // 1. Fetch data from MongoDB through Economy Bridge
        int level = Economy.getLevel(userId);
        int xp = Economy.getXP(userId);
        int hp = Economy.getHealth(userId);
        
        // 2. Fetch Equipment Objects
        Item weapon = Items.getItem(Economy.getEquippedWeapon(userId));
        Item armor = Items.getItem(Economy.getEquippedArmor(userId));

        // 3. Calculate Dynamic Attributes
        int weaponAtk = (weapon != null) ? weapon.getAttack() : 0;
        int armorDef = (armor != null) ? armor.getDefense() : 0;
        int totalAtk = 10 + (level * 2) + weaponAtk; // Base + Scale + Gear
        
        // Combat Power formula: (ATK * 1.5) + (DEF * 2) + (LVL * 10)
        double combatPower = (totalAtk * 1.5) + (armorDef * 2.0) + (level * 10);

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("📊 Hero Chronicle: " + event.getAuthor().getName())
            .setThumbnail(event.getAuthor().getEffectiveAvatarUrl())
            .setColor(new Color(142, 68, 173)) // Royal Purple
            .setTimestamp(Instant.now());

        // XP Progress Bar
        int nextLevelXp = level * 100;
        eb.addField("✨ Level " + level, renderProgressBar(xp, nextLevelXp) + " [" + xp + "/" + nextLevelXp + "]", false);

        // Stats Grid
        eb.addField("❤️ Health", hp + "/100", true);
        eb.addField("⚔️ Attack", String.valueOf(totalAtk), true);
        eb.addField("🛡️ Defense", String.valueOf(armorDef), true);

        // Equipment Summary
        String weaponLine = (weapon != null) ? "⚔️ " + weapon.getName() : "👊 Bare Fists";
        String armorLine = (armor != null) ? "🛡️ " + armor.getName() : "👕 Simple Rags";
        eb.addField("🎒 Equipment", weaponLine + "\n" + armorLine, true);

        // The "Thinking Question" / Tip
        String tip = (hp < 30) ? "⚠️ Your health is low! Drink some tea." : 
                     (weaponAtk < 10) ? "💡 Visit the Bazaar to find a better blade." : 
                     "🔥 You are looking strong, traveler.";
        
        eb.addField("🔥 Combat Power: `" + (int)combatPower + "`", tip, false);
        eb.setFooter("Hero ID: " + userId);

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Helper to render a visual [■■■□□] progress bar
     */
    private String renderProgressBar(int current, int target) {
        float percentage = (float) current / target;
        int totalBlocks = 10;
        int filledBlocks = Math.round(percentage * totalBlocks);
        
        StringBuilder bar = new StringBuilder("`[");
        for (int i = 0; i < totalBlocks; i++) {
            if (i < filledBlocks) bar.append("■");
            else bar.append("□");
        }
        return bar.append("]`").toString();
    }
}