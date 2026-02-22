package systems; // This MUST match the folder name

public class GearSystem {
    
    public static String getWeaponName(int tier) {
        if (tier == 1) return "Rusty Dagger";
        if (tier == 2) return "Steel Longsword";
        if (tier == 3) return "Infinity Blade";
        return "Fists";
    }

    public static String getRarityColor(String rarity) {
        switch(rarity.toLowerCase()) {
            case "common": return "#95A5A6"; // Gray
            case "rare": return "#3498DB";   // Blue
            case "legendary": return "#F1C40F"; // Gold
            default: return "#FFFFFF";
        }
    }
}