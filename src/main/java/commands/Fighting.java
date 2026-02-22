package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import java.awt.Color;
import java.util.Random;
import economy.Economy;
import com.yuihara.Items;
import com.yuihara.Item;

@Component
public class Fighting extends ListenerAdapter {

    /**
     * This is the main method called by your listener switch case.
     * It must be public static void to be accessible without an instance.
     */
    public static void startFight(MessageReceivedEvent event) {
        Random r = new Random();
        long userId = event.getAuthor().getIdLong();

        // 1. Load Player Data from MongoDB
        int userLevel = Economy.getLevel(userId);
        int currentHealth = Economy.getHealth(userId);

        // Health Gate
        if (currentHealth <= 15) {
            event.getChannel().sendMessage("⚠️ You are too exhausted! Drink some tea to recover your HP before fighting.").queue();
            return;
        }

        // 2. Load Gear from Database
        String weaponId = Economy.getEquippedWeapon(userId);
        String armorId = Economy.getEquippedArmor(userId);
        
        Item weapon = (weaponId != null) ? Items.getItem(weaponId) : null;
        Item armor = (armorId != null) ? Items.getItem(armorId) : null;

        // 3. Stats Calculation
        int weaponAtk = (weapon != null) ? weapon.getAttack() : 2; // Default 2 for fists
        int armorDef = (armor != null) ? armor.getDefense() : 1;   // Default 1 for clothes
        String weaponName = (weapon != null) ? weapon.getName().toUpperCase() : "FISTS";

        // 4. Monster Generation
        int monsterTier = (userLevel / 5) + 1;
        String monsterName = getMonsterName(monsterTier, r);

        // 5. ADVANCED COMBAT MATH
        boolean playerDodged = r.nextInt(100) < 10; // 10% Dodge
        boolean isCrit = r.nextInt(100) < 15;      // 15% Critical

        // Player Attack Logic
        int playerBase = r.nextInt(15) + 10 + (userLevel * 2);
        int totalPlayerDmg = (playerBase + weaponAtk) * (isCrit ? 2 : 1);

        // Monster Attack Logic
        int monsterBase = r.nextInt(20) + 10 + (monsterTier * 5); 
        int totalMonsterDmg = playerDodged ? 0 : Math.max(5, monsterBase - armorDef);

        // Update Health in MongoDB
        Economy.addHealth(userId, -totalMonsterDmg);

        // 6. Build the UI
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("⚔️ Dungeon Encounter: " + monsterName);
        eb.setThumbnail("https://i.imgur.com/K3v0m3S.png");
        eb.setColor(isCrit ? Color.ORANGE : (playerDodged ? Color.GREEN : Color.CYAN));

        StringBuilder battleLog = new StringBuilder();
        battleLog.append("🛡️ **Gear:** ").append(weaponName).append(" | DEF: +").append(armorDef).append("\n\n");
        battleLog.append("💥 You dealt **").append(totalPlayerDmg).append("** DMG").append(isCrit ? " **(CRITICAL!)**" : "").append("\n");
        
        if (playerDodged) {
            battleLog.append("💨 **DODGED!** You sidestepped the ").append(monsterName).append("'s attack!");
        } else {
            battleLog.append("👹 ").append(monsterName).append(" dealt **").append(totalMonsterDmg).append("** DMG.");
        }
        
        eb.setDescription(battleLog.toString());

        // 7. Result Logic (Loot & XP)
        if (totalPlayerDmg >= (monsterBase + (monsterTier * 10))) {
            int loot = r.nextInt(400) + 200 + (userLevel * 50);
            int xp = (20 * monsterTier) + r.nextInt(15);
            
            // Saving Tealeafs and XP to MongoDB
            Economy.addTealeafs(userId, loot);
            Economy.addXP(userId, xp);

            eb.addField("🏆 VICTORY", "You harvested **" + loot + " 🍃 Tealeafs** and gained **" + xp + "** XP!", false);
        } else {
            int loss = r.nextInt(100) + 50;
            Economy.addTealeafs(userId, -loss); // Penalize Tealeafs on defeat
            eb.addField("💀 DEFEAT", "The " + monsterName + " overpowered you. You lost **" + loss + " 🍃 Tealeafs** while escaping!", false);
        }

        eb.setFooter("Remaining HP: " + Economy.getHealth(userId) + "/100 | Level: " + userLevel);
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Helper method to generate monster names based on player tier.
     */
    private static String getMonsterName(int tier, Random r) {
        String[][] monsters = {
            {"Shadow Slime", "Lost Spirit", "Thicket Stalker"}, // Tier 1
            {"Tea-Leaf Thief", "Rogue Samurai", "Iron Golem"}, // Tier 2
            {"Ancient Void Dragon", "Imperial Guardian", "Mist Archon"} // Tier 3
        };
        int index = Math.min(tier - 1, monsters.length - 1);
        return monsters[index][r.nextInt(monsters[index].length)];
    }
}