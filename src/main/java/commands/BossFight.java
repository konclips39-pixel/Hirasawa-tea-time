package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import economy.Inventory;
import java.awt.Color;
import java.util.Random;

@Component
public class BossFight extends ListenerAdapter {
    // Shared Boss Stats
    private static int bossHp = 5000;
    private static final int BOSS_MAX_HP = 5000;
    private static String currentBoss = "The Ancient Void Dragon";

    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        Random r = new Random();

        // 1. Check if player has enough health
        if (Economy.getHealth(userId) <= 15) {
            event.getChannel().sendMessage("⚠️ You are too injured to fight a Boss! Use `,heal` first.").queue();
            return;
        }

        // 2. Calculate Damage and Retaliation
        int weaponDmg = Inventory.getWeaponDamage(userId);
        int playerDmg = r.nextInt(20) + 10 + weaponDmg;
        int bossRetaliation = r.nextInt(25) + 15 - Inventory.getArmorDefense(userId);
        if (bossRetaliation < 5) bossRetaliation = 5; 

        // 3. Apply Damage to Boss and Health Loss to Player (MongoDB)
        bossHp -= playerDmg;
        Economy.addHealth(userId, -bossRetaliation);

        // 4. Give participation XP
        int participationXP = 5 + r.nextInt(5);
        Economy.addXP(userId, participationXP);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🐉 WORLD BOSS: " + currentBoss);
        eb.setColor(Color.RED);
        eb.setThumbnail("https://i.imgur.com/8P9H0Wz.png");
        
        // Health Bar Visual
        String healthBar = renderHealthBar(bossHp, BOSS_MAX_HP);
        eb.setDescription("The server is attacking the Boss!\n\n**Boss HP:** " + Math.max(0, bossHp) + " / " + BOSS_MAX_HP + "\n" + healthBar);

        eb.addField("⚔️ Your Attack", "You dealt **" + playerDmg + "** damage!", true);
        eb.addField("🔥 Boss Counter", "You lost **" + bossRetaliation + "** HP and some tea was scorched!", true);

        // 5. Win/Loss Condition
        if (bossHp <= 0) {
            int reward = 10000; // Final blow reward
            int killXP = 500;
            
            // UPDATED: Now uses addTealeafs for MongoDB save
            Economy.addTealeafs(userId, reward); 
            Economy.addXP(userId, killXP);

            eb.addField("🏆 SLAYER!", "You delivered the final blow! You gathered **" + reward + " 🍃 Tealeafs** and gained **" + (killXP + participationXP) + "** XP!", false);
            eb.setColor(Color.BLACK);
            
            // Reset boss
            bossHp = BOSS_MAX_HP; 
        } else {
            eb.setFooter("The Boss is still standing! You earned " + participationXP + " XP for this strike!");
        }

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private static String renderHealthBar(int current, int max) {
        int bars = 10;
        double percentage = (double) Math.max(0, current) / max;
        int filled = (int) Math.ceil(percentage * bars);
        
        StringBuilder sb = new StringBuilder("`[");
        for (int i = 0; i < bars; i++) {
            if (i < filled) sb.append("■");
            else sb.append(" ");
        }
        sb.append("]`");
        return sb.toString();
    }

	public static Object run(MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
}