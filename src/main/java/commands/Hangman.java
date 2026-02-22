package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.awt.Color;
import java.util.*;

@Component
public class Hangman extends ListenerAdapter {
    private final Map<Long, GameState> activeGames = new HashMap<>();
    private final String[] WORDS = {"JAVA", "DISCORD", "TEALEAF", "DASHBOARD", "SPRINGBOOT", "ECLIPSE", "DATABASE", "MONGODB"};

    private static class GameState {
        String word;
        Set<Character> guesses = new TreeSet<>(); // TreeSet keeps them alphabetical
        int lives = 6;
        int reward = 250;

        GameState(String word) {
            this.word = word;
        }
    }

    public void handleCommand(MessageReceivedEvent event, String[] args) {
        long userId = event.getAuthor().getIdLong();

        // 1. Start a new game if no arguments
        if (args.length == 0) {
            if (!activeGames.containsKey(userId)) {
                String randomWord = WORDS[new Random().nextInt(WORDS.length)];
                activeGames.put(userId, new GameState(randomWord));
                sendGameEmbed(event, userId, "🎮 **Hangman Started!** Good luck.");
            } else {
                sendGameEmbed(event, userId, "⚠️ You already have a game running!");
            }
            return;
        }

        // 2. Process Guess
        if (activeGames.containsKey(userId)) {
            String input = args[0].toUpperCase();
            char guess = input.charAt(0);
            GameState game = activeGames.get(userId);

            if (game.guesses.contains(guess)) {
                sendGameEmbed(event, userId, "❌ You already guessed `" + guess + "`!");
                return;
            }

            game.guesses.add(guess);

            if (!game.word.contains(String.valueOf(guess))) {
                game.lives--;
            }

            // 3. Win/Loss Logic
            if (isWin(game)) {
                Economy.addTealeafs(userId, game.reward);
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("🎉 VICTORY!")
                        .setColor(Color.GREEN)
                        .setDescription("The word was: **" + game.word + "**\n\nYou earned **" + game.reward + " 🍃 Tealeafs**!")
                        .setThumbnail(event.getAuthor().getEffectiveAvatarUrl());
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                activeGames.remove(userId);
            } else if (game.lives <= 0) {
                EmbedBuilder eb = new EmbedBuilder()
                        .setTitle("💀 GAME OVER")
                        .setColor(Color.RED)
                        .setDescription("The word was: **" + game.word + "**\nBetter luck next time!")
                        .addField("Final Visual", "```\n" + getGallows(0) + "\n```", false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                activeGames.remove(userId);
            } else {
                sendGameEmbed(event, userId, game.word.contains(String.valueOf(guess)) ? "✅ Correct!" : "❌ Wrong choice!");
            }
        }
    }

    private void sendGameEmbed(MessageReceivedEvent event, long userId, String status) {
        GameState game = activeGames.get(userId);
        EmbedBuilder eb = new EmbedBuilder();
        
        eb.setTitle("🪵 Hangman Session");
        eb.setColor(getHealthColor(game.lives));
        eb.setThumbnail("https://i.imgur.com/vH97NlO.png"); // Use a cool hanging rope icon
        
        eb.setDescription(status + "\n\n" +
                "```\n" + getGallows(game.lives) + "\n```");

        // Format the word display: A _ _ L E
        StringBuilder wordDisplay = new StringBuilder();
        for (char c : game.word.toCharArray()) {
            wordDisplay.append(game.guesses.contains(c) ? "**" + c + "** " : "＿ ");
        }
        
        eb.addField("Word to Guess", wordDisplay.toString(), false);
        eb.addField("Guesses", game.guesses.isEmpty() ? "None yet" : "`" + game.guesses.toString() + "`", true);
        eb.addField("Remaining Lives", "❤️ " + game.lives + " / 6", true);
        
        eb.setFooter("Type ,hangman <letter> to play!");

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private Color getHealthColor(int lives) {
        if (lives > 4) return Color.GREEN;
        if (lives > 2) return Color.ORANGE;
        return Color.RED;
    }

    private boolean isWin(GameState game) {
        for (char c : game.word.toCharArray()) {
            if (!game.guesses.contains(c)) return false;
        }
        return true;
    }

    private String getGallows(int lives) {
        String[] stages = {
            "  +---+\n  |   |\n  O   |\n /|\\  |\n / \\  |\n      |\n=========", // 0 lives
            "  +---+\n  |   |\n  O   |\n /|\\  |\n /    |\n      |\n=========", // 1 life
            "  +---+\n  |   |\n  O   |\n /|\\  |\n      |\n      |\n=========", // 2 lives
            "  +---+\n  |   |\n  O   |\n /|   |\n      |\n      |\n=========", // 3 lives
            "  +---+\n  |   |\n  O   |\n  |   |\n      |\n      |\n=========", // 4 lives
            "  +---+\n  |   |\n  O   |\n      |\n      |\n      |\n=========", // 5 lives
            "  +---+\n  |   |\n      |\n      |\n      |\n      |\n========="  // 6 lives
        };
        return stages[Math.max(0, Math.min(lives, 6))];
    }

	public Object onCommand(MessageReceivedEvent event, String[] split) {
		return null;
	}
}