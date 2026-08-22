package net.skycore.commands;

import net.skycore.SkyCore;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class RankCommand implements CommandExecutor {

    private final SkyCore plugin;

    public RankCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skycore.admin")) {
            sender.sendMessage("\u00A7cBu komutu kullanma yetkin yok.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            sender.sendMessage("\u00A7bMevcut rutbeler: \u00A7f" + String.join(", ", plugin.getRankManager().getRankOrder()));
            return true;
        }
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage("\u00A7cKullanim: /rank set <oyuncu> <rutbe>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("\u00A7cOyuncu bulunamadi (cevrimici olmali).");
                return true;
            }
            boolean ok = plugin.getRankManager().setRank(target.getUniqueId(), args[2].toLowerCase());
            if (ok) {
                sender.sendMessage("\u00A7a" + target.getName() + " artik \"" + args[2] + "\" rutbesinde.");
                target.sendMessage("\u00A7aRutben \"" + args[2] + "\" olarak guncellendi.");
            } else {
                sender.sendMessage("\u00A7cGecersiz rutbe. Mevcut: " + String.join(", ", plugin.getRankManager().getRankOrder()));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                sender.sendMessage("\u00A7cKullanim: /rank info <oyuncu>");
                return true;
            }
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("\u00A7cOyuncu bulunamadi.");
                return true;
            }
            sender.sendMessage("\u00A7f" + target.getName() + "\u00A77'nin rutbesi: \u00A7f" + plugin.getRankManager().getRank(target.getUniqueId()));
            return true;
        }
        sender.sendMessage("\u00A7cKullanim: /rank <set|list|info>");
        return true;
    }
}
