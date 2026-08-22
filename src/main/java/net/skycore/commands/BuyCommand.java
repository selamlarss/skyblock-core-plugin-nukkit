package net.skycore.commands;

import net.skycore.SkyCore;
import net.skycore.managers.MarketManager;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class BuyCommand implements CommandExecutor {

    private final SkyCore plugin;

    public BuyCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage("\u00A7cKullanim: /buy <esya> [miktar]");
            return true;
        }
        MarketManager.MarketItem item = plugin.getMarketManager().get(args[0]);
        if (item == null) {
            player.sendMessage("\u00A7cBu esya markette yok. /market ile listeye bak.");
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
        boolean ok = plugin.getMarketManager().buy(player, item, amount);
        if (ok) {
            player.sendMessage("\u00A7a" + amount + "x " + item.key + " satin aldin (-" +
                    plugin.getEconomyManager().getCurrencySymbol() + String.format("%.2f", item.buyPrice * amount) + ").");
        } else {
            player.sendMessage("\u00A7cYeterli bakiyen yok.");
        }
        return true;
    }
}
