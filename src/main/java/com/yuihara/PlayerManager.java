package com.yuihara;

import java.util.HashMap;
import java.util.Map;
import commands.PlayerAccount;

public class PlayerManager {
    private static final Map<Long, PlayerAccount> players = new HashMap<>();

    public static PlayerAccount getPlayer(long userId) {
        // If player doesn't exist, create them
        return players.computeIfAbsent(userId, id -> new PlayerAccount(id));
    }

    // This is the "Commit" method that would write to a file
    public static void savePlayers() {
        System.out.println("[SYSTEM] Saving player data to disk...");
        // Here you would add your File/Database writing logic
    }
}