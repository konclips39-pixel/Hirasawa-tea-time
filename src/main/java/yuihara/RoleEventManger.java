package yuihara;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// We extend ListenerAdapter so JDA knows this class listens for events
public class RoleEventManger extends ListenerAdapter {

    // This method triggers whenever someone gets a new role
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        String user = event.getUser().getName();
        
        // Log it to your console for testing
        System.out.println("Role added to: " + user);
        
        // You can add logic here, like sending a message when 
        // someone gets a specific "VIP" or "Member" role.
    }
}