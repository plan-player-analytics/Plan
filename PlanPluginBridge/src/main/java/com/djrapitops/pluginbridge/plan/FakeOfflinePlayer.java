/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
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

    public FakeOfflinePlayer(PlayerContainer player) {
        this(player.getValue(PlayerKeys.UUID).orElse(null), player.getValue(PlayerKeys.NAME).orElse("Notch"));
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
