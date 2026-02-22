package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import com.yuihara.Item;
import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Leaderboard extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event) {
        // 1. Get all player accounts from MongoDB
        List<PlayerAccount> allPlayers = Economy.getAllPlayers();

        // 2. Sort players by their calculated Combat Power
        List<PlayerAccount> sortedPlayers = allPlayers.stream()
            .sorted((p1, p2) -> Double.compare(calculatePower(p2), calculatePower(p1)))
            .limit(10) // Only show top 10
            .collect(Collectors.toList());

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🏆 Imperial Power Rankings")
            .setColor(new Color(255, 215, 0)) // Trophy Gold
            .setThumbnail("https://i.imgur.com/vR7An9z.png") // Trophy Icon
            .setDescription("The strongest travelers in the Hirasawa Tea Gardens.");

        StringBuilder lbContent = new StringBuilder();
        
        for (int i = 0; i < sortedPlayers.size(); i++) {
            PlayerAccount player = sortedPlayers.get(i);
            double power = calculatePower(player);
            
            // Add Medals for Top 3
            String rankLabel = switch (i) {
                case 0 -> "🥇";
                case 1 -> "🥈";
                case 2 -> "🥉";
                default -> (i + 1) + ".";
            };

            // Attempt to get the username from JDA cache
            String name = event.getJDA().retrieveUserById(player.getUserId()).complete().getName();
            
            lbContent.append(String.format("%s **%s** — `%d CP` (Lvl %d)\n", 
                rankLabel, name, (int)power, player.getLevel()));
        }

        eb.setDescription(lbContent.toString());
        eb.setFooter("Calculated using: (Atk * 1.5) + (Def * 2) + (Lvl * 10)");

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * The Shared Calculation Engine
     * This ensures the Leaderboard and Stats command use the EXACT same math.
     */
    private double calculatePower(PlayerAccount p) {
        Item weapon = Items.getItem(p.getEquippedWeapon());
        Item armor = Items.getItem(p.getEquippedArmor());

        int weaponAtk = (weapon != null) ? weapon.getAttack() : 0;
        int armorDef = (armor != null) ? armor.getDefense() : 0;
        int totalAtk = 10 + (p.getLevel() * 2) + weaponAtk;

        return (totalAtk * 1.5) + (armorDef * 2.0) + (p.getLevel() * 10);
    }
}