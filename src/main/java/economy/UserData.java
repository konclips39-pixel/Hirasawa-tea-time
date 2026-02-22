package economy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class UserData {
    
    @Id
    private String id; // Discord user ID
    
    private int balance;
    private int bankBalance;
    private int level;
    private int xp;
    private int health;
    private String equippedWeapon;
    private String equippedArmor;
    
    // Constructors
    public UserData() {
        this.balance = 0;
        this.bankBalance = 0;
        this.level = 1;
        this.xp = 0;
        this.health = 100;
        this.equippedWeapon = "none";
        this.equippedArmor = "none";
    }
    
    public UserData(String id) {
        this.id = id;
        this.balance = 0;
        this.bankBalance = 0;
        this.level = 1;
        this.xp = 0;
        this.health = 100;
        this.equippedWeapon = "none";
        this.equippedArmor = "none";
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public int getBalance() {
        return balance;
    }
    
    public void setBalance(int balance) {
        this.balance = balance;
    }
    
    public int getBankBalance() {
        return bankBalance;
    }
    
    public void setBankBalance(int bankBalance) {
        this.bankBalance = bankBalance;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public int getXp() {
        return xp;
    }
    
    public void setXp(int xp) {
        this.xp = xp;
    }
    
    public int getHealth() {
        return health;
    }
    
    public void setHealth(int health) {
        this.health = health;
    }
    
    public String getEquippedWeapon() {
        return equippedWeapon;
    }
    
    public void setEquippedWeapon(String equippedWeapon) {
        this.equippedWeapon = equippedWeapon;
    }
    
    public String getEquippedArmor() {
        return equippedArmor;
    }
    
    public void setEquippedArmor(String equippedArmor) {
        this.equippedArmor = equippedArmor;
    }
}