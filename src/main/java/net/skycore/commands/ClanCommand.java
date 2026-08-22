package net.skycore.commands;

import net.skycore.SkyCore;
import net.skycore.model.Clan;
import org.powernukkitx.Player;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

public class ClanCommand implements CommandExecutor {

    private final SkyCore plugin;

    public ClanCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Bu komut sadece oyun icinde kullanilabilir.");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage("\u00A7cKullanim: /clan create <isim>");
                    return true;
                }
                double cost = plugin.getPluginConfig().getDouble("clan.create-cost", 500.0);
                if (!plugin.getEconomyManager().has(player.getUniqueId(), cost)) {
                    player.sendMessage("\u00A7cKlan kurmak icin " + plugin.getEconomyManager().getCurrencySymbol() + cost + " gerekli.");
                    return true;
                }
                Clan clan = plugin.getClanManager().create(args[1], player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("\u00A7cBu isimde bir klan var ya da zaten bir klanin var.");
                    return true;
                }
                plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost);
                player.sendMessage("\u00A7a\"" + clan.getName() + "\" klani kuruldu!");
            }
            case "invite" -> {
                Clan clan = plugin.getClanManager().getClanOf(player.getUniqueId());
                if (clan == null || !clan.getLeader().equals(player.getUniqueId())) {
                    player.sendMessage("\u00A7cSadece klan lideri davet edebilir.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage("\u00A7cKullanim: /clan invite <oyuncu>");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("\u00A7cOyuncu bulunamadi.");
                    return true;
                }
                plugin.getClanManager().invite(clan.getName(), target.getUniqueId());
                player.sendMessage("\u00A7a" + target.getName() + " davet edildi.");
                target.sendMessage("\u00A7e" + clan.getName() + " klanina davet edildin! /clan accept");
            }
            case "accept" -> {
                Clan clan = plugin.getClanManager().acceptInvite(player.getUniqueId());
                player.sendMessage(clan != null ? "\u00A7a" + clan.getName() + " klanina katildin!" : "\u00A7cBekleyen bir klan davetin yok.");
            }
            case "leave" -> {
                boolean ok = plugin.getClanManager().leave(player.getUniqueId());
                player.sendMessage(ok ? "\u00A7aKlanindan ayrildin." : "\u00A7cBir klanin yok.");
            }
            case "disband" -> {
                boolean ok = plugin.getClanManager().disband(player.getUniqueId());
                player.sendMessage(ok ? "\u00A7aKlanini dagittin." : "\u00A7cKlan lideri degilsin.");
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage("\u00A7cKullanim: /clan kick <oyuncu>");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("\u00A7cOyuncu bulunamadi.");
                    return true;
                }
                boolean ok = plugin.getClanManager().kick(player.getUniqueId(), target.getUniqueId());
                player.sendMessage(ok ? "\u00A7aOyuncu klandan atildi." : "\u00A7cAtma yetkin yok ya da oyuncu klanda degil.");
            }
            case "chat", "c" -> {
                plugin.getClanManager().toggleClanChat(player.getUniqueId());
                boolean on = plugin.getClanManager().isClanChatToggled(player.getUniqueId());
                player.sendMessage(on ? "\u00A7aKlan sohbeti acildi (yazdiklarin sadece klanina gider)." : "\u00A7cKlan sohbeti kapatildi.");
            }
            case "info" -> {
                Clan clan = plugin.getClanManager().getClanOf(player.getUniqueId());
                if (clan == null) {
                    player.sendMessage("\u00A7cBir klanin yok.");
                    return true;
                }
                player.sendMessage("\u00A7bKlan: \u00A7f" + clan.getName());
                player.sendMessage("\u00A7bUye sayisi: \u00A7f" + clan.getMembers().size());
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("\u00A7b--- SkyCore Klan Komutlari ---");
        player.sendMessage("\u00A7f/clan create <isim>, invite <oyuncu>, accept, leave, disband, kick <oyuncu>, chat, info");
    }
}
