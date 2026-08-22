package net.skycore.commands;

import net.skycore.SkyCore;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class PayCommand implements CommandExecutor {

    private final SkyCore plugin;

    public PayCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage("\u00A7cKullanim: /pay <oyuncu> <miktar>");
            return true;
        }
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            player.sendMessage("\u00A7cOyuncu bulunamadi.");
            return true;
        }
        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("\u00A7cKendine para gonderemezsin.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("\u00A7cGecersiz miktar.");
            return true;
        }
        if (amount <= 0) {
            player.sendMessage("\u00A7cMiktar 0'dan buyuk olmali.");
            return true;
        }
        boolean ok = plugin.getEconomyManager().transfer(player.getUniqueId(), target.getUniqueId(), amount);
        if (!ok) {
            player.sendMessage("\u00A7cYeterli bakiyen yok.");
            return true;
        }
        String symbol = plugin.getEconomyManager().getCurrencySymbol();
        player.sendMessage("\u00A7a" + symbol + amount + " tutarini " + target.getName() + " adli oyuncuya gonderdin.");
        target.sendMessage("\u00A7a" + player.getName() + " sana " + symbol + amount + " gonderdi.");
        return true;
    }
}
