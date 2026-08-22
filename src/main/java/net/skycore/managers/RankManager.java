package net.skycore.managers;

import net.skycore.SkyCore;
import org.powernukkitx.utils.Config;
import org.powernukkitx.utils.ConfigSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Rutbe sistemi. config.yml -> ranks bolumunden rutbe tanimlarini okur,
 * playerranks.yml icinde her oyuncunun rutbesini saklar.
 */
public class RankManager {

    private final SkyCore plugin;
    private final Config storage;
    private final Map<UUID, String> playerRanks = new LinkedHashMap<>();
    private final List<String> rankOrder;
    private final String defaultRank = "default";

    public RankManager(SkyCore plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "playerranks.yml");
        this.storage = new Config(file, Config.YAML);
        this.rankOrder = plugin.getPluginConfig().getStringList("ranks.order");
        for (String key : storage.getKeys(false)) {
            try {
                playerRanks.put(UUID.fromString(key), storage.getString(key));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, String> e : playerRanks.entrySet()) {
            storage.set(e.getKey().toString(), e.getValue());
        }
        storage.save();
    }

    public String getRank(UUID uuid) {
        return playerRanks.getOrDefault(uuid, defaultRank);
    }

    public boolean setRank(UUID uuid, String rank) {
        if (!rankOrder.contains(rank)) return false;
        playerRanks.put(uuid, rank);
        return true;
    }

    public List<String> getRankOrder() {
        return rankOrder;
    }

    public ConfigSection getRankSection(String rank) {
        return plugin.getPluginConfig().getSection("ranks." + rank);
    }

    public String getPrefix(UUID uuid) {
        ConfigSection sec = getRankSection(getRank(uuid));
        if (sec == null) return "";
        return colorize(sec.getString("prefix", ""));
    }

    public String getColor(UUID uuid) {
        ConfigSection sec = getRankSection(getRank(uuid));
        if (sec == null) return "";
        return colorize(sec.getString("color", "&7"));
    }

    public String getChatFormat(UUID uuid) {
        ConfigSection sec = getRankSection(getRank(uuid));
        if (sec == null) return "{player}: {message}";
        return sec.getString("chat-format", "{prefix}{player}: {message}");
    }

    public static String colorize(String s) {
        if (s == null) return "";
        return s.replace("&", "\u00A7");
    }
}
