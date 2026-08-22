package net.skycore;

import org.powernukkitx.plugin.PluginBase;
import org.powernukkitx.utils.Config;

import net.skycore.commands.*;
import net.skycore.listeners.ChatAndJoinListener;
import net.skycore.managers.*;

import java.io.File;

public class SkyCore extends PluginBase {

    private static SkyCore instance;

    private Config config;
    private EconomyManager economyManager;
    private IslandManager islandManager;
    private ClanManager clanManager;
    private RankManager rankManager;
    private MarketManager marketManager;
    private ScoreboardManager scoreboardManager;

    public static SkyCore get() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        this.config = new Config(new File(getDataFolder(), "config.yml"), Config.YAML);

        // Managers (this order matters: economy/rank before island/clan before scoreboard)
        this.economyManager = new EconomyManager(this);
        this.rankManager = new RankManager(this);
        this.islandManager = new IslandManager(this);
        this.clanManager = new ClanManager(this);
        this.marketManager = new MarketManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        // Listeners
        getServer().getPluginManager().registerEvents(new ChatAndJoinListener(this), this);

        // Commands
        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("money").setExecutor(new MoneyCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("baltop").setExecutor(new BalTopCommand(this));
        getCommand("clan").setExecutor(new ClanCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("market").setExecutor(new MarketCommand(this));
        getCommand("buy").setExecutor(new BuyCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("scoreboard").setExecutor(new ScoreboardCommandExec(this));

        // Periodic scoreboard refresh (every second by default, see config)
        scoreboardManager.startAutoUpdate();

        getLogger().info("SkyCore etkinlestirildi. Ada, ekonomi, klan, rutbe, scoreboard ve market hazir.");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) economyManager.saveAll();
        if (islandManager != null) islandManager.saveAll();
        if (clanManager != null) clanManager.saveAll();
        if (rankManager != null) rankManager.saveAll();
        getLogger().info("SkyCore devre disi birakildi, veriler kaydedildi.");
    }

    public void saveDefaultConfig() {
        File f = new File(getDataFolder(), "config.yml");
        if (!f.exists()) {
            saveResource("config.yml", false);
        }
    }

    public Config getPluginConfig() {
        return config;
    }

    public EconomyManager getEconomyManager() { return economyManager; }
    public IslandManager getIslandManager() { return islandManager; }
    public ClanManager getClanManager() { return clanManager; }
    public RankManager getRankManager() { return rankManager; }
    public MarketManager getMarketManager() { return marketManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
}
