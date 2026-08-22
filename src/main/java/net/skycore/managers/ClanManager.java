package net.skycore.managers;

import net.skycore.SkyCore;
import net.skycore.model.Clan;
import org.powernukkitx.Player;
import org.powernukkitx.utils.Config;

import java.io.File;
import java.util.*;

public class ClanManager {

    private final SkyCore plugin;
    private final Config storage;
    private final Map<String, Clan> clansByName = new HashMap<>();
    private final Map<UUID, String> pendingClanInvites = new HashMap<>(); // invited -> clanName
    private final Set<UUID> clanChatToggled = new HashSet<>();

    public ClanManager(SkyCore plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), "clans.yml");
        this.storage = new Config(file, Config.YAML);
        load();
    }

    private void load() {
        Object raw = storage.get("clans");
        if (raw instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> map) {
                    try {
                        String name = String.valueOf(map.get("name"));
                        UUID leader = UUID.fromString(String.valueOf(map.get("leader")));
                        Clan clan = new Clan(name, leader);
                        clan.getMembers().clear();
                        Object membersRaw = map.get("members");
                        if (membersRaw instanceof List<?> ml) {
                            for (Object mo : ml) clan.getMembers().add(UUID.fromString(String.valueOf(mo)));
                        }
                        clansByName.put(name.toLowerCase(), clan);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void saveAll() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Clan clan : clansByName.values()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", clan.getName());
            m.put("leader", clan.getLeader().toString());
            List<String> members = new ArrayList<>();
            for (UUID u : clan.getMembers()) members.add(u.toString());
            m.put("members", members);
            list.add(m);
        }
        storage.set("clans", list);
        storage.save();
    }

    public boolean exists(String name) {
        return clansByName.containsKey(name.toLowerCase());
    }

    public Clan create(String name, UUID leader) {
        if (exists(name) || getClanOf(leader) != null) return null;
        Clan clan = new Clan(name, leader);
        clansByName.put(name.toLowerCase(), clan);
        return clan;
    }

    public Clan getClan(String name) {
        return clansByName.get(name.toLowerCase());
    }

    public Clan getClanOf(UUID uuid) {
        for (Clan clan : clansByName.values()) {
            if (clan.isMember(uuid)) return clan;
        }
        return null;
    }

    public void invite(String clanName, UUID target) {
        pendingClanInvites.put(target, clanName);
    }

    public boolean hasPendingInvite(UUID target) {
        return pendingClanInvites.containsKey(target);
    }

    public Clan acceptInvite(UUID target) {
        String clanName = pendingClanInvites.remove(target);
        if (clanName == null) return null;
        Clan clan = getClan(clanName);
        if (clan == null) return null;
        int max = plugin.getPluginConfig().getInt("clan.max-members", 15);
        if (clan.getMembers().size() >= max) return null;
        clan.getMembers().add(target);
        return clan;
    }

    public boolean leave(UUID uuid) {
        Clan clan = getClanOf(uuid);
        if (clan == null) return false;
        if (clan.getLeader().equals(uuid)) {
            clansByName.remove(clan.getName().toLowerCase());
            return true;
        }
        clan.getMembers().remove(uuid);
        return true;
    }

    public boolean disband(UUID leader) {
        Clan clan = getClanOf(leader);
        if (clan == null || !clan.getLeader().equals(leader)) return false;
        clansByName.remove(clan.getName().toLowerCase());
        return true;
    }

    public boolean kick(UUID leader, UUID target) {
        Clan clan = getClanOf(leader);
        if (clan == null || !clan.getLeader().equals(leader)) return false;
        return clan.getMembers().remove(target);
    }

    public void toggleClanChat(UUID uuid) {
        if (clanChatToggled.contains(uuid)) clanChatToggled.remove(uuid);
        else clanChatToggled.add(uuid);
    }

    public boolean isClanChatToggled(UUID uuid) {
        return clanChatToggled.contains(uuid);
    }

    public void sendClanMessage(Player sender, String message) {
        Clan clan = getClanOf(sender.getUniqueId());
        if (clan == null) return;
        String formatted = "\u00A78[Klan] \u00A7f" + clan.getName() + " \u00A77> \u00A7f" + sender.getName() + "\u00A7f: " + message;
        for (UUID memberUuid : clan.getMembers()) {
            Player p = plugin.getServer().getPlayer(memberUuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(formatted);
            }
        }
    }
}
