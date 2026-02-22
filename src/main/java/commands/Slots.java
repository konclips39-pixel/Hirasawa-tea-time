package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import economy.Economy;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class Slots {

    private static final String[] EMOJIS = {"🍒", "🔔", "🍋", "💎", "⭐", "🍃"};
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random rand = new Random();

    public void handleCommand(MessageReceivedEvent event, String[] args) {
        long userId = event.getAuthor().getIdLong();

        if (args.length == 0) {
            event.getChannel().sendMessage("⚠️ Usage: `,slots <amount>`").queue();
            return;
        }

        int bet;
        try {
            bet = Integer.parseInt(args[0]);
            if (bet < 10) {
                event.getChannel().sendMessage("⚠️ Minimum bet is **10 🍃**").queue();
                return;
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ Invalid bet amount.").queue();
            return;
        }

        if (Economy.getTealeafs(userId) < bet) {
            event.getChannel().sendMessage("❌ You don't have enough Tealeafs!").queue();
            return;
        }

        // Deduct bet immediately
        Economy.addTealeafs(userId, -bet);

        // Initial "Spinning" UI
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🎰 Imperial Slot Machine")
                .setColor(Color.YELLOW)
                .setDescription("```\n[ 🔄 | 🔄 | 🔄 ]\n[ 🔄 | 🔄 | 🔄 ]\n[ 🔄 | 🔄 | 🔄 ]\n```\n*Spinning the wheels...*")
                .setFooter("Bet: " + bet + " 🍃 | User: " + event.getAuthor().getName());

        event.getChannel().sendMessageEmbeds(eb.build()).queue(message -> {
            // Wait 1.5 seconds then show final result (prevents rate-limit spam)
            scheduler.schedule(() -> finalizeSpin(message, userId, bet), 1500, TimeUnit.MILLISECONDS);
        });
    }

    private void finalizeSpin(Message message, long userId, int bet) {
        // Generate a 3x3 grid
        String[][] grid = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = EMOJIS[rand.nextInt(EMOJIS.length)];
            }
        }

        // Logic: Check middle row for win
        boolean win = grid[1][0].equals(grid[1][1]) && grid[1][1].equals(grid[1][2]);
        boolean partial = grid[1][0].equals(grid[1][1]) || grid[1][1].equals(grid[1][2]);
        
        long winnings = 0;
        String resultText;
        Color color;

        if (win) {
            winnings = (long) bet * 5;
            if (grid[1][1].equals("💎")) winnings = (long) bet * 20; // Jackpot for Diamonds
            resultText = "🎉 **BIG WIN!** You earned **" + winnings + " 🍃**";
            color = Color.GREEN;
        } else if (partial) {
            winnings = (long) (bet * 1.5);
            resultText = "✨ **Small Win!** You earned **" + winnings + " 🍃**";
            color = Color.CYAN;
        } else {
            resultText = "💀 **No Luck.** You lost your bet.";
            color = Color.RED;
        }

        if (winnings > 0) Economy.addTealeafs(userId, (int) winnings);

        // Build the 7-line visual (3 rows + borders/info)
        StringBuilder display = new StringBuilder("```\n");
        display.append("  ╔═══════════╗\n");
        for (int i = 0; i < 3; i++) {
            display.append(i == 1 ? "▶ " : "  "); // Indicator for the winning line
            display.append("║ ").append(grid[i][0]).append(" | ").append(grid[i][1]).append(" | ").append(grid[i][2]).append(" ║\n");
        }
        display.append("  ╚═══════════╝\n```");

        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("🎰 Slot Machine Results")
                .setColor(color)
                .setDescription(display.toString() + "\n" + resultText)
                .addField("Tealeafs", "New Balance: **" + Economy.getTealeafs(userId) + " 🍃**", true)
                .setFooter("Bet: " + bet);

        message.editMessageEmbeds(eb.build()).queue();
    }
}