package com.yuihara;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import economy.Economy;
import listeners.listener;

@Configuration
public class BotConfig {

    @Autowired
    private EconomyRepository economyRepository;
    @Bean
    public JDA jda(listener listenerBean) throws InterruptedException {
        // THIS IS THE NUCLEAR FIX: Force TLS 1.2 before the DB starts
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        // Initialize the economy database
        Economy.init(economyRepository);
        
        // ... rest of your code
        
        // 1. PUT YOUR NEW TOKEN HERE
        String token = "";

        JDA jda = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
            .addEventListeners(listenerBean)
            .build();

        jda.awaitReady();
     // Replace "123456789012345678" with the Server ID you just copied
        String myServerId = "1463811474513199250"; 

        var guild = jda.getGuildById(myServerId);

        if (guild != null) {
            guild.updateCommands().addCommands(
                Commands.slash("daily", "Claim your daily tea"),
                Commands.slash("help", "Get bot instructions"),
                Commands.slash("shop", "Open the tea bazaar"),
                Commands.slash("balance", "Check your current coins"),
                Commands.slash("inventory", "View your items")
            ).queue(
                success -> System.out.println("✅ Successfully synced commands to server: " + myServerId),
                error -> System.err.println("❌ Failed to sync guild commands: " + error.getMessage())
            );
        } else {
            System.err.println("❌ Could not find server with ID: " + myServerId + ". Make sure the bot is invited to that server!");
        }

        // 2. REGISTER COMMANDS
        // Note: If you already have these in listener.java, you can delete this block
        jda.updateCommands().addCommands(
            Commands.slash("daily", "Claim your daily tea"),
            Commands.slash("help", "Get bot instructions"),
            Commands.slash("shop", "Open the tea bazaar"),
            Commands.slash("balance", "Check your current coins"),
            Commands.slash("inventory", "View your items")
        ).queue();

        return jda; 
    }
}