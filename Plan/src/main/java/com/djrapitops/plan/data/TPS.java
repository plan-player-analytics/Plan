/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data;

/**
 * Class containing single datapoint of TPS/players online.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPS {
    private final long date;
    private final double tps;
    private final int players;

    public TPS(long date, double tps, int players) {
        this.date = date;
        this.tps = tps;
        this.players = players;
    }

    public long getDate() {
        return date;
    }

    public double getTps() {
        return tps;
    }

    public int getPlayers() {
        return players;
    }
}
