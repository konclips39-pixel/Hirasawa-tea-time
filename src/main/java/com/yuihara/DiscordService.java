package com.yuihara;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import net.dv8tion.jda.api.JDA;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.core.ParameterizedTypeReference;

@Service
public class DiscordService {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Autowired
    private JDA jda; 

    public DiscordService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.webClient = WebClient.builder()
                .baseUrl("https://discord.com/api")
                .build();
    }

    /**
     * Fetches servers and filters for those where the bot is a member.
     */
    public List<Map<String, Object>> getUserServers(OAuth2User principal) {
        try {
            // 1. Get the OAuth2 client for the logged-in user
            // Ensure the registrationId "discord" matches your application.properties/yml
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("discord", principal.getName());
            
            if (client == null || client.getAccessToken() == null) {
                return new ArrayList<>();
            }

            String accessToken = client.getAccessToken().getTokenValue();
            
            // 2. Fetch the list of guilds using a more robust TypeReference to avoid cast warnings
            List<Map<String, Object>> allServers = webClient.get()
                    .uri("/users/@me/guilds")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block(); // block() is fine in standard WebMvc

            if (allServers == null) return new ArrayList<>();

            // 3. FILTER: Only return servers where 'jda' (the bot) is also present
            return allServers.stream()
                .filter(server -> {
                    Object guildIdObj = server.get("id");
                    if (guildIdObj == null) return false;
                    
                    String guildId = guildIdObj.toString();
                    // JDA must be fully initialized for this to work
                    return jda.getGuildById(guildId) != null;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            // Log the actual stack trace so you can see why it's failing in the console
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}