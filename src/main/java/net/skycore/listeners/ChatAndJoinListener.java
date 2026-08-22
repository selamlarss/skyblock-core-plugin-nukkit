package net.skycore.listeners;

import net.skycore.SkyCore;
import net.skycore.model.Clan;
import org.powernukkitx.Player;
import org.powernukkitx.event.EventHandler;
import org.powernukkitx.event.Listener;
import org.powernukkitx.event.player.PlayerChatEvent;
import org.powernukkitx.event.player.PlayerJoinEvent;
import org.powernukkitx.event.player.PlayerQuitEvent;

public class ChatAndJoinListener implements Listener {

    private final SkyCore plugin;

    public ChatAndJoinListener(SkyCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.getRankManager().getPrefix(player.getUniqueId());
        event.setJoinMessage(prefix + player.getName() + " \u00A7asunucuya katildi.");
        plugin.getScoreboardManager().applyToPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String prefix = plugin.getRankManager().getPrefix(player.getUniqueId());
        event.setQuitMessage(prefix + player.getName() + " \u00A7csunucudan ayrildi.");
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player player = event.getPlayer();

        // Klan sohbeti acilmissa mesaji sadece klana gonder ve genel chat'i iptal et
        if (plugin.getClanManager().isClanChatToggled(player.getUniqueId())) {
            Clan clan = plugin.getClanManager().getClanOf(player.getUniqueId());
            if (clan != null) {
                event.setCancelled(true);
                plugin.getClanManager().sendClanMessage(player, event.getMessage());
                return;
            }
        }

        String format = plugin.getRankManager().getChatFormat(player.getUniqueId());
        String prefix = plugin.getRankManager().getPrefix(player.getUniqueId());
        String rendered = format
                .replace("{prefix}", prefix)
                .replace("{player}", player.getName())
                .replace("{message}", event.getMessage());
        event.setFormat(net.skycore.managers.RankManager.colorize(rendered));
    }
}
