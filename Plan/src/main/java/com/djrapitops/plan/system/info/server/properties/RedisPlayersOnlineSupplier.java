package com.djrapitops.plan.system.info.server.properties;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;

import java.util.function.Supplier;

/**
 * Players online supplier when using RedisBungee.
 *
 * @author Rsl1122
 */
public class RedisPlayersOnlineSupplier implements Supplier<Integer> {

    @Override
    public Integer get() {
        return RedisBungee.getApi().getPlayerCount();
    }
}