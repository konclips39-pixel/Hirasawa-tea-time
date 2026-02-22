package commands;

import economy.Economy;

public class CurrencyManager {

    /**
     * Safety method to add Tealeafs. 
     * Handles the String-to-Long conversion so you don't have to do it in every command.
     */
    public static void addTealeafs(String userId, int amount) {
        try {
            long id = Long.parseLong(userId);
            Economy.addTealeafs(id, amount);
        } catch (NumberFormatException e) {
            System.err.println("Invalid User ID passed to CurrencyManager: " + userId);
        }
    }

    /**
     * The "Store" logic.
     * Checks if a user has enough leaves before taking them.
     * Returns true if successful, false if they are too broke.
     */
    public static boolean canAffordAndPay(String userId, int cost) {
        long id = Long.parseLong(userId);
        int currentBalance = Economy.getTealeafs(id);

        if (currentBalance >= cost) {
            Economy.addTealeafs(id, -cost); // Subtract the cost
            return true;
        }
        return false;
    }

    /**
     * Formatting helper.
     * Call this whenever you want to show a balance in a message.
     * Example: CurrencyManager.format(500) -> "500 🍃 Tealeafs"
     */
    public static String format(int amount) {
        return "**" + amount + " 🍃 Tealeafs**";
    }

    /**
     * High-level check for daily limits or caps.
     * You can expand this to prevent users from having more than 1 million leaves, etc.
     */
    public static int getSafeBalance(String userId) {
        try {
            return Economy.getTealeafs(Long.parseLong(userId));
        } catch (Exception e) {
            return 0;
        }
    }
}