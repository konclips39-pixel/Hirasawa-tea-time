package commands;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

/**
 * This class now represents a "User Profile" in your MongoDB Database.
 * Every field here will be a column/key in your database.
 */
@Document(collection = "users") // This tells Spring to save this in the 'users' collection
public class PlayerAccount {

    @Id
    private long userId;

    // --- Economy & Stats ---
    private int balance = 1000;
    private int level = 1;
    private int xp = 0;
    private int health = 100;
    private String currentJob = "none";

    // --- Cooldown Timestamps ---
    private long lastDailyClaim = 0;
    private long lastWorkTime = 0;
    @SuppressWarnings("unused")
	private long lastFishTime = 0;

    // --- Equipment & Inventory ---
    private String equippedWeapon = "fists";
    private String equippedArmor = "cloth";
    private List<String> inventory = new ArrayList<>();

    // Constructor
    public PlayerAccount(long userId, int i) {
        this.userId = userId;
    }

    public PlayerAccount(Long id) {
		// TODO Auto-generated constructor stub
	}

	// --- Core Balance Methods ---
    public int getBalance() { return balance; }
    public void setBalance(int amount) { this.balance = amount; }
    
    public void addBalance(int amount) {
        this.balance += amount;
    }

    public boolean subtractBalance(int amount) {
        if (this.balance < amount) return false;
        this.balance -= amount;
        return true;
    }

    // --- Leveling System ---
    public void addXP(int amount) {
        this.xp += amount;
        // Logic: Every 100 XP is a level
        if (this.xp >= (this.level * 100)) {
            this.xp -= (this.level * 100);
            this.level++;
        }
    }

    // --- Combat & Health ---
    public int getHealth() { return health; }
    public void setHealth(int health) { 
        this.health = Math.min(100, Math.max(0, health)); 
    }

    // --- Inventory & Gear ---
    public List<String> getInventory() { return inventory; }
    
    public void addItem(String itemId) {
        this.inventory.add(itemId);
    }

    public boolean removeItem(String itemId) {
        return this.inventory.remove(itemId);
    }

    // --- Getters & Setters (The "Engine" of the data) ---
    public long getUserId() { return userId; }
    
    public long getLastDailyClaim() { return lastDailyClaim; }
    public void setLastDailyClaim(long time) { this.lastDailyClaim = time; }

    public long getLastWorkTime() { return lastWorkTime; }
    public void setLastWorkTime(long time) { this.lastWorkTime = time; }

    public String getEquippedWeapon() { return equippedWeapon; }
    public void setEquippedWeapon(String weapon) { this.equippedWeapon = weapon; }

    public String getEquippedArmor() { return equippedArmor; }
    public void setEquippedArmor(String armor) { this.equippedArmor = armor; }

    public String getCurrentJob() { return currentJob; }
    public void setCurrentJob(String job) { this.currentJob = job; }

    public int getLevel() { return level; }
    public int getXp() { return xp; }
}