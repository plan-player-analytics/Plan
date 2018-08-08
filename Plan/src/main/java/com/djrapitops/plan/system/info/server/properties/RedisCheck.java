package com.djrapitops.plan.system.info.server.properties;

/**
 * Utility class for checking if RedisBungee API is available.
 *
 * @author Rsl1122
 */
public class RedisCheck {

    public static boolean isClassAvailable() {
        try {
            Class.forName("com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}