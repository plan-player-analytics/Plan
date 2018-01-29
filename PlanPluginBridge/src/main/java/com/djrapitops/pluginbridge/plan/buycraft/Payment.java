/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import java.util.UUID;

/**
 * Represents a BuyCraft payment.
 *
 * @author Rsl1122
 */
public class Payment implements Comparable<Payment> {

    private final double amount;
    private final String currency;
    private final UUID uuid;
    private final String playerName;
    private final long date;
    private final String packages;

    public Payment(double amount, String currency, UUID uuid, String playerName, long date, String packages) {
        this.amount = amount;
        this.currency = currency;
        this.uuid = uuid;
        this.playerName = playerName;
        this.date = date;
        this.packages = packages;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getDate() {
        return date;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPackages() {
        return packages;
    }

    @Override
    public int compareTo(Payment o) {
        return this.playerName.toLowerCase().compareTo(o.playerName.toLowerCase());
    }
}