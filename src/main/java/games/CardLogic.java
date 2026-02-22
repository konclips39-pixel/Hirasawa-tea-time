package games;

import java.util.Random;

public class CardLogic {
    // A simple list of cards to draw from
    private static final String[] CARDS = {"A❤️", "K♠️", "Q♦️", "J♣️", "10❤️", "9♠️", "8♦️", "7♣️", "6❤️", "5♠️"};

    public static String getRandomCard() {
        return CARDS[new Random().nextInt(CARDS.length)];
    }

    public static int getCardValue(String card) {
        // Remove emojis/suits so "10" becomes "10" or "K" becomes "K"
        String value = card.replaceAll("[^0-9JQKA]", ""); 
        
        if (value.equals("J") || value.equals("Q") || value.equals("K")) return 10;
        if (value.equals("A")) return 11;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0; // This is what is happening to you right now
        }
    
	}
}