package economy;

import com.yuihara.EconomyRepository;
import com.yuihara.model.EconomyUser;
import commands.PlayerAccount;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@Component
public class Economy {

    private static EconomyRepository economyRepo; 

    @Autowired
    private EconomyRepository repoInstance;

    @PostConstruct
    public void init() {
        Economy.economyRepo = repoInstance;
        System.out.println("✅ Economy Repository initialized for static access.");
    }

    private static EconomyUser getUser(long userId) {
        if (economyRepo == null) {
            throw new IllegalStateException("Economy Repository is not initialized!");
        }
        return economyRepo.findById(String.valueOf(userId))
                .orElseGet(() -> {
                    EconomyUser newUser = new EconomyUser(String.valueOf(userId));
                    newUser.setTealeafs(100);
                    newUser.setBank(0);
                    newUser.setHealth(100);
                    newUser.setLevel(1);
                    newUser.setXp(0);
                    newUser.setInventory(new ArrayList<>());
                    return economyRepo.save(newUser);
                });
    }

    // --- Currency Logic ---
    public static int getTealeafs(long userId) { return getUser(userId).getTealeafs(); }

    public static void addTealeafs(long userId, int amount) {
        EconomyUser user = getUser(userId);
        user.setTealeafs(Math.max(0, user.getTealeafs() + amount));
        economyRepo.save(user);
    }

    public static int getBalance(long userId) { return getTealeafs(userId); }
    public static void addBalance(long userId, int amount) { addTealeafs(userId, amount); }
    public static String getBalanceString(long userId) {
        return "**" + getTealeafs(userId) + " 🍃 Tealeafs**";
    }

    // --- Bank Logic ---
    public static int getBankBalance(long userId) { return getUser(userId).getBank(); }

    public static boolean deposit(long userId, int amount) {
        EconomyUser user = getUser(userId);
        if (user.getTealeafs() < amount || amount <= 0) return false;
        user.setTealeafs(user.getTealeafs() - amount);
        user.setBank(user.getBank() + amount);
        economyRepo.save(user);
        return true;
    }

    public static boolean withdraw(long userId, int amount) {
        EconomyUser user = getUser(userId);
        if (user.getBank() < amount || amount <= 0) return false;
        user.setBank(user.getBank() - amount);
        user.setTealeafs(user.getTealeafs() + amount);
        economyRepo.save(user);
        return true;
    }

    // --- RPG Stats ---
    public static int getHealth(long userId) { return getUser(userId).getHealth(); }
    public static void addHealth(long userId, int amount) {
        EconomyUser user = getUser(userId);
        user.setHealth(Math.min(100, Math.max(0, user.getHealth() + amount)));
        economyRepo.save(user);
    }

    public static int getXP(long userId) { return getUser(userId).getXp(); }
    public static int getLevel(long userId) { return getUser(userId).getLevel(); }

    public static void addXP(long userId, int xpToAdd) {
        EconomyUser user = getUser(userId);
        user.setXp(user.getXp() + xpToAdd);
        int xpNeeded = user.getLevel() * 100; 
        if (user.getXp() >= xpNeeded) {
            user.setXp(user.getXp() - xpNeeded);
            user.setLevel(user.getLevel() + 1);
        }
        economyRepo.save(user);
    }

    // --- Inventory & Equipment ---
    public static ArrayList<?> getUserInventory(long userId) {
        return getUser(userId).getInventory();
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<String> getUserInventory1(long userId) {
        return (ArrayList<String>) getUser(userId).getInventory();
    }

    public static void addToInventory(long userId, String itemId) {
        EconomyUser user = getUser(userId);
        @SuppressWarnings("unchecked")
        ArrayList<String> inventory = (ArrayList<String>) user.getInventory();
        if (inventory == null) {
            inventory = new ArrayList<>();
            user.setInventory(inventory);
        }
        inventory.add(itemId);
        economyRepo.save(user);
    }

    public static boolean removeFromInventory(long userId, String itemId) {
        EconomyUser user = getUser(userId);
        boolean removed = user.getInventory().remove(itemId);
        if (removed) economyRepo.save(user);
        return removed;
    }

    public static String getEquippedWeapon(long userId) { return getUser(userId).getEquippedWeapon(); }
    public static String getEquippedArmor(long userId) { return getUser(userId).getEquippedArmor(); }

    // --- Work & Jobs ---
    public static String getUserJob(long userId) { return getUser(userId).getJob(); }
    public static void setUserJob(long userId, String jobId) {
        EconomyUser user = getUser(userId);
        user.setJob(jobId);
        economyRepo.save(user);
    }

    public static long getLastWorkTime(long userId) { return getUser(userId).getLastWork(); }
    public static void setLastWorkTime(long userId, long timeMillis) {
        EconomyUser user = getUser(userId);
        user.setLastWork(timeMillis);
        economyRepo.save(user);
    }

    // --- Leaderboard ---
    public static String getTopBalances() {
        List<EconomyUser> top = economyRepo.findAll(Sort.by(Sort.Direction.DESC, "tealeafs")).stream()
                .limit(10).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder("🏆 **Richest Tea Masters** 🏆\n");
        for (int i = 0; i < top.size(); i++) {
            sb.append(String.format("%d. <@%s> - %d 🍃\n", i+1, top.get(i).getId(), top.get(i).getTealeafs()));
        }
        return sb.toString();
    }

    public static List<PlayerAccount> getAllPlayers() {
        return economyRepo.findAll().stream()
                .map(u -> new PlayerAccount(Long.parseLong(u.getId()), u.getTealeafs()))
                .collect(Collectors.toList());
    }

    public static boolean withdraw(long userId, Object amount) {
        if (amount instanceof Integer) return withdraw(userId, (int) amount);
        return false;
    }

    public static boolean equipItem(long userId, String itemId) {
        return false; // Placeholder
    }

	public static void init(EconomyRepository economyRepository) {
		
	}
}