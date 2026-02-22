package commands;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import economy.Economy;
import java.awt.Color;
import java.util.*;

public class TriviaManager {
    private static String currentAnswer = null;
    private static boolean isEventActive = false;

    // Use a Map for Question -> Answer
    private static final Map<String, String> wikiTrivia = new HashMap<>();

    static {
        wikiTrivia.put("What is the name of the 'Heritage Cherry Sunburst' Gibson Les Paul Yui plays?", "giita");
        wikiTrivia.put("Which character has a pet turtle named Ton-chan?", "azu-nyan");
        wikiTrivia.put("What is the name of the restaurant where the girls often eat cake and drink tea?", "kotobuki");
        wikiTrivia.put("What is the name of the school's music teacher who was once in a death metal band?", "sawako");
        wikiTrivia.put("What is the title of the song Yui wrote for her sister UI?", "u&i");
        wikiTrivia.put("Who is the drummer and self-proclaimed president of the Light Music Club?", "ritsu");
    }

    public static void startRandomEvent(TextChannel channel) {
        if (isEventActive) return; // Don't start a new one if one is already running

        List<String> questions = new ArrayList<>(wikiTrivia.keySet());
        String question = questions.get(new Random().nextInt(questions.size()));
        currentAnswer = wikiTrivia.get(question);
        isEventActive = true;

        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🎸 AFTER SCHOOL TRIVIA! 🍵")
            .setDescription("**Question:** " + question + "\n\n*Be the first to type the correct answer to win 250 🍃 Tealeafs!*")
            .setColor(Color.PINK)
            .setFooter("Trivia Event Triggered!");

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public static void checkAnswer(MessageReceivedEvent event) {
        if (!isEventActive || currentAnswer == null) return;

        String userMsg = event.getMessage().getContentRaw().toLowerCase().trim();

        if (userMsg.equalsIgnoreCase(currentAnswer)) {
            isEventActive = false;
            currentAnswer = null;
            long userId = event.getAuthor().getIdLong();

            // Reward the user in the Database
            Economy.addTealeafs(userId, 250);

            event.getChannel().sendMessage("✅ **CORRECT!** " + event.getAuthor().getAsMention() + 
                " answered correctly and earned **250 🍃 Tealeafs**!").queue();
        }
    }
}