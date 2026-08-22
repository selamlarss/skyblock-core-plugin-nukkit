package net.skycore.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Clan {
    private String name;
    private UUID leader;
    private final List<UUID> members = new ArrayList<>();

    public Clan(String name, UUID leader) {
        this.name = name;
        this.leader = leader;
        this.members.add(leader);
    }

    public String getName() { return name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<UUID> getMembers() { return members; }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }
}
