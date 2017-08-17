/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan;

import main.java.com.djrapitops.plan.data.UserData;
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

    private final UserData data;

    public FakeOfflinePlayer(UserData data) {
        this.data = data;
    }

    @Override
    public boolean isOnline() {
        return data.isOnline();
    }

    @Override
    public String getName() {
        String name = data.getName();
        if (name == null) {
            name = "null";
        }
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return data.getUuid();
    }

    @Override
    public boolean isBanned() {
        return data.isBanned();
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
        return data.getRegistered();
    }

    @Override
    public long getLastPlayed() {
        return data.getLastPlayed();
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
        return data.isOp();
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
