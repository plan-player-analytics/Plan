/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class FakeOfflinePlayer implements OfflinePlayer {

    private final UUID uuid;
    private final String name;

    public FakeOfflinePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public boolean isBanned() {
        return false;
    }

    @Override
    public boolean isWhitelisted() {
        return true;
    }

    @Override
    public void setWhitelisted(boolean bln) {
        /* Not used */
    }

    @Override
    public Player getPlayer() {
        return null;
    }

    @Override
    public long getFirstPlayed() {
        return 0L;
    }

    @Override
    public long getLastPlayed() {
        return 0L;
    }

    @Override
    public boolean hasPlayedBefore() {
        return true;
    }

    @Override
    public Location getBedSpawnLocation() {
        return new Location(null, 0, 0, 0);
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean bln) {
        /* Not used */
    }

    @Override
    public Map<String, Object> serialize() {
        return new HashMap<>();
    }

}
