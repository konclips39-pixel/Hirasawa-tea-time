package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import games.CardLogic; 
import economy.Economy;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

@Component
public class Blackjack extends ListenerAdapter {
    
    public static final Map<Long, GameState> activeGames = new HashMap<>();

    public static class GameState {
        public String playerHand;
        public String dealerHand;
        public int playerTotal;
        public int dealerTotal;

        GameState(String pHand, String dHand, int pTotal, int dTotal) {
            this.playerHand = pHand;
            this.dealerHand = dHand;
            this.playerTotal = pTotal;
            this.dealerTotal = dTotal;
        }
    }

    public void handleCommand(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        
        String p1 = CardLogic.getRandomCard();
        String p2 = CardLogic.getRandomCard();
        String d1 = CardLogic.getRandomCard();
        
        int pTotal = CardLogic.getCardValue(p1) + CardLogic.getCardValue(p2);
        int dTotal = CardLogic.getCardValue(d1);

        activeGames.put(userId, new GameState(p1 + " " + p2, d1, pTotal, dTotal));

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🃏 Blackjack Table");
        eb.setColor(new Color(34, 139, 34)); // Forest Green table
        eb.addField("Your Hand", p1 + " " + p2 + " (Total: " + pTotal + ")", true);
        eb.addField("Dealer's Hand", d1 + " ❓", true);
        eb.setFooter("Game for " + event.getAuthor().getName());

        event.getChannel().sendMessageEmbeds(eb.build())
            .setActionRow(
                Button.primary("hit", "Hit 🃏"), 
                Button.danger("stand", "Stand ✋")
            ).queue();
    }

    public void handleHit(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        GameState game = activeGames.get(userId);

        if (game == null) {
            event.reply("No active game found!").setEphemeral(true).queue();
            return;
        }

        String newCard = CardLogic.getRandomCard();
        game.playerHand += " " + newCard;
        game.playerTotal += CardLogic.getCardValue(newCard);

        if (game.playerTotal > 21) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("💥 Bust!");
            eb.setColor(Color.RED);
            eb.setDescription("You hit " + game.playerTotal + " and spilled your tea!\n**Hand:** " + game.playerHand);
            event.editMessageEmbeds(eb.build()).setComponents().queue();
            activeGames.remove(userId);
        } else {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("🃏 Blackjack - Hit");
            eb.setColor(Color.GREEN);
            eb.addField("Your Hand", game.playerHand + " (Total: " + game.playerTotal + ")", true);
            eb.addField("Dealer's Hand", game.dealerHand + " ❓", true);
            event.editMessageEmbeds(eb.build()).queue();
        }
    }

    public void handleStand(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        GameState game = activeGames.get(userId);

        if (game == null) return;

        while (game.dealerTotal < 17) {
            String dCard = CardLogic.getRandomCard();
            game.dealerHand += " " + dCard;
            game.dealerTotal += CardLogic.getCardValue(dCard);
        }

        String result;
        Color color;

        if (game.dealerTotal > 21) {
            int reward = 200;
            Economy.addTealeafs(userId, reward); // UPDATED
            result = "🎉 **Dealer Busts! You Win!**\nReward: **" + reward + " 🍃 Tealeafs**";
            color = Color.GREEN;
        } else if (game.playerTotal > game.dealerTotal) {
            int reward = 200;
            Economy.addTealeafs(userId, reward); // UPDATED
            result = "🎉 **You Win!**\nReward: **" + reward + " 🍃 Tealeafs**";
            color = Color.GREEN;
        } else if (game.playerTotal == game.dealerTotal) {
            result = "🤝 **Push!** It's a tie.";
            color = Color.GRAY;
        } else {
            result = "💀 **Dealer Wins.** You lost your wager.";
            color = Color.RED;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🃏 Blackjack Final Result");
        eb.setColor(color);
        eb.addField("Your Final Hand", game.playerHand + " (Total: " + game.playerTotal + ")", false);
        eb.addField("Dealer's Final Hand", game.dealerHand + " (Total: " + game.dealerTotal + ")", false);
        eb.setDescription(result);
        
        event.editMessageEmbeds(eb.build()).setComponents().queue();
        activeGames.remove(userId);
    }
}