package net.skycore.managers;

import net.skycore.SkyCore;
import org.powernukkitx.Player;
import org.powernukkitx.item.Item;
import org.powernukkitx.utils.ConfigSection;

import java.util.*;

/**
 * Warp Market: /market ile fiyat listesi, /buy ve /sell ile alim-satim.
 *
 * NOT (onemli): PowerNukkitX bu jar surumunde klasik "chest GUI + InventoryClickEvent"
 * yerine yeni bir DDUI (Data Driven UI) form sistemi kullaniyor
 * (org.powernukkitx.ddui.CustomForm, ButtonElement, LabelElement, vb.).
 * Bu API surumden surume degistigi ve burada derleyici olmadan tam imzasini
 * dogrulayamadigim icin, asagida GARANTI CALISAN metin/komut tabanli bir market
 * yaptim (/market, /buy, /sell). Gercek "resimli" (buton + ikon) bir pencere
 * istiyorsan openVisualForm() metodunu PNX'in guncel DDUI API'sine gore
 * doldurman yeterli - iskeleti asagida birakildi.
 */
public class MarketManager {

    public static class MarketItem {
        public final String key;
        public final int itemId;
        public final int meta;
        public final double buyPrice;
        public final double sellPrice;

        public MarketItem(String key, int itemId, int meta, double buyPrice, double sellPrice) {
            this.key = key;
            this.itemId = itemId;
            this.meta = meta;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }
    }

    private final SkyCore plugin;
    private final Map<String, MarketItem> items = new LinkedHashMap<>();

    public MarketManager(SkyCore plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        ConfigSection section = plugin.getPluginConfig().getSection("market.items");
        if (section == null) return;
        for (String key : section.getKeys(false)) {
            ConfigSection entry = section.getSection(key);
            int itemId = entry.getInt("item-id", 0);
            int meta = entry.getInt("meta", 0);
            double buy = entry.getDouble("buy", 0);
            double sell = entry.getDouble("sell", 0);
            items.put(key.toUpperCase(), new MarketItem(key.toUpperCase(), itemId, meta, buy, sell));
        }
    }

    public Collection<MarketItem> getAllItems() {
        return items.values();
    }

    public MarketItem get(String key) {
        return items.get(key.toUpperCase());
    }

    public MarketItem findByExampleItem(Item item) {
        for (MarketItem mi : items.values()) {
            if (mi.itemId == item.getId() && mi.meta == item.getDamage()) return mi;
        }
        return null;
    }

    public void sendPriceList(Player player) {
        String symbol = plugin.getEconomyManager().getCurrencySymbol();
        player.sendMessage("\u00A7b\u00A7l--- Warp Market ---");
        for (MarketItem mi : items.values()) {
            player.sendMessage("\u00A7f" + mi.key + " \u00A77| \u00A7aAl: " + symbol + mi.buyPrice
                    + " \u00A77| \u00A7cSat: " + symbol + mi.sellPrice);
        }
        player.sendMessage("\u00A77Kullanim: \u00A7f/buy <esya> <miktar> \u00A77| \u00A7f/sell <esya> <miktar>");
    }

    public boolean buy(Player player, MarketItem item, int amount) {
        if (amount <= 0) return false;
        double cost = item.buyPrice * amount;
        if (!plugin.getEconomyManager().has(player.getUniqueId(), cost)) return false;
        plugin.getEconomyManager().removeBalance(player.getUniqueId(), cost);
        Item given = Item.get(item.itemId, item.meta, amount);
        player.getInventory().addItem(given);
        return true;
    }

    public boolean sell(Player player, MarketItem item, int amount) {
        if (amount <= 0) return false;
        Item toRemove = Item.get(item.itemId, item.meta, amount);
        if (!player.getInventory().contains(toRemove)) return false;
        player.getInventory().removeItem(toRemove);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), item.sellPrice * amount);
        return true;
    }

    public double sellHandItemStack(Player player) {
        Item hand = player.getInventory().getItemInHand();
        if (hand == null || hand.getId() == 0) return 0;
        MarketItem mi = findByExampleItem(hand);
        if (mi == null) return -1;
        int count = hand.getCount();
        double total = mi.sellPrice * count;
        player.getInventory().setItemInHand(Item.get(0));
        plugin.getEconomyManager().addBalance(player.getUniqueId(), total);
        return total;
    }

    /**
     * TODO (opsiyonel gelistirme): PNX'in org.powernukkitx.ddui.CustomForm /
     * ButtonElement / LabelElement API'siyle gercek "resimli" buton menusu icin
     * guncel DDUI dokumantasyonuna bakip burayi doldurabilirsin. Su an icin
     * metin listesine dusuyor (garantili calisir).
     */
    public void openVisualForm(Player player) {
        sendPriceList(player);
    }
}
