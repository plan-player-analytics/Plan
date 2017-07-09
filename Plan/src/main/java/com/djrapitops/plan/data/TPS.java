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

    /**
     * Constructor.
     *
     * @param date time of the average calculation.
     * @param tps average tps for the last minute.
     * @param players average players for the last minute.
     */
    public TPS(long date, double tps, int players) {
        this.date = date;
        this.tps = tps;
        this.players = players;
    }

    /**
     * Get the time of the average calculation.
     *
     * @return epoch ms.
     */
    public long getDate() {
        return date;
    }

    /**
     * Get the average tps for the minute.
     *
     * @return 0-20 double
     */
    public double getTps() {
        return tps;
    }

    /**
     * Get the average players for the minute.
     *
     * @return Players online.
     */
    public int getPlayers() {
        return players;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (int) (this.date ^ (this.date >>> 32));
        hash = 97 * hash + (int) (Double.doubleToLongBits(this.tps) ^ (Double.doubleToLongBits(this.tps) >>> 32));
        hash = 97 * hash + this.players;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TPS other = (TPS) obj;
        if (this.date != other.date) {
            return false;
        }
        if (Double.doubleToLongBits(this.tps) != Double.doubleToLongBits(other.tps)) {
            return false;
        }
        if (this.players != other.players) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TPS{" + date + "|" + tps + "|" + players + '}';
    }
}
