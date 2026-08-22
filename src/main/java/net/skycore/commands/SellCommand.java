package net.skycore.commands;

import net.skycore.SkyCore;
import net.skycore.managers.MarketManager;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class SellCommand implements CommandExecutor {

    private final SkyCore plugin;

    public SellCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }

        // /sell <esya> <miktar> -> belirli esya ve miktar sat
        if (args.length >= 1) {
            MarketManager.MarketItem item = plugin.getMarketManager().get(args[0]);
            if (item == null) {
                player.sendMessage("\u00A7cBu esya markette yok.");
                return true;
            }
            int amount = 1;
            if (args.length >= 2) {
                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage("\u00A7cGecersiz miktar.");
                    return true;
                }
            }
            boolean ok = plugin.getMarketManager().sell(player, item, amount);
            if (ok) {
                player.sendMessage("\u00A7a" + amount + "x " + item.key + " sattin (+" +
                        plugin.getEconomyManager().getCurrencySymbol() + String.format("%.2f", item.sellPrice * amount) + ").");
            } else {
                player.sendMessage("\u00A7cEnvanterinde yeterli miktarda yok.");
            }
            return true;
        }

        // /sell -> elde tuttugun yigini otomatik sat
        double result = plugin.getMarketManager().sellHandItemStack(player);
        if (result < 0) {
            player.sendMessage("\u00A7cElindeki esya markette satilamiyor.");
        } else if (result == 0) {
            player.sendMessage("\u00A7cElinde satacak bir esya yok.");
        } else {
            player.sendMessage("\u00A7aElindeki esyayi " + plugin.getEconomyManager().getCurrencySymbol()
                    + String.format("%.2f", result) + " karsiliginda sattin.");
        }
        return true;
    }
}
