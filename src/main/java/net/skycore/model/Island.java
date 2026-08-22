package net.skycore.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Island {

    private UUID owner;
    private final List<UUID> members = new ArrayList<>();
    private int centerX;
    private int centerY;
    private int centerZ;
    private int level = 0;
    private String warpMessage = null; // if set, island is public-warpable (community warp list)
    private boolean warpEnabled = false;

    public Island(UUID owner, int centerX, int centerY, int centerZ) {
        this.owner = owner;
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
    }

    public UUID getOwner() { return owner; }
    public void setOwner(UUID owner) { this.owner = owner; }
    public List<UUID> getMembers() { return members; }
    public int getCenterX() { return centerX; }
    public int getCenterY() { return centerY; }
    public int getCenterZ() { return centerZ; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public String getWarpMessage() { return warpMessage; }
    public void setWarpMessage(String warpMessage) { this.warpMessage = warpMessage; }
    public boolean isWarpEnabled() { return warpEnabled; }
    public void setWarpEnabled(boolean warpEnabled) { this.warpEnabled = warpEnabled; }

    public boolean isMemberOrOwner(UUID uuid) {
        return owner.equals(uuid) || members.contains(uuid);
    }
}
