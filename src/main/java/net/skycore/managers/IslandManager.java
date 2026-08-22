package net.skycore.managers;

import net.skycore.SkyCore;
import net.skycore.model.Island;
import org.powernukkitx.Player;
import org.powernukkitx.Server;
import org.powernukkitx.block.Block;
import org.powernukkitx.level.Level;
import org.powernukkitx.level.Position;
import org.powernukkitx.utils.Config;

import java.io.File;
import java.util.*;

/**
 * Ada (SkyBlock island) sistemi.
 * Her adaya sabit bir "grid" koordinati verilir ve basit bir baslangic platformu
 * (toprak + cim + agac + sandik) uretilir. Adalar arasi mesafe config'ten okunur.
 */
public class IslandManager {

    private final SkyCore plugin;
    private final Config storage;
    private final Map<UUID, Island> islandsByOwner = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>(); // invited -> owner
    private int nextGridIndex = 0;

    private final String worldName;
    private final int size;
    private final int spacing;
    private final int startX, startY, startZ;

    public IslandManager(SkyCore plugin) {
        this.plugin = plugin;
        this.worldName = plugin.getPluginConfig().getString("island.world-name", "skyblock");
        this.size = plugin.getPluginConfig().getInt("island.size", 100);
        this.spacing = plugin.getPluginConfig().getInt("island.spacing", 300);
        this.startX = plugin.getPluginConfig().getInt("island.start-x", 0);
        this.startY = plugin.getPluginConfig().getInt("island.start-y", 100);
        this.startZ = plugin.getPluginConfig().getInt("island.start-z", 0);

        File file = new File(plugin.getDataFolder(), "islands.yml");
        this.storage = new Config(file, Config.YAML);
        load();
    }

    private void load() {
        this.nextGridIndex = storage.getInt("next-grid-index", 0);
        Object rawList = storage.get("islands");
        if (rawList instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> map) {
                    try {
                        UUID owner = UUID.fromString(String.valueOf(map.get("owner")));
                        int x = (int) map.get("x");
                        int y = (int) map.get("y");
                        int z = (int) map.get("z");
                        Island island = new Island(owner, x, y, z);
                        island.setLevel(map.get("level") != null ? (int) map.get("level") : 0);
                        Object membersRaw = map.get("members");
                        if (membersRaw instanceof List<?> ml) {
                            for (Object mo : ml) island.getMembers().add(UUID.fromString(String.valueOf(mo)));
                        }
                        islandsByOwner.put(owner, island);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void saveAll() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Island isl : islandsByOwner.values()) {
            Map<String, Object> m = new HashMap<>();
            m.put("owner", isl.getOwner().toString());
            m.put("x", isl.getCenterX());
            m.put("y", isl.getCenterY());
            m.put("z", isl.getCenterZ());
            m.put("level", isl.getLevel());
            List<String> members = new ArrayList<>();
            for (UUID u : isl.getMembers()) members.add(u.toString());
            m.put("members", members);
            list.add(m);
        }
        storage.set("islands", list);
        storage.set("next-grid-index", nextGridIndex);
        storage.save();
    }

    public boolean hasIsland(UUID uuid) {
        if (islandsByOwner.containsKey(uuid)) return true;
        return getIslandOfMember(uuid) != null;
    }

    public Island getOwnedIsland(UUID uuid) {
        return islandsByOwner.get(uuid);
    }

    public Island getIslandOfMember(UUID uuid) {
        if (islandsByOwner.containsKey(uuid)) return islandsByOwner.get(uuid);
        for (Island isl : islandsByOwner.values()) {
            if (isl.getMembers().contains(uuid)) return isl;
        }
        return null;
    }

    /** Yeni bir ada olusturur, dunyada basit bir platform yerlestirir ve sahibi doner. */
    public Island createIsland(Player player) {
        UUID uuid = player.getUniqueId();
        if (hasIsland(uuid)) return null;

        int gridX = nextGridIndex % 20;
        int gridZ = nextGridIndex / 20;
        nextGridIndex++;

        int cx = startX + gridX * spacing;
        int cz = startZ + gridZ * spacing;
        int cy = startY;

        Island island = new Island(uuid, cx, cy, cz);
        islandsByOwner.put(uuid, island);

        buildStarterPlatform(cx, cy, cz);
        teleportToIsland(player, island);
        return island;
    }

    private Level getIslandWorld() {
        Server server = plugin.getServer();
        Level level = server.getLevelByName(worldName);
        if (level == null) {
            server.loadLevel(worldName);
            level = server.getLevelByName(worldName);
        }
        return level;
    }

    /** Basit bir baslangic adasi: 5x5 toprak, ustunde cim, ortada bir agac gudugu ve sandik. */
    private void buildStarterPlatform(int cx, int cy, int cz) {
        Level level = getIslandWorld();
        if (level == null) {
            plugin.getLogger().warning("'" + worldName + "' adli dunya bulunamadi! Bir void dunyasi olusturup config.yml'de belirtin.");
            return;
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(cx + x, cy - 1, cz + z, Block.get(Block.DIRT));
                level.setBlock(cx + x, cy, cz + z, Block.get(Block.DIRT));
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(cx + x, cy + 1, cz + z, Block.get(Block.AIR));
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setBlock(cx + x, cy - 1, cz + z, Block.get(Block.GRASS));
            }
        }
        // basit agac
        level.setBlock(cx, cy, cz + 2, Block.get(Block.LOG));
        level.setBlock(cx, cy + 1, cz + 2, Block.get(Block.LOG));
        level.setBlock(cx, cy + 2, cz + 2, Block.get(Block.LEAVES));
        // sandik
        level.setBlock(cx, cy, cz - 2, Block.get(Block.CHEST));
    }

    public void teleportToIsland(Player player, Island island) {
        Level level = getIslandWorld();
        if (level == null) return;
        Position pos = new Position(island.getCenterX() + 0.5, island.getCenterY() + 1, island.getCenterZ() + 0.5, level);
        player.teleport(pos);
    }

    public boolean invite(Player owner, UUID target) {
        Island island = getOwnedIsland(owner.getUniqueId());
        if (island == null) return false;
        int max = plugin.getPluginConfig().getInt("island.max-members", 6);
        if (island.getMembers().size() + 1 >= max) return false;
        pendingInvites.put(target, owner.getUniqueId());
        return true;
    }

    public boolean acceptInvite(UUID target) {
        UUID ownerUuid = pendingInvites.remove(target);
        if (ownerUuid == null) return false;
        Island island = islandsByOwner.get(ownerUuid);
        if (island == null) return false;
        island.getMembers().add(target);
        return true;
    }

    public boolean hasPendingInvite(UUID target) {
        return pendingInvites.containsKey(target);
    }

    public boolean kick(Player owner, UUID target) {
        Island island = getOwnedIsland(owner.getUniqueId());
        if (island == null) return false;
        return island.getMembers().remove(target);
    }

    public boolean leave(UUID uuid) {
        for (Island island : islandsByOwner.values()) {
            if (island.getMembers().remove(uuid)) return true;
        }
        return false;
    }

    public boolean deleteIsland(UUID owner) {
        return islandsByOwner.remove(owner) != null;
    }

    /** Ada seviyesini basitce eleman sayisi ve zamanla arttirilabilecek bir deger olarak hesaplar. */
    public int calculateLevel(Island island) {
        double perBlock = plugin.getPluginConfig().getDouble("island.level-per-block-value", 0.5);
        // basitlestirilmis: seviye, ada sahibinin manuel /island level ile tetikledigi bir taramaya dayanir.
        // gercek blok taramasi sunucu performansi icin async yapilmalidir; burada placeholder olarak
        // mevcut level degeri + 1 dondurulur (admin/otomasyonla genisletilebilir).
        return island.getLevel();
    }

    public Collection<Island> getAllIslands() {
        return islandsByOwner.values();
    }
}
