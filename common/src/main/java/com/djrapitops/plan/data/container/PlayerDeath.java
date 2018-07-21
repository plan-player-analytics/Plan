package com.djrapitops.plan.data.container;


import com.djrapitops.plan.data.store.objects.DateHolder;

import java.util.UUID;

public class PlayerDeath implements DateHolder {

    private final UUID killer;
    private final long date;
    private final String weapon;

    public PlayerDeath(UUID killer, String weapon, long date) {
        this.killer = killer;
        this.date = date;
        this.weapon = weapon;
    }

    public UUID getKiller() {
        return killer;
    }

    @Override
    public long getDate() {
        return date;
    }

    public String getWeapon() {
        return weapon;
    }
}
