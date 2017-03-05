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

    /**
     *
     */
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

    /**
     *
     * @param gmZero
     */
    public void addToGmZero(long gmZero) {
        this.gmZero += gmZero;
    }

    /**
     *
     * @param gmOne
     */
    public void addToGmOne(long gmOne) {
        this.gmOne += gmOne;
    }

    /**
     *
     * @param gmTwo
     */
    public void addToGmTwo(long gmTwo) {
        this.gmTwo += gmTwo;
    }

    /**
     *
     * @param gmThree
     */
    public void addGmThree(long gmThree) {
        this.gmThree += gmThree;
    }

    /**
     *
     * @param totalLoginTimes
     */
    public void addTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes += totalLoginTimes;
    }

    /**
     *
     * @param totalPlaytime
     */
    public void addTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime += totalPlaytime;
    }

    /**
     *
     * @param totalBanned
     */
    public void addTotalBanned(int totalBanned) {
        this.totalBanned += totalBanned;
    }

    /**
     *
     * @param active
     */
    public void addActive(int active) {
        this.active += active;
    }

    /**
     *
     * @param joinleaver
     */
    public void addJoinleaver(int joinleaver) {
        this.joinleaver += joinleaver;
    }

    /**
     *
     * @param inactive
     */
    public void addInactive(int inactive) {
        this.inactive += inactive;
    }

    /**
     *
     * @param totalKills
     */
    public void addTotalKills(long totalKills) {
        this.totalKills += totalKills;
    }

    /**
     *
     * @param totalMobKills
     */
    public void addTotalMobKills(long totalMobKills) {
        this.totalMobKills += totalMobKills;
    }

    /**
     *
     * @param totalDeaths
     */
    public void addTotalDeaths(long totalDeaths) {
        this.totalDeaths += totalDeaths;
    }

    /**
     *
     * @param ops
     */
    public void addOps(int ops) {
        this.ops += ops;
    }

    /**
     *
     * @return
     */
    public long getGmZero() {
        return gmZero;
    }

    /**
     *
     * @return
     */
    public long getGmOne() {
        return gmOne;
    }

    /**
     *
     * @return
     */
    public long getGmTwo() {
        return gmTwo;
    }

    /**
     *
     * @return
     */
    public long getGmThree() {
        return gmThree;
    }

    /**
     *
     * @return
     */
    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    /**
     *
     * @return
     */
    public long getTotalPlaytime() {
        return totalPlaytime;
    }

    /**
     *
     * @return
     */
    public int getTotalBanned() {
        return totalBanned;
    }

    /**
     *
     * @return
     */
    public int getActive() {
        return active;
    }

    /**
     *
     * @return
     */
    public int getJoinleaver() {
        return joinleaver;
    }

    /**
     *
     * @return
     */
    public int getInactive() {
        return inactive;
    }

    /**
     *
     * @return
     */
    public long getTotalKills() {
        return totalKills;
    }

    /**
     *
     * @return
     */
    public long getTotalMobKills() {
        return totalMobKills;
    }

    /**
     *
     * @return
     */
    public long getTotalDeaths() {
        return totalDeaths;
    }

    /**
     *
     * @return
     */
    public int getOps() {
        return ops;
    }

    /**
     *
     * @return
     */
    public List<Integer> getAges() {
        return ages;
    }

    /**
     *
     * @return
     */
    public HashMap<String, Long> getLatestLogins() {
        return latestLogins;
    }

    /**
     *
     * @return
     */
    public HashMap<String, Long> getPlaytimes() {
        return playtimes;
    }

    /**
     *
     * @return
     */
    public List<SessionData> getSessiondata() {
        return sessiondata;
    }

    /**
     *
     * @param commandUse
     */
    public void setCommandUse(HashMap<String, Integer> commandUse) {
        this.commandUse = commandUse;
    }

    /**
     *
     * @return
     */
    public HashMap<String, Integer> getCommandUse() {
        return commandUse;
    }

    /**
     *
     * @return
     */
    public List<Long> getRegistered() {
        return registered;
    }
}
