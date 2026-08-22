package net.skycore.managers;

import net.skycore.SkyCore;
import net.skycore.model.Island;
import org.powernukkitx.Player;
import org.powernukkitx.scheduler.PluginTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Basit sidebar scoreboard yoneticisi. PowerNukkitX'in cn.nukkit.scoreboard paketindeki
 * IScoreboard / ScoreboardManager API'si surumden surume degisebildigi icin burada,
 * her tick'te oyunculara "fake" bir scoreboard olarak (displayScoreboard yerine)
 * basit bir text-tabanli implementasyon kullanilir; sunucunuzdaki gercek Scoreboard API'sine
 * gore net.skycore.managers.ScoreboardManager#applyToPlayer metodunu uyarlaman gerekebilir.
 */
public class ScoreboardManager {

    private final SkyCore plugin;
    private final Set<java.util.UUID> disabled = new HashSet<>();

    public ScoreboardManager(SkyCore plugin) {
        this.plugin = plugin;
    }

    public void startAutoUpdate() {
        int interval = plugin.getPluginConfig().getInt("scoreboard.update-interval-ticks", 20);
        plugin.getServer().getScheduler().scheduleRepeatingTask(new PluginTask<SkyCore>(plugin) {
            @Override
            public void onRun(int currentTick) {
                for (Player p : plugin.getServer().getOnlinePlayers().values()) {
                    if (!disabled.contains(p.getUniqueId())) {
                        applyToPlayer(p);
                    }
                }
            }
        }, interval);
    }

    public void toggle(Player player) {
        java.util.UUID uuid = player.getUniqueId();
        if (disabled.contains(uuid)) {
            disabled.remove(uuid);
            applyToPlayer(player);
            player.sendMessage("\u00A7aScoreboard acildi.");
        } else {
            disabled.add(uuid);
            removeFromPlayer(player);
            player.sendMessage("\u00A7cScoreboard kapatildi.");
        }
    }

    public void applyToPlayer(Player player) {
        List<String> lines = plugin.getPluginConfig().getStringList("scoreboard.lines");
        String title = net.skycore.managers.RankManager.colorize(
                plugin.getPluginConfig().getString("scoreboard.title", "&b&lSkyBlock"));

        Island island = plugin.getIslandManager().getIslandOfMember(player.getUniqueId());
        int islandLevel = island != null ? island.getLevel() : 0;

        java.util.List<String> rendered = new java.util.ArrayList<>();
        for (String raw : lines) {
            String line = net.skycore.managers.RankManager.colorize(raw)
                    .replace("{player}", player.getName())
                    .replace("{rank}", plugin.getRankManager().getPrefix(player.getUniqueId()))
                    .replace("{currency}", plugin.getEconomyManager().getCurrencySymbol())
                    .replace("{balance}", String.format("%.1f", plugin.getEconomyManager().getBalance(player.getUniqueId())))
                    .replace("{islandlevel}", String.valueOf(islandLevel))
                    .replace("{online}", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
            rendered.add(line);
        }

        // NOT: Gercek PNX scoreboard packet/API'siyle gonderim burada yapilir.
        // API surumune gore asagidaki cagriyi (displayScoreboard vb.) kendi PNX
        // surumunuzdeki metod imzasina gore duzenlemeniz gerekebilir.
        plugin.getServer().getScoreboardManager().setScoreboardLines(player, title, rendered);
    }

    public void removeFromPlayer(Player player) {
        plugin.getServer().getScoreboardManager().removeScoreboard(player);
    }
}
