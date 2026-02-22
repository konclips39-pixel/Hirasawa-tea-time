package com.yuihara;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.ArrayList;

import com.yuihara.model.EconomyUser;

import yuihara.yuihara.model.ShopItem;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired private EconomyRepository economyRepo;
    @Autowired private ShopRepository shopRepo;

    // Helper to prevent repeating code for user stats
    private void setupUserStats(Model model, OAuth2User principal) {
        String discordId = principal.getAttribute("id") != null ? principal.getAttribute("id").toString() : "0";
        String username = principal.getAttribute("username") != null ? principal.getAttribute("username").toString() : "Unknown User";
        String avatar = principal.getAttribute("avatar") != null ? principal.getAttribute("id").toString() + "/" + principal.getAttribute("avatar").toString() : "";
        
        // Fetch user or create a temporary one if they haven't started yet
        EconomyUser user = economyRepo.findById(discordId).orElse(new EconomyUser(discordId));
        
        model.addAttribute("name", username);
        model.addAttribute("avatarUrl", "https://cdn.discordapp.com/avatars/" + avatar + ".png");
        
        // Updated to use your new "Tealeafs" system
        model.addAttribute("tealeafs", user.getTealeafs());
        model.addAttribute("level", user.getLevel());
        model.addAttribute("xp", user.getXp());
        model.addAttribute("health", user.getHealth());
    }

    @GetMapping("/main")
    public String mainDashboard(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        model.addAttribute("view", "main");
        return "index";
    }

    @GetMapping("/shop")
    public String shopPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        try {
            model.addAttribute("shopItems", shopRepo.findAll());
        } catch (Exception e) {
            model.addAttribute("shopItems", new ArrayList<ShopItem>());
        }
        model.addAttribute("view", "shop");
        return "index";
    }

    @GetMapping("/leaderboard")
    public String leaderboardPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        try {
            // Updated to find all and sort by Tealeafs for the web view
            model.addAttribute("topUsers", economyRepo.findAll());
        } catch (Exception e) {
            model.addAttribute("topUsers", new ArrayList<EconomyUser>());
        }
        model.addAttribute("view", "leaderboard");
        return "index";
    }

    @GetMapping("/inventory")
    public String inventoryPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        
        EconomyUser user = economyRepo.findById(principal.getAttribute("id").toString()).orElse(null);
        model.addAttribute("inventory", user != null ? user.getInventory() : new ArrayList<String>());
        
        model.addAttribute("view", "inventory");
        return "index";
    }

    @GetMapping("/dungeons")
    public String dungeonsPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        model.addAttribute("view", "dungeons");
        return "index";
    }

    @GetMapping("/economy")
    public String economyPage(Model model, @AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return "redirect:/";
        setupUserStats(model, principal);
        model.addAttribute("view", "economy");
        return "index";
    }
}