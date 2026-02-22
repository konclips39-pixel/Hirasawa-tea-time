package com.yuihara;

import java.util.HashMap;
import java.util.Map;

public class PrefixManager {
    // Stores the prefix for each server (Guild ID -> Prefix)
    private static final Map<String, String> guildPrefixes = new HashMap<>();

    public static String getPrefix(String guildId) {
        return guildPrefixes.getOrDefault(guildId, "?");
    }

    public static void setPrefix(String guildId, String prefix) {
        guildPrefixes.put(guildId, prefix);
        System.out.println("Prefix updated to '" + prefix + "' for guild: " + guildId);
    }
}