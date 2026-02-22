package com.yuihara.model;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class EconomyUser {

    @Id
    private String id;
    private int tealeafs; 
    private int bank;
    private int level;
    private int xp;
    private int health;
    private String bio;
    private String equippedWeapon;
    private String equippedArmor;

    // 1. Default Constructor
    public EconomyUser() {}

    // 2. Constructor for new users
    public EconomyUser(String id) {
        this.id = id;
        this.tealeafs = 0; 
        this.bank = 0;
        this.level = 1;
        this.xp = 0;
        this.health = 100;
        this.bio = "No bio set yet!";
        this.equippedWeapon = "fists";
        this.equippedArmor = "cloth";
    }

    // 3. ALL Getters and Setters (If any are missing, the project will stay red)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getTealeafs() { return tealeafs; }
    public void setTealeafs(int tealeafs) { this.tealeafs = tealeafs; }

    public int getBank() { return bank; }
    public void setBank(int bank) { this.bank = bank; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getEquippedWeapon() { return equippedWeapon; }
    public void setEquippedWeapon(String equippedWeapon) { this.equippedWeapon = equippedWeapon; }

    public String getEquippedArmor() { return equippedArmor; }
    public void setEquippedArmor(String equippedArmor) { this.equippedArmor = equippedArmor; }

	public void setLastWork(long timeMillis) {
		
	}

	public long getLastWork() {
		return 0;
	}

	public void setJob(String jobId) {
		
	}

	public String getJob() {
		return null;
	}

	public ArrayList<?> getInventory() {
		return null;
	}

	public void setInventory(ArrayList<?> arrayList) {
		
	}
}