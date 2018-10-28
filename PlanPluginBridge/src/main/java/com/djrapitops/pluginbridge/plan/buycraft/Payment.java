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
package com.djrapitops.pluginbridge.plan.buycraft;

import java.util.UUID;

/**
 * Represents a BuyCraft payment.
 *
 * Payments are sorted most recent first by natural ordering.
 *
 * @author Rsl1122
 */
class Payment implements Comparable<Payment> {

    private final double amount;
    private final String currency;
    private final UUID uuid;
    private final String playerName;
    private final long date;
    private final String packages;

    Payment(double amount, String currency, UUID uuid, String playerName, long date, String packages) {
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
        return -Long.compare(this.date, o.date);
    }
}