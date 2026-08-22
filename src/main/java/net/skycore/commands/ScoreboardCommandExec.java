package net.skycore.commands;

import net.skycore.SkyCore;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class ScoreboardCommandExec implements CommandExecutor {

    private final SkyCore plugin;

    public ScoreboardCommandExec(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        plugin.getScoreboardManager().toggle(player);
        return true;
    }
}
