package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class Jobs extends ListenerAdapter {

    // Use LinkedHashMap to keep the order from cheapest to best
    private static final Map<String, JobInfo> jobRegistry = new LinkedHashMap<>();

    private static class JobInfo {
        String name;
        int pay;
        int requiredLevel;
        String description;

        JobInfo(String name, int pay, int level, String desc) {
            this.name = name;
            this.pay = pay;
            this.requiredLevel = level;
            this.description = desc;
        }
    }

    static {
        jobRegistry.put("apprentice", new JobInfo("Tea Apprentice", 200, 1, "Standard leaf sorting."));
        jobRegistry.put("merchant", new JobInfo("Leaf Merchant", 450, 5, "Trading rare Oolong in the markets."));
        jobRegistry.put("guerrilla", new JobInfo("Tea Smuggler", 900, 15, "High-risk transport of imperial blends."));
        jobRegistry.put("master", new JobInfo("Grand Tea Master", 2500, 30, "The highest honor in the gardens."));
    }

    public void handleCommand(MessageReceivedEvent event, String[] args) {
        if (args.length == 0) {
            sendHelp(event);
            return;
        }

        String subCommand = args[0].toLowerCase();
        long userId = event.getAuthor().getIdLong();

        switch (subCommand) {
            case "list":
                listJobs(event, userId);
                break;
            case "apply":
                if (args.length < 2) {
                    event.getChannel().sendMessage("❌ Specify a job! Example: `,job apply merchant`").queue();
                } else {
                    applyForJob(event, userId, args[1].toLowerCase());
                }
                break;
            case "work":
                processWork(event, userId);
                break;
            default:
                sendHelp(event);
        }
    }

    private void listJobs(MessageReceivedEvent event, long userId) {
        int userLevel = Economy.getLevel(userId);
        EmbedBuilder eb = new EmbedBuilder()
                .setTitle("💼 Imperial Career Board")
                .setColor(Color.YELLOW)
                .setDescription("Apply for a job to increase your daily earnings. Higher levels unlock better pay!");

        jobRegistry.forEach((id, info) -> {
            String status = (userLevel >= info.requiredLevel) ? "✅ Available" : "🔒 Requires Lvl " + info.requiredLevel;
            eb.addField(info.name + " (" + id + ")", 
                "Pay: **" + info.pay + " 🍃**\n" + info.description + "\nStatus: `" + status + "`", true);
        });

        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    private void applyForJob(MessageReceivedEvent event, long userId, String jobId) {
        if (!jobRegistry.containsKey(jobId)) {
            event.getChannel().sendMessage("❌ That job doesn't exist! Use `,job list`.").queue();
            return;
        }

        JobInfo info = jobRegistry.get(jobId);
        if (Economy.getLevel(userId) < info.requiredLevel) {
            event.getChannel().sendMessage("❌ Your reputation is too low! You need Level **" + info.requiredLevel + "** for this position.").queue();
            return;
        }

        // Save the job ID to MongoDB
        Economy.setUserJob(userId, jobId);
        event.getChannel().sendMessage("🎉 Congratulations! You are now a **" + info.name + "**. Use `,job work` to start earning.").queue();
    }

    private void processWork(MessageReceivedEvent event, long userId) {
        String currentJobId = Economy.getUserJob(userId);
        if (currentJobId == null || currentJobId.equalsIgnoreCase("none")) {
            event.getChannel().sendMessage("⚠️ You are unemployed! Use `,job list` and `,job apply <id>` first.").queue();
            return;
        }

        // Cooldown Logic
        long lastWork = Economy.getLastWorkTime(userId);
        long cooldown = TimeUnit.HOURS.toMillis(2);
        long diff = System.currentTimeMillis() - lastWork;

        if (diff < cooldown) {
            long remaining = cooldown - diff;
            event.getChannel().sendMessage("⏳ You're exhausted! Return in **" + TimeUnit.MILLISECONDS.toMinutes(remaining) + " minutes**.").queue();
            return;
        }

        JobInfo info = jobRegistry.get(currentJobId);
        Economy.addTealeafs(userId, info.pay);
        Economy.addXP(userId, 20); // Working grants a bit of XP
        Economy.setLastWorkTime(userId, System.currentTimeMillis());

        event.getChannel().sendMessage("🛠️ **Work Complete!** As a " + info.name + ", you earned **" + info.pay + " 🍃 Tealeafs** and **20 XP**.").queue();
    }

    private void sendHelp(MessageReceivedEvent event) {
        event.getChannel().sendMessage("📋 **Job Commands:**\n`,job list` - See available careers\n`,job apply <id>` - Start a new career\n`,job work` - Earn money from your current job").queue();
    }

	public static void listJobs(MessageReceivedEvent event) {
		
	}

	public static void apply(MessageReceivedEvent event, String trim) {
		
	}
}