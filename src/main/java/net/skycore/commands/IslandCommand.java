package net.skycore.commands;

import net.skycore.SkyCore;
import net.skycore.model.Island;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

import java.util.UUID;

public class IslandCommand implements CommandExecutor {

    private final SkyCore plugin;

    public IslandCommand(SkyCore plugin) {
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
                if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                    player.sendMessage("\u00A7cZaten bir adan var!");
                    return true;
                }
                Island island = plugin.getIslandManager().createIsland(player);
                if (island != null) {
                    player.sendMessage("\u00A7aAdan olusturuldu! Hos geldin.");
                } else {
                    player.sendMessage("\u00A7cAda olusturulamadi.");
                }
            }
            case "home", "tp" -> {
                Island island = plugin.getIslandManager().getIslandOfMember(player.getUniqueId());
                if (island == null) {
                    player.sendMessage("\u00A7cBir adan yok. /island create ile olustur.");
                    return true;
                }
                plugin.getIslandManager().teleportToIsland(player, island);
                player.sendMessage("\u00A7aAdana isinlandin.");
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage("\u00A7cKullanim: /island invite <oyuncu>");
                    return true;
                }
                Server server = plugin.getServer();
                Player target = server.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("\u00A7cOyuncu bulunamadi.");
                    return true;
                }
                boolean ok = plugin.getIslandManager().invite(player, target.getUniqueId());
                if (ok) {
                    player.sendMessage("\u00A7a" + target.getName() + " davet edildi.");
                    target.sendMessage("\u00A7e" + player.getName() + " seni adasina davet etti! /island accept " + player.getName());
                } else {
                    player.sendMessage("\u00A7cDavet gonderilemedi (ada dolu olabilir).");
                }
            }
            case "accept" -> {
                boolean ok = plugin.getIslandManager().acceptInvite(player.getUniqueId());
                player.sendMessage(ok ? "\u00A7aDaveti kabul ettin, artik adanin uyesisin." : "\u00A7cBekleyen bir davetin yok.");
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage("\u00A7cKullanim: /island kick <oyuncu>");
                    return true;
                }
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("\u00A7cOyuncu bulunamadi.");
                    return true;
                }
                boolean ok = plugin.getIslandManager().kick(player, target.getUniqueId());
                player.sendMessage(ok ? "\u00A7aOyuncu adandan cikarildi." : "\u00A7cBu oyuncu adanda degil.");
            }
            case "leave" -> {
                boolean ok = plugin.getIslandManager().leave(player.getUniqueId());
                player.sendMessage(ok ? "\u00A7aAdadan ayrildin." : "\u00A7cBir adaya uye degilsin.");
            }
            case "delete" -> {
                boolean ok = plugin.getIslandManager().deleteIsland(player.getUniqueId());
                player.sendMessage(ok ? "\u00A7aAdan silindi." : "\u00A7cSenin bir adan yok (ya da sahibi degilsin).");
            }
            case "level" -> {
                Island island = plugin.getIslandManager().getIslandOfMember(player.getUniqueId());
                if (island == null) {
                    player.sendMessage("\u00A7cBir adan yok.");
                    return true;
                }
                player.sendMessage("\u00A7bAda seviyen: \u00A7f" + plugin.getIslandManager().calculateLevel(island));
            }
            default -> sendHelp(player);
        }
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("\u00A7b--- SkyCore Ada Komutlari ---");
        player.sendMessage("\u00A7f/island create \u00A77- Ada olustur");
        player.sendMessage("\u00A7f/island home \u00A77- Adana isinlan");
        player.sendMessage("\u00A7f/island invite <oyuncu> \u00A77- Davet et");
        player.sendMessage("\u00A7f/island accept \u00A77- Daveti kabul et");
        player.sendMessage("\u00A7f/island kick <oyuncu> \u00A77- Uyeyi at");
        player.sendMessage("\u00A7f/island leave \u00A77- Adadan ayril");
        player.sendMessage("\u00A7f/island delete \u00A77- Adani sil");
        player.sendMessage("\u00A7f/island level \u00A77- Ada seviyeni gor");
    }
}
