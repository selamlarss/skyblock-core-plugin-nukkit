package net.skycore.managers;

import net.skycore.SkyCore;
import org.powernukkitx.utils.Config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Basit ama tam fonksiyonel dahili ekonomi sistemi.
 * economy.yml icinde uuid -> bakiye seklinde saklanir.
 */
public class EconomyManager {

    private final SkyCore plugin;
    private final Config storage;
    private final Map<UUID, Double> balances = new HashMap<>();
    private final double startingBalance;

    public EconomyManager(SkyCore plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "economy.yml");
        this.storage = new Config(file, Config.YAML);
        this.startingBalance = plugin.getPluginConfig().getDouble("economy.starting-balance", 100.0);
        load();
    }

    private void load() {
        for (String key : storage.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                balances.put(uuid, storage.getDouble(key, startingBalance));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, Double> e : balances.entrySet()) {
            storage.set(e.getKey().toString(), e.getValue());
        }
        storage.save();
    }

    public double getBalance(UUID uuid) {
        return balances.computeIfAbsent(uuid, k -> startingBalance);
    }

    public void setBalance(UUID uuid, double amount) {
        balances.put(uuid, Math.max(0, amount));
    }

    public void addBalance(UUID uuid, double amount) {
        setBalance(uuid, getBalance(uuid) + amount);
    }

    public boolean removeBalance(UUID uuid, double amount) {
        double bal = getBalance(uuid);
        if (bal < amount) return false;
        setBalance(uuid, bal - amount);
        return true;
    }

    public boolean has(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        if (!removeBalance(from, amount)) return false;
        addBalance(to, amount);
        return true;
    }

    public String getCurrencySymbol() {
        return plugin.getPluginConfig().getString("economy.currency-symbol", "$");
    }

    public Map<UUID, Double> getAllBalances() {
        return balances;
    }
}
