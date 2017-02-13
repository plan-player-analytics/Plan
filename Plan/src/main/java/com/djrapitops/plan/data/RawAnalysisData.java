package main.java.com.djrapitops.plan.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Rsl1122
 */
public class RawAnalysisData {

    private long gmZero;
    private long gmOne;
    private long gmTwo;
    private long gmThree;
    private long totalLoginTimes;
    private long totalPlaytime;
    private int totalBanned;
    private int active;
    private int joinleaver;
    private int inactive;
    private long totalKills;
    private long totalMobKills;
    private long totalDeaths;
    private int ops;
    private List<Integer> ages;
    private HashMap<String, Long> latestLogins;
    private HashMap<String, Long> playtimes;
    private List<SessionData> sessiondata;
    private HashMap<String, Integer> commandUse;
    private List<Long> registered;

    public RawAnalysisData() {
        gmZero = 0;
        gmOne = 0;
        gmTwo = 0;
        gmThree = 0;
        totalLoginTimes = 0;
        totalPlaytime = 0;
        totalBanned = 0;
        active = 0;
        joinleaver = 0;
        inactive = 0;
        totalKills = 0;
        totalMobKills = 0;
        ops = 0;
        ages = new ArrayList<>();
        latestLogins = new HashMap<>();
        playtimes = new HashMap<>();
        sessiondata = new ArrayList<>();        
        commandUse = new HashMap<>();
        registered = new ArrayList<>();
    }

    public void addToGmZero(long gmZero) {
        this.gmZero += gmZero;
    }

    public void addToGmOne(long gmOne) {
        this.gmOne += gmOne;
    }

    public void addToGmTwo(long gmTwo) {
        this.gmTwo += gmTwo;
    }

    public void addGmThree(long gmThree) {
        this.gmThree += gmThree;
    }

    public void addTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes += totalLoginTimes;
    }

    public void addTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime += totalPlaytime;
    }

    public void addTotalBanned(int totalBanned) {
        this.totalBanned += totalBanned;
    }

    public void addActive(int active) {
        this.active += active;
    }

    public void addJoinleaver(int joinleaver) {
        this.joinleaver += joinleaver;
    }

    public void addInactive(int inactive) {
        this.inactive += inactive;
    }

    public void addTotalKills(long totalKills) {
        this.totalKills += totalKills;
    }

    public void addTotalMobKills(long totalMobKills) {
        this.totalMobKills += totalMobKills;
    }

    public void addTotalDeaths(long totalDeaths) {
        this.totalDeaths += totalDeaths;
    }

    public void addOps(int ops) {
        this.ops += ops;
    }

    public long getGmZero() {
        return gmZero;
    }

    public long getGmOne() {
        return gmOne;
    }

    public long getGmTwo() {
        return gmTwo;
    }

    public long getGmThree() {
        return gmThree;
    }

    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    public long getTotalPlaytime() {
        return totalPlaytime;
    }

    public int getTotalBanned() {
        return totalBanned;
    }

    public int getActive() {
        return active;
    }

    public int getJoinleaver() {
        return joinleaver;
    }

    public int getInactive() {
        return inactive;
    }

    public long getTotalKills() {
        return totalKills;
    }

    public long getTotalMobKills() {
        return totalMobKills;
    }

    public long getTotalDeaths() {
        return totalDeaths;
    }

    public int getOps() {
        return ops;
    }

    public List<Integer> getAges() {
        return ages;
    }

    public HashMap<String, Long> getLatestLogins() {
        return latestLogins;
    }

    public HashMap<String, Long> getPlaytimes() {
        return playtimes;
    }

    public List<SessionData> getSessiondata() {
        return sessiondata;
    }

    public void setCommandUse(HashMap<String, Integer> commandUse) {
        this.commandUse = commandUse;
    }

    public HashMap<String, Integer> getCommandUse() {
        return commandUse;
    }

    public List<Long> getRegistered() {
        return registered;
    }
}
