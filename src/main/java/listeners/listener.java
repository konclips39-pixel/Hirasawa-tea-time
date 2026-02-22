package listeners;

import org.springframework.stereotype.Component;
import com.yuihara.Item;
import com.yuihara.PrefixManager;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import commands.*;
import economy.Economy;
import economy.Inventory;
import games.CardLogic;

import java.awt.Color;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class listener extends ListenerAdapter {

    private static final Hangman hangmanHandler = new Hangman();
    private static final Map<Long, String> equippedArmor = new ConcurrentHashMap<>();
    
    private final String[] responses = {
        "It is certain.", "Without a doubt.", "Yes definitely.", 
        "Reply hazy, try again.", "Ask again later.", "Better not tell you now.",
        "Don't count on it.", "My sources say no.", "Outlook not so good.", "Very doubtful."
    };

    // --- Initialization & Slash Command Sync ---
    @SuppressWarnings("deprecation")
	@Override
    public void onReady(ReadyEvent event) {
        System.out.println("✅ TeaTimeBot is awake! Logged in as: " + event.getJDA().getSelfUser().getAsTag());
        
        // Register Slash Commands once on Startup (Proper way)
        event.getJDA().updateCommands().addCommands(
            Commands.slash("rules", "Read the server rules"),
            Commands.slash("balance", "Check your wallet and bank balance"),
            Commands.slash("daily", "Claim your daily reward"),
            Commands.slash("fight", "Start a fight"),
            Commands.slash("heal", "Heal yourself"),
            Commands.slash("profile", "View your hero stats"),
            Commands.slash("shop", "View the marketplace"),
            Commands.slash("equip", "Equip an item from your inventory")
                .addOptions(new OptionData(OptionType.STRING, "item_id", "The ID of the item to equip", true)),
            Commands.slash("deposit", "Deposit money into your bank")
                .addOptions(new OptionData(OptionType.INTEGER, "amount", "Amount to deposit", true)),
            Commands.slash("withdraw", "Withdraw money from your bank")
                .addOptions(new OptionData(OptionType.INTEGER, "amount", "Amount to withdraw", true))
        ).queue(s -> System.out.println("✅ Global Slash Commands Synced."));
    }

    // --- Core Message Event Logic ---
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || !event.isFromGuild()) return;

        String msg = event.getMessage().getContentRaw();
        String guildId = event.getGuild().getId();
        
        // Dynamic Prefix Management
        String guildPrefix = PrefixManager.getPrefix(guildId);
        if (guildPrefix == null || guildPrefix.isBlank()) guildPrefix = ",";

        // Support for both default and custom guild prefixes
        if (!msg.startsWith(",") && !msg.startsWith(guildPrefix)) return;

        String activePrefix = msg.startsWith(",") ? "," : guildPrefix;
        String withoutPrefix = msg.substring(activePrefix.length()).trim();
        if (withoutPrefix.isEmpty()) return;

        String[] parts = withoutPrefix.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        long userId = event.getAuthor().getIdLong();

        try {
            switch (command) {
                // SYSTEM
                case "status" -> event.getChannel().sendMessage("✅ Bot logic is active! Database SSL is currently bypassing.").queue();
                case "rules" -> RulesCommand.sendRules(event);
                case "say" -> handleSayCommand(event, args);
                
                // ADMIN
                case "admin-give" -> handleAdminGive(event, args);
                case "admin-take" -> AdminTake.execute(event, args);
                case "money" -> handleMoneyCommand(event, args, userId);

                // ECONOMY
                case "bal", "balance" -> handleBalance(event, userId);
                case "dep", "deposit" -> handleDeposit(event, args, userId);
                case "with", "withdraw" -> handleWithdraw(event, args, userId);
                case "daily" -> DailyCommand.run(event);
                case "lb", "leaderboard" -> handleLeaderboard(event);
                
                // RPG
                case "p", "profile", "stats" -> handleProfile(event, userId);
                case "fight" -> Fighting.startFight(event);
                case "boss" -> BossFight.run(event);
                case "heal" -> handleHeal(event, userId);
                case "job" -> handleJobCommand(event, args);
                
                // INVENTORY
                case "shop" -> Shop.showShop(event);
                case "inv", "inventory" -> Items.showInventory(event);
                case "buy" -> handleBuy(event, args, userId);
                case "equip" -> handleEquip(event, args, userId);

                // GAMES & FUN
                case "slots" -> handleSlots(event, args, userId);
                case "fish" -> Fish.run(event);
                case "work" -> Work.execute(event);
                case "beg" -> new Beg().onCommand(event);
                case "crime" -> Crime.run(event);
                case "dice" -> Dice.run(event);
                case "highlow" -> HighLow.run(event);
                case "hangman" -> hangmanHandler.onCommand(event, args.split("\\s+"));
                case "8ball" -> handle8Ball(event, args);
                
                // HELP
                case "help" -> handleHelpMenu(event);
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ **Critical Database Error:** " + e.getMessage()).queue();
            e.printStackTrace();
        }
    }
    // ============================================
    // COMMAND HANDLERS
    // ============================================
 // ============================================
    // COMPLETED COMMAND HANDLERS
    // ============================================

    private void handleHelpMenu(MessageReceivedEvent event) {
        EmbedBuilder help = new EmbedBuilder()
            .setTitle("🍵 HIRASAWA TEA TIME | Imperial Manual")
            .setDescription(
                "Welcome to the **Imperial Tea Gardens**, " + event.getAuthor().getAsMention() + ".\n" +
                "Below is the complete protocol list for all active systems.\n\n" +
                "**📜 Universal Prefix:** `,` (comma) | **🤖 Version:** `3.2.0-FLASH`"
            )
            .setColor(new Color(101, 67, 33)) 
            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())

            .addField("🍃 Economy & Banking", 
                "◈ `bal` • `dep` • `with` • `give` • `lb` • `daily` • `beg` • `work` • `crime` ", false)

            .addField("⚔️ RPG & Combat", 
                "◈ `stats` • `inv` • `fight` • `bossfight` • `adventure` • `profile` • `gear` ", false)

            .addField("🎰 Games & Entertainment", 
                "◈ `blackjack` • `slots` • `dice` • `highlow` • `hangman` • `8ball` • `fish` ", false)

            .addField("🏪 Marketplace & Equipment", 
                "◈ `shop` • `buy` • `sell` • `equip` • `items` • `upgrade` ", false)
            
            .addField("📋 System & Utility", 
                "◈ `help` • `rules` • `say` • `ping` • `info` • `prefix` ", false)

            .setFooter("Requested by " + event.getAuthor().getName() + " • Click a button for detailed guides")
            .setTimestamp(Instant.now());

        event.getChannel().sendMessageEmbeds(help.build())
            .setActionRow(
                Button.success("help_eco", "🍃 Economy"),
                Button.danger("help_rpg", "⚔️ RPG"),
                Button.primary("help_games", "🎰 Games"),
                Button.secondary("help_shop", "🏪 Shop")
            ).queue();
    }

    private void handle8Ball(MessageReceivedEvent event, String args) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("🔮 **The Oracle whispers:** You must ask a question first!").queue();
            return;
        }
        // Using the responses array defined at the top of your listener class
        String randomResponse = responses[new Random().nextInt(responses.length)];
        event.getChannel().sendMessage("🔮 **8-Ball says:** " + randomResponse).queue();
    }

    private void handleSayCommand(MessageReceivedEvent event, String args) {
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ You don't have permission to use this command.").queue();
            return;
        }

        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: `,say <message> in <#channel>`").queue();
            return;
        }

        int inIndex = args.toLowerCase().lastIndexOf(" in ");
        if (inIndex == -1) {
            event.getChannel().sendMessage("❌ Use 'in' before the channel. Example: `,say Hello in #general`").queue();
            return;
        }

        String sayMessage = args.substring(0, inIndex).trim();
        String channelPart = args.substring(inIndex + 4).trim();

        TextChannel targetChannel = event.getMessage().getMentions().getChannels().stream()
            .filter(channel -> channel instanceof TextChannel)
            .map(channel -> (TextChannel) channel)
            .findFirst()
            .orElse(event.getGuild().getTextChannels().stream()
                .filter(tc -> tc.getName().equalsIgnoreCase(channelPart))
                .findFirst().orElse(null));

        if (targetChannel == null) {
            event.getChannel().sendMessage("❌ Channel not found: `" + channelPart + "`").queue();
            return;
        }

        event.getMessage().delete().queue(
            success -> targetChannel.sendMessage(sayMessage).queue(),
            error -> event.getChannel().sendMessage("❌ Failed to delete your message (Check permissions).").queue()
        );
    }

    private void handleBalance(MessageReceivedEvent event, long userId) {
        int bal = Economy.getBalance(userId);
        int bank = (int) Economy.getBankBalance(userId);
        event.getChannel().sendMessage("💰 **Wallet:** " + bal + " | 🏦 **Bank:** " + bank).queue();
    }

    private void handleDeposit(MessageReceivedEvent event, String args, long userId) {
        try {
            int amount = args.equalsIgnoreCase("all") ? Economy.getBalance(userId) : Integer.parseInt(args.trim());
            if (Economy.deposit(userId, amount)) {
                event.getChannel().sendMessage("✅ Deposited **" + amount + "** tealeafs!").queue();
            } else {
                event.getChannel().sendMessage("❌ Not enough wallet tealeafs.").queue();
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,deposit <amount|all>`").queue();
        }
    }

    private void handleWithdraw(MessageReceivedEvent event, String args, long userId) {
        try {
            Object amount;
			if (args.equalsIgnoreCase("all"))
				amount = Economy.getBankBalance(userId);
			else
				amount = Integer.parseInt(args.trim());
            if (Economy.withdraw(userId, amount)) {
                event.getChannel().sendMessage("🏧 Withdrew **" + amount + "** tealeafs!").queue();
            } else {
                event.getChannel().sendMessage("❌ Not enough bank balance.").queue();
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,withdraw <amount|all>`").queue();
        }
    }

    private void handleMoneyCommand(MessageReceivedEvent event, String args, long userId) {
        if (args.startsWith("add ")) {
            handleMoneyAdd(event, args, userId);
        } else if (args.startsWith("give ")) {
            handleMoneyGive(event, args, userId);
        } else {
            event.getChannel().sendMessage("❌ Usage: `,money add <amount>` or `,money give @user <amount>`").queue();
        }
    }

    private void handleMoneyAdd(MessageReceivedEvent event, String args, long userId) {
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ Permission Denied.").queue();
            return;
        }
        try {
            int amount = Integer.parseInt(args.split("\\s+")[1]);
            Economy.addBalance(userId, amount);
            event.getChannel().sendMessage("💎 Successfully added **" + amount + "** tealeafs to wallet!").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,money add <amount>`").queue();
        }
    }

    private void handleMoneyGive(MessageReceivedEvent event, String args, long userId) {
        try {
            List<User> mentioned = event.getMessage().getMentions().getUsers();
            if (mentioned.isEmpty()) {
                event.getChannel().sendMessage("❌ Mention a user to give money to.").queue();
                return;
            }
            long targetId = mentioned.get(0).getIdLong();
            int amount = Integer.parseInt(args.split("\\s+")[2]);

            if (Economy.getBalance(userId) >= amount && amount > 0) {
                Economy.addBalance(userId, -amount);
                Economy.addBalance(targetId, amount);
                event.getChannel().sendMessage("💸 Sent **" + amount + "** tealeafs to <@" + targetId + ">!").queue();
            } else {
                event.getChannel().sendMessage("❌ Insufficient funds.").queue();
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,money give @user <amount>`").queue();
        }
    }

    private void handleAdminGive(MessageReceivedEvent event, String args) {
        if (event.getAuthor().getIdLong() != 935181639561150464L) {
            event.getChannel().sendMessage("⛔ Access Denied.").queue();
            return;
        }
        try {
            List<User> mentioned = event.getMessage().getMentions().getUsers();
            if (mentioned.isEmpty()) return;
            long targetId = mentioned.get(0).getIdLong();
            int amount = Integer.parseInt(args.split("\\s+")[1]);
            Economy.addBalance(targetId, amount);
            event.getChannel().sendMessage("💎 Admin Spawned **" + amount + "** tealeafs for <@" + targetId + ">!").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,admin-give @user <amount>`").queue();
        }
    }

    private void handleProfile(MessageReceivedEvent event, long userId) {
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("👤 " + event.getAuthor().getName() + "'s Hero Profile")
            .setColor(Color.decode("#00fbff"))
            .addField("📊 Stats", "**Level:** " + Economy.getLevel(userId) + "\n**XP:** " + Economy.getXP(userId) + "/100", true)
            .addField("💰 Currency", "**Wallet:** " + Economy.getBalance(userId) + "\n**Bank:** " + Economy.getBankBalance(userId), true)
            .setFooter("Keep fighting!");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleLeaderboard(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("🏆 Global Wealth Leaderboard")
            .setColor(Color.RED)
            .setDescription(Economy.getTopBalances());
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void handleHeal(MessageReceivedEvent event, long userId) {
        if (Economy.getHealth(userId) >= 100) {
            event.getChannel().sendMessage("❌ You are at full health!").queue();
            return;
        }
        Economy.addHealth(userId, 50);
        event.getChannel().sendMessage("🧪 Healed **50 HP**! Current HP: " + Economy.getHealth(userId)).queue();
    }

    private void handleJobCommand(MessageReceivedEvent event, String args) {
        if (args.equalsIgnoreCase("list")) {
            Jobs.listJobs(event);
        } else if (args.toLowerCase().startsWith("apply ")) {
            Jobs.apply(event, args.substring(6).trim());
        } else {
            event.getChannel().sendMessage("❌ Usage: `,job list` or `,job apply <name>`").queue();
        }
    }

    private void handleBuy(MessageReceivedEvent event, String args, long userId) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ Usage: `,buy <item_id>`").queue();
            return;
        }

        String itemId = args.toLowerCase().trim().replace(" Berlin ", "_");
        Map<String, Integer> shopItems = new HashMap<>();
        shopItems.put("iron_sword", 2500);
        shopItems.put("katana", 7500);
        shopItems.put("knight_plate", 5000);
        shopItems.put("health_pot", 500);

        if (!shopItems.containsKey(itemId)) {
            event.getChannel().sendMessage("❌ Item not found in shop.").queue();
            return;
        }

        int price = shopItems.get(itemId);
        if (Economy.getBalance(userId) >= price) {
            Economy.addBalance(userId, -price);
            if (itemId.equals("health_pot")) {
                Economy.addHealth(userId, 50);
            } else if (itemId.contains("sword") || itemId.contains("katana")) {
                Inventory.setWeapon(userId, itemId);
            } else if (itemId.contains("plate")) {
                Inventory.setArmor(userId, itemId);
            }
            event.getChannel().sendMessage("🛒 Purchased **" + itemId.replace("_", " ") + "**!").queue();
        } else {
            event.getChannel().sendMessage("❌ Not enough tealeafs!").queue();
        }
    }

    private void handleEquip(MessageReceivedEvent event, String args, long userId) {
        if (args.isEmpty()) {
            event.getChannel().sendMessage("❌ Please specify an item to equip.\nExample: `,equip steel longsword`").queue();
            return;
        }

        String itemId = args.trim().toLowerCase().replaceAll("\\s+", "_");

        if (itemId.length() < 3) {
            event.getChannel().sendMessage("❌ Invalid item name.").queue();
            return;
        }

        boolean success = Economy.equipItem(userId, itemId);

        if (success) {
            event.getChannel().sendMessage("✅ Equipped **" + itemId.replace("_", " ") + "** successfully!").queue();
        } else {
            event.getChannel().sendMessage(
                "❌ Could not equip **" + itemId.replace("_", " ") + "**.\n" +
                "• Make sure you own it (check with `,inv`)\n" +
                "• Check spelling or use the exact item name"
            ).queue();
        }
    }

    private void handleSlots(MessageReceivedEvent event, String args, long userId) {
        int bet = 50;
        if (!args.isEmpty()) {
            try {
                bet = Integer.parseInt(args.trim());
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("❌ Invalid bet amount!").queue();
                return;
            }
        }
        
        if (Economy.getBalance(userId) < bet) {
            event.getChannel().sendMessage("❌ Not enough tealeafs to bet!").queue();
            return;
        }
        
        Economy.addBalance(userId, -bet);
        String result = "🎰 Slot Machine\n[ 🍒 | 🔔 | 🍋 ]\nYou bet **" + bet + "** tealeafs\nBalance: **" + Economy.getBalance(userId) + "**";
        event.getChannel().sendMessage(result).queue();
    }

    public void onButtonInteraction1(ButtonInteractionEvent event) {
        String buttonId = event.getComponentId();

        if (buttonId.equals("hit") || buttonId.equals("stand")) {
            Blackjack blackjackHandler = new Blackjack();
            if (buttonId.equals("hit")) blackjackHandler.handleHit(event);
            else blackjackHandler.handleStand(event);
            return;
        }

        EmbedBuilder eb = new EmbedBuilder().setColor(new Color(114, 137, 218));

        switch (buttonId) {
            case "help_econ", "help_economy" -> {
                eb.setTitle("💰 Economy & Banking Guide")
                  .setDescription("Manage your tealeafs, deposit safely, and climb the leaderboard!\n💡 **Tip:** Always save tealeafs in your bank to avoid losing them during adventures!")
                  .addField("Wallet & Bank", "`,bal` - Check your wallet & bank\n`,deposit <amt>` - Deposit tealeafs into bank\n`,withdraw <amt>` - Withdraw tealeafs back to wallet", false)
                  .addField("Transfers & Leaderboard", "`,money give @user <amt>` - Send tealeafs to another player\n`,lb` - Check top players globally", false)
                  .addField("Daily Rewards", "`,daily` - Claim your daily bonus tealeafs to boost your wallet", false);
            }
            case "help_combat", "help_dungeons" -> {
                eb.setTitle("⚔️ Combat & RPG Guide")
                  .setDescription("Grow stronger, defeat monsters, and conquer bosses!\n💡 **Tip:** Equip the best weapons and heal between battles.")
                  .addField("Player Stats", "`,profile` - View your level & XP\n`,inv` - Check your current equipment", false)
                  .addField("Battle Commands", "`,fight` - Start a fight\n`,heal` - Recover HP\n`,boss` - Challenge a dungeon boss", false)
                  .addField("Tips", "Use potions wisely and upgrade your weapons to maximize damage!", false);
            }
            case "help_games" -> {
                eb.setTitle("🎰 Games & Fun Guide")
                  .setDescription("Enjoy casino games and fun minigames!\n💡 **Tip:** Practice games to improve your skills and earn rewards!")
                  .addField("Casino Games", "`,blackjack`, `,slots`, `,highlow`", false)
                  .addField("Mini Games & Fun", "`,fish`, `,beg`, `,crime`, `,dice`, `,hangman`", false)
                  .addField("Tips", "Try multiple games daily to earn tealeafs and improve your stats!", false);
            }
            case "help_shop" -> {
                eb.setTitle("🏪 Marketplace Guide")
                  .setDescription("Browse the shop, buy items, and equip them to enhance your adventures!\n💡 **Tip:** Always check your balance before buying and remember better gear boosts combat performance.")
                  .addField("Buying Items", "`,shop` - View items in the marketplace\n`,buy <item>` - Purchase an item\n`,equip <item>` - Equip your new gear", false)
                  .addField("Gear Benefits", "Weapons increase fight damage, potions restore HP, armor reduces damage taken.", false)
                  .addField("Strategy", "Combine different gear types to maximize your combat efficiency!", false);
            }
            default -> {
                if (buttonId.startsWith("hl_")) {
                    java.util.Random r = new java.util.Random();
                    int secret = r.nextInt(100) + 1;
                    event.editMessage("🎲 The secret number was **" + secret + "**!\nGame Over.").setComponents().queue();
                }
                return;
            }
        }

        event.editMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("equip")) {
            String itemId = event.getOption("item_id").getAsString().toLowerCase().replace(" ", "_");
            Item item = Items.getItem(itemId);

            if (item == null) {
                event.reply("❌ That item does not exist in the database!").setEphemeral(true).queue();
                return;
            }
            
            event.reply("✅ Equipped **" + itemId + "**!").setEphemeral(true).queue();
        }
    }
    

    public static void equipArmor(long userId, String itemId) {
        equippedArmor.put(userId, itemId);
        System.out.println("DEBUG: Armor " + itemId + " successfully equipped to user " + userId);
    }
    
    public static String getEquippedArmor(long userId) {
        return equippedArmor.getOrDefault(userId, null);
    }
    
    public static MessageEmbed getRulesEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🍵 HIRASAWA TEA TIME | SERVER RULES");
        // ... add all your fields here ...
        return embed.build();
    }
    
    

    @SuppressWarnings("unused")
	private void handleHelp(MessageReceivedEvent event) {
        EmbedBuilder help = new EmbedBuilder()
            .setTitle("🍵 HIRASAWA TEA TIME | Imperial Manual")
            .setDescription(
                "Welcome to the **TEATIMEBOT**, " + event.getAuthor().getAsMention() + ".\n" +
                "Below is the complete protocol list for all active systems.\n\n" +
                "**📜 Universal Prefix:** `,` (comma) | **🤖 Version:** `3.2.0-FLASH`"
            )
            .setColor(new Color(101, 67, 33)) 
            .setThumbnail(event.getJDA().getSelfUser().getAvatarUrl())

            .addField("🍃 Economy & Banking", 
                "◈ `bal` • `dep` • `with` • `give` • `lb` • `daily` • `beg` • `work` • `crime` ", false)

            .addField("⚔️ RPG & Combat", 
                "◈ `stats` • `inv` • `fight` • `bossfight` • `adventure` • `profile` • `gear` ", false)

            .addField("🎰 Games & Entertainment", 
                "◈ `blackjack` • `slots` • `dice` • `highlow` • `hangman` • `8ball` • `fish` ", false)

            .addField("🏪 Marketplace & Equipment", 
                "◈ `shop` • `buy` • `sell` • `equip` • `items` • `upgrade` ", false)

            .setFooter("Requested by " + event.getAuthor().getName() + " • Click a button for detailed guides")
            .setTimestamp(Instant.now());

        event.getChannel().sendMessageEmbeds(help.build())
            .setActionRow(
                Button.success("help_eco", "🍃 Economy Details"),
                Button.danger("help_rpg", "⚔️ RPG Manual"),
                Button.primary("help_games", "🎰 Game List"),
                Button.secondary("help_shop", "🏪 Shop Info")
            ).queue();
    }

			@Override
			public void onButtonInteraction(ButtonInteractionEvent event) {
			    String buttonId = event.getComponentId();

			    // ---------------------------
			    // 1. Handle Blackjack buttons
			    // ---------------------------
			    if (buttonId.equals("hit") || buttonId.equals("stand")) {
			        Blackjack blackjackHandler = new Blackjack();
			        if (buttonId.equals("hit")) blackjackHandler.handleHit(event);
			        else blackjackHandler.handleStand(event);
			        return;
			    }

			    // ---------------------------
			    // 2. Create embed for help
			    // ---------------------------
			    EmbedBuilder eb = new EmbedBuilder().setColor(new Color(114, 137, 218)); // Discord purple-ish

			    // ---------------------------
			    // 3. Help menu buttons
			    // ---------------------------
			    switch (buttonId) {
			        case "help_econ", "help_economy" -> {
			            eb.setTitle("💰 Economy & Banking Guide")
			              .setDescription("Manage your tealeafs, deposit safely, and climb the leaderboard!\n💡 **Tip:** Always save tealeafs in your bank to avoid losing them during adventures!")
			              .addField("Wallet & Bank", "`,bal` - Check your wallet & bank\n`,deposit <amt>` - Deposit tealeafs into bank\n`,withdraw <amt>` - Withdraw tealeafs back to wallet", false)
			              .addField("Transfers & Leaderboard", "`,give @user <amt>` - Send tealeafs to another player\n`,lb` - Check top players globally", false)
			              .addField("Daily Rewards", "`,daily` - Claim your daily bonus tealeafs to boost your wallet", false);
			        }
			        case "help_combat", "help_dungeons" -> {
			            eb.setTitle("⚔️ Combat & RPG Guide")
			              .setDescription("Grow stronger, defeat monsters, and conquer bosses!\n💡 **Tip:** Equip the best weapons and heal between battles.")
			              .addField("Player Stats", "`,profile` - View your level & XP\n`,inv` - Check your current equipment", false)
			              .addField("Battle Commands", "`,fight` - Start a fight\n`,heal` - Recover HP\n`,boss` - Challenge a dungeon boss", false)
			              .addField("Tips", "Use potions wisely and upgrade your weapons to maximize damage!", false);
			        }
			        case "help_games" -> {
			            eb.setTitle("🎰 Games & Fun Guide")
			              .setDescription("Enjoy casino games and fun minigames!\n💡 **Tip:** Practice games to improve your skills and earn rewards!")
			              .addField("Casino Games", "`,blackjack`, `,slots`, `,highlow`", false)
			              .addField("Mini Games & Fun", "`,fish`, `,beg`, `,crime`, `,dice`, `,hangman`", false)
			              .addField("Tips", "Try multiple games daily to earn tealeafs and improve your stats!", false);
			        }
			        case "help_shop" -> {
			            eb.setTitle("🏪 Marketplace Guide")
			              .setDescription("Browse the shop, buy items, and equip them to enhance your adventures!\n💡 **Tip:** Always check your balance before buying and remember better gear boosts combat performance.")
			              .addField("Buying Items", "`,shop` - View items in the marketplace\n`,buy <item>` - Purchase an item\n`,equip <item>` - Equip your new gear", false)
			              .addField("Gear Benefits", "Weapons increase fight damage, potions restore HP, armor reduces damage taken.", false)
			              .addField("Strategy", "Combine different gear types to maximize your combat efficiency!", false);
			        }
			        default -> {
			            // 4. Secret game or unhandled
			            if (buttonId.startsWith("hl_")) {
			                java.util.Random r = new java.util.Random();
			                int secret = r.nextInt(100) + 1;
			                event.editMessage("🎲 The secret number was **" + secret + "**!\nGame Over.").setComponents().queue();
			            }
			            return; // Not a help button
			        }
			    }

			    // ---------------------------
			    // 5. Send the updated embed
			    // ---------------------------
			    event.editMessageEmbeds(eb.build()).queue();
			}

			
    public void handleStand(ButtonInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        Blackjack.GameState game = Blackjack.activeGames.get(userId); 
        
        if (game == null) {
            event.reply("No active game found!").setEphemeral(true).queue();
            return;
        }

        while (game.dealerTotal < 17) {
            String newDCard = CardLogic.getRandomCard();
            game.dealerHand += " " + newDCard;
            game.dealerTotal += CardLogic.getCardValue(newDCard);
        }

        String result;
        if (game.dealerTotal > 21 || game.playerTotal > game.dealerTotal) {
            int reward = 200; 
            Economy.addBalance(userId, reward); 
            result = "🎉 **You Win!** + " + reward + " tealeafs.";
        } else if (game.playerTotal == game.dealerTotal) {
            result = "🤝 **Push!** It's a tie.";
        } else {
            result = "💀 **Dealer Wins.**";
        }

        EmbedBuilder res = new EmbedBuilder();
        res.setTitle("🃏 Blackjack Final Result");
        res.setDescription(result);
        res.addField("Your Hand", game.playerHand + " (" + game.playerTotal + ")", true);
        res.addField("Dealer's Hand", game.dealerHand + " (" + game.dealerTotal + ")", true);

        event.editMessageEmbeds(res.build()).setComponents().queue();
        Blackjack.activeGames.remove(userId);
    }

    @SuppressWarnings("unused")
	private void handleProfile1(MessageReceivedEvent event, long userId) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("👤 " + event.getAuthor().getName() + "'s Hero Profile");
        eb.setColor(Color.decode("#00fbff")); 
        eb.setThumbnail(event.getAuthor().getEffectiveAvatarUrl());
        eb.addField("📊 Stats", "**Level:** " + Economy.getLevel(userId) + "\n**XP:** " + Economy.getXP(userId) + "/100", true);
        eb.addField("💰 Currency", "**Wallet:** " + Economy.getBalance(userId) + "\n**Bank:** " + Economy.getBankBalance(userId), true);
        eb.setFooter("Keep fighting to reach the next level!");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }
    
    @SuppressWarnings("unused")
	private void handleLeaderboard1(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("🏆 Global Wealth Leaderboard");
        eb.setColor(Color.RED);
        eb.setDescription(Economy.getTopBalances()); 
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @SuppressWarnings("unused")
	private void handleHeal1(MessageReceivedEvent event, long userId) {
        int currentHp = Economy.getHealth(userId);
        if (currentHp >= 100) {
            event.getChannel().sendMessage("❤️ You are already at full health!").queue();
            return;
        }
        Economy.addHealth(userId, 50);
        event.getChannel().sendMessage("🧪 You used a recovery item and healed **50 HP**! Current HP: " + Economy.getHealth(userId)).queue();
    }

    @SuppressWarnings("unused")
	private void handleMoneyAdd1(MessageReceivedEvent event, String args, long userId) {
        if (event.getMember() == null || !event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            event.getChannel().sendMessage("❌ You do not have permission!").queue();
            return;
        }
        try {
            int amount = Integer.parseInt(args.split("\\s+")[1]);
            Economy.addBalance(userId, amount);
            EmbedBuilder eb = new EmbedBuilder()
                .setTitle("💎 Admin Mint")
                .setDescription("Successfully added **" + amount + "** tealeafs!")
                .setColor(Color.decode("#FFD700"));
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,money add <amount>`").queue();
        }
    }

    @SuppressWarnings("unused")
	private void handleMoneyGive1(MessageReceivedEvent event, String args, long userId) {
        try {
            List<User> mentioned = event.getMessage().getMentions().getUsers();
            if (mentioned.isEmpty()) {
                event.getChannel().sendMessage("❌ You must mention a user!").queue();
                return;
            }

            // 1. Split the arguments into a list of words
            String[] splitArgs = args.split("\\s+");
            
            // 2. Get the VERY LAST word and try to turn it into a number
            // This ignores all the emojis/text in the middle!
            int amount = Integer.parseInt(splitArgs[splitArgs.length - 1]);
            
            long targetId = mentioned.get(0).getIdLong();
            int donorBalance = Economy.getBalance(userId);

            // 3. Check if the user actually has enough money
            if (donorBalance >= amount && amount > 0) {
                Economy.addBalance(userId, -amount);
                Economy.addBalance(targetId, amount);
                
                event.getChannel().sendMessage("💸 Sent **" + amount + "** tealeafs to " + mentioned.get(0).getAsMention() + "!").queue();
            } else {
                // This will tell you EXACTLY how much you have if it fails
                event.getChannel().sendMessage("❌ Insufficient funds! You have **" + donorBalance + "** tealeafs, but tried to send **" + amount + "**.").queue();
            }
        } catch (NumberFormatException e) {
            event.getChannel().sendMessage("❌ I couldn't find the amount! Make sure the number is at the very end of the message.").queue();
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,money give @user <amount>`").queue();
        }
    }

  
	@SuppressWarnings("unused")
	private void handleWithdraw1(MessageReceivedEvent event, String msg, long userId) {
        try {
            int amount = Integer.parseInt(msg.split(" ")[1]);
            if (Economy.withdraw(userId, amount)) {
                event.getChannel().sendMessage("🏧 Withdrew **" + amount + "** tealeafs!").queue();
            } else {
                event.getChannel().sendMessage("❌ Insufficient bank funds.").queue();
            }
        } catch (Exception e) {
            event.getChannel().sendMessage("❌ Usage: `,withdraw <amount>`").queue();
        }
    }

  
    @SuppressWarnings("unused")
	private void handleBuy1(MessageReceivedEvent event, String msg, long userId) {
        String itemId = msg.replace(",buy ", "").toLowerCase().trim();
        int price = 0;
        String itemName = "";

        // 1. Define Price Mapping
        switch (itemId) {
            case "iron_sword" -> { price = 2500; itemName = "Iron Sword"; }
            case "katana"     -> { price = 7500; itemName = "Katana"; }
            case "shadow_dagger" -> { price = 12000; itemName = "Shadow Dagger"; }
            case "mythic_blade"  -> { price = 15000; itemName = "Mythic Blade"; }
            case "dragon_slayer" -> { price = 50000; itemName = "Dragon Slayer"; }
            case "void_reaver"   -> { price = 100000; itemName = "Void Reaver"; }
            case "knight_plate"  -> { price = 5000; itemName = "Knight Plate"; }
            case "leather_tunic" -> { price = 1500; itemName = "Leather Tunic"; }
            case "health_pot"    -> { price = 500; itemName = "Health Potion"; }
            case "xp_booster"    -> { price = 2000; itemName = "XP Booster"; }
            default -> {
                event.getChannel().sendMessage("❌ Item not found!").queue();
                return;
            }
        }

        // 2. Check Balance once
        int currentBalance = Economy.getBalance(userId);
        if (currentBalance < price) {
            event.getChannel().sendMessage("❌ Not enough tealeafs! You need " + (price - currentBalance) + " more.").queue();
            return;
        }

        // 3. Process Transaction
        Economy.addBalance(userId, -price);

        if (itemId.contains("sword") || itemId.contains("blade") || itemId.contains("katana") || 
            itemId.contains("dagger") || itemId.contains("slayer") || itemId.contains("reaver")) {
            Inventory.setWeapon(userId, itemId);
        } else if (itemId.contains("plate") || itemId.contains("tunic")) {
            Inventory.setArmor(userId, itemId);
        } else if (itemId.equals("health_pot")) {
            Economy.addHealth(userId, 50);
        } else if (itemId.equals("xp_booster")) {
            Economy.addXP(userId, 500);
        }

        // 4. Send Success Embed
        EmbedBuilder eb = new EmbedBuilder()
            .setTitle("📦 Purchase Successful!")
            .setDescription("You bought a **" + itemName + "**!")
            .setColor(Color.GREEN)
            .setFooter("Balance: " + Economy.getBalance(userId) + " 🪙");
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    
    }
     // -------------------------
     // SLOT MACHINE HELPER
     // -------------------------
 // Inside your listener class, e.g., MessageListener
   

    public String[] getResponses() {
		return responses;
	}


	public class MyListener extends ListenerAdapter {

        // Magic 8-ball responses
        private final String[] responses = {
            "It is certain.", "It is decidedly so.", "Without a doubt.", "Yes definitely.",
            "You may rely on it.", "As I see it, yes.", "Most likely.", "Outlook good.",
            "Yes.", "Signs point to yes.", "Reply hazy, try again.", "Ask again later.",
            "Better not tell you now.", "Cannot predict now.", "Concentrate and ask again.",
            "Don't count on it.", "My reply is no.", "My sources say no.",
            "Outlook not so good.", "Very doubtful."
        };


        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            
            // We'll call it 'message' here so your code below works perfectly
            String message = event.getMessage().getContentRaw();

            // Check if it starts with your prefix
            if (message.startsWith(",")) {
                
                // PING COMMAND
                if (message.equalsIgnoreCase(",ping")) {
                    event.getChannel().sendMessage("Pong! Finally working.").queue();
                } 
                
                // SLOTS COMMAND
                else if (message.startsWith(",slots")) {
                    int bet = 50; 
                    String[] parts = message.split("\\s+");
                    if (parts.length > 1) {
                        try {
                            bet = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            // Stay at 50
                        }
                    }
                    handleSlots(event, bet);
                } 
                
                // 8-BALL COMMAND
                else if (message.startsWith(",8ball")) {
                    String[] parts = message.split("\\s+", 2);
                    if (parts.length < 2) {
                        event.getChannel().sendMessage("❌ You need to ask a question!").queue();
                    } else {
                        String randomResponse = responses[new java.util.Random().nextInt(responses.length)];
                        event.getChannel().sendMessage("🔮 **8-Ball says:** " + randomResponse).queue();
                    }
                }
            } 
        } // Closes onMessageReceived
        

        private void handleSlots(MessageReceivedEvent event, int bet) {
            long userId = event.getAuthor().getIdLong();
            int balance = Economy.getBalance(userId);

            if (balance < bet) {
                event.getChannel().sendMessage("❌ You don't have enough tealeafs to bet " + bet + "!").queue();
                return;
            }

            final String[] emojis = { "🍒", "🔔", "🍋", "💎", "⭐", "🍇" };
            Random rand = new Random();

            // Deduct bet immediately
            Economy.addBalance(userId, -bet);

            event.getChannel().sendMessage("🎰 Slot Machine\n🎲 Spinning...").queue(spinMessage -> {
                new Thread(() -> {
                    try {
                        String r1 = "", r2 = "", r3 = "";
                        for (int i = 0; i < 3; i++) {
                            r1 = emojis[rand.nextInt(emojis.length)];
                            r2 = emojis[rand.nextInt(emojis.length)];
                            r3 = emojis[rand.nextInt(emojis.length)];
                            
                            spinMessage.editMessage("🎰 Slot Machine\n[ " + r1 + " | " + r2 + " | " + r3 + " ]\n🎲 Spinning...").queue();
                            Thread.sleep(600); 
                        }

                        String finalResult;
                        if (r1.equals(r2) && r2.equals(r3)) {
                            int winAmount = bet * 5;
                            Economy.addBalance(userId, winAmount);
                            finalResult = "🎉 **JACKPOT!** You won **" + winAmount + "** tealeafs!";
                        } else if (r1.equals(r2) || r2.equals(r3) || r1.equals(r3)) {
                            int winAmount = (int) (bet * 1.5);
                            Economy.addBalance(userId, winAmount);
                            finalResult = "✨ **Small Win!** You won **" + winAmount + "** tealeafs.";
                        } else {
                            finalResult = "💀 **Better luck next time!**";
                        }

                        spinMessage.editMessage("🎰 Slot Machine\n[ " + r1 + " | " + r2 + " | " + r3 + " ]\n" + finalResult).queue();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            });
        }
    }
}