package economy;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.yuihara.Item;
import com.yuihara.Items;

public class Inventory {
    private static HashMap<Long, String> equippedWeapon = new HashMap<>();
    private static HashMap<Long, String> equippedArmor = new HashMap<>();

    // --- DATABASE CONNECTION CONFIG ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mysql?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    // --- SAVE FUNCTION: Adds item to the SQL table ---
    public static void addItem(long userId, String itemId) {
        String sql = "INSERT INTO user_inventory (user_id, item_id) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            pstmt.setString(2, itemId);
            pstmt.executeUpdate();
            System.out.println("Saved " + itemId + " for user " + userId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- LOAD FUNCTION: Gets items for the Dashboard ---
    public static List<Item> getUserOwnedItems(long userId) {
        List<Item> ownedItems = new ArrayList<>();
        String sql = "SELECT item_id FROM user_inventory WHERE user_id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Item item = Items.getItem(rs.getString("item_id"));
                if (item != null) ownedItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ownedItems;
    }

    // --- WEAPON LOGIC ---
    public static void setWeapon(long userId, String weaponId) {
        equippedWeapon.put(userId, weaponId);
    }

    public static String getEquippedWeapon(long userId) {
        return equippedWeapon.getOrDefault(userId, "none");
    }

    public static int getWeaponDamage(long userId) {
        String weapon = getEquippedWeapon(userId);
        switch (weapon) {
            case "iron_sword": return 15;
            case "katana": return 30;
            case "shadow_dagger": return 55;
            case "mythic_blade": return 75;
            case "dragon_slayer": return 150;
            case "void_reaver": return 300;
            case "steel_longsword": return 25;
            default: return 0;
        }
    }

    // --- ARMOR LOGIC ---
    public static void setArmor(long userId, String armorId) {
        equippedArmor.put(userId, armorId);
    }

    public static String getEquippedArmor(long userId) {
        return equippedArmor.getOrDefault(userId, "none");
    }

    public static int getArmorDefense(long userId) {
        String armor = getEquippedArmor(userId);
        switch (armor) {
            case "leather_tunic": return 5;
            case "knight_plate": return 20;
            default: return 0;
        }
    }

    public static boolean hasItem(long userId, String itemId) {
        List<Item> owned = getUserOwnedItems(userId);
        for (Item item : owned) {
            if (item.getId().equals(itemId)) return true;
        }
        return false;
    }

    public static void equipArmor(long userId, String itemId) {
        setArmor(userId, itemId);
        System.out.println("User " + userId + " equipped armor: " + itemId);
    }

	public static void equipItem(long userId, String itemName) {		
	}
}