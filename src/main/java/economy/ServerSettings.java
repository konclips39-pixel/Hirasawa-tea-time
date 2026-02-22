package economy;

import java.util.HashMap;
import java.util.Map;

public class ServerSettings {
    // This saves prefixes in memory while the bot is running
    private static Map<Long, String> customPrefixes = new HashMap<>();

    public static String getPrefix(long guildId) {
        // If we don't have a prefix for this server yet, return '?'
        return customPrefixes.getOrDefault(guildId, "?"); 
    }

    public static void setPrefix(long guildId, String prefix) {
        customPrefixes.put(guildId, prefix);
    }
}