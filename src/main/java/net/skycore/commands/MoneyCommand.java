package net.skycore.commands;

import net.skycore.SkyCore;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class MoneyCommand implements CommandExecutor {

    private final SkyCore plugin;

    public MoneyCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String symbol = plugin.getEconomyManager().getCurrencySymbol();

        if (args.length >= 1) {
            Player target = plugin.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("\u00A7cOyuncu bulunamadi.");
                return true;
            }
            double bal = plugin.getEconomyManager().getBalance(target.getUniqueId());
            sender.sendMessage("\u00A7f" + target.getName() + "\u00A77'nin bakiyesi: \u00A7a" + symbol + String.format("%.2f", bal));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Kullanim: /money <oyuncu>");
            return true;
        }
        double bal = plugin.getEconomyManager().getBalance(player.getUniqueId());
        sender.sendMessage("\u00A77Bakiyen: \u00A7a" + symbol + String.format("%.2f", bal));
        return true;
    }
}
