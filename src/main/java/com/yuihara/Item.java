package com.yuihara;

import com.yuihara.Items.Rarity;

public class Item {
    private String id;
    private String name;
    private int power;
    private int defense;
    private int price;
    private boolean isWeapon;
	// In Item.java, make sure the order is exactly like this:
    public Item(String id, String name, String description, int power, int defense, int price, boolean isWeapon, Rarity common) {
        this.id = id;
        this.name = name;
        this.power = power;
        this.defense = defense;
        this.price = price;
        this.isWeapon = isWeapon;
    }
    // Standard Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getPower() { return power; }
    public int getPrice() { return price; }
    public boolean isWeapon() { return isWeapon; }
    public int getAttack() {
        return this.power; // This ensures '25' is returned for the Longsword
    }

    public int getDefense() {
        return this.defense; // This ensures '5' is returned for the Tunic
    }
	public String getDamage() {
		return null;
	}
	public String getRarity() {
		// TODO Auto-generated method stub
		return null;
	}
}