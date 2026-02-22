package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import economy.Economy;
import java.util.Random;

@Component
public class Crime extends ListenerAdapter {

    public void handleCommand(MessageReceivedEvent event) {
        Random r = new Random();
        long userId = event.getAuthor().getIdLong();
        
        // 30% success rate (High Risk)
        if (r.nextInt(100) < 30) {
            int gain = r.nextInt(1000) + 500; // 500-1500 tealeafs
            
            // UPDATED: Now uses addTealeafs for MongoDB save
            Economy.addTealeafs(userId, gain);
            
            event.getChannel().sendMessage("🥷 **SUCCESS!** You successfully raided the Imperial Tea Reserve and escaped with **" + gain + " 🍃 Tealeafs**!").queue();
        } else {
            int fine = 300;
            
            // Subtracting the fine from MongoDB
            Economy.addTealeafs(userId, -fine);
            
            event.getChannel().sendMessage("🚓 **BUSTED!** The Tea Inspectors caught you smuggling low-grade leaves. You were fined **" + fine + " 🍃 Tealeafs**.").queue();
        }
    }

	public static Object run(MessageReceivedEvent event) {
		// TODO Auto-generated method stub
		return null;
	}
}