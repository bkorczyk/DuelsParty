import org.antix.duelsparty.DuelsPartyPlugin;
import org.antix.duelsparty.command.SubCommand;
import org.bukkit.entity.Player;

public class SubDuelQueue implements SubCommand {
    @Override
    public void execute(Player player, String[] args, String lang) {
        if (args.length < 2) {
            player.sendMessage("§6§l» §cPoprawne użycie: /duel queue <kit>");
            return;
        }
        String kitId = args[1].toLowerCase();
        DuelsPartyPlugin.getInstance().getDuelManager().toggleQueue(player, kitId);
    }
}