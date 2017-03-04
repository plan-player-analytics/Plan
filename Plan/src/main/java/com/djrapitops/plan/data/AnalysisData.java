package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.ui.Html;

/**
 *
 * @author Rsl1122
 */
public class AnalysisData {

    private long refreshDate;

    private long averagePlayTime;
    private long totalPlayTime;
    private double averageAge;
    private String commandUseTableHtml;
    private long totalCommands;
    private String top20ActivePlayers;
    private String recentPlayers;
    private String sortablePlayersTable;
    private String[] playersDataArray;

    private int newPlayersMonth;
    private int newPlayersWeek;
    private int newPlayersDay;

    private double gm0Perc;
    private double gm1Perc;
    private double gm2Perc;
    private double gm3Perc;

    private int banned;
    private int active;
    private int inactive;
    private int joinleaver;
    private int total;

    private int totalPlayers;
    private long totalLoginTimes;
    private int ops;

    private long totalkills;
    private long totalmobkills;
    private long totaldeaths;

    /**
     * Class constructor.
     *
     * All data has to be set with setters to avoid NPE.
     */
    public AnalysisData() {
        sortablePlayersTable = Html.ERROR_NOT_SET+"";
        commandUseTableHtml = Html.ERROR_NOT_SET+"";
        top20ActivePlayers = Html.ERROR_NOT_SET+"";
        recentPlayers = Html.ERROR_NOT_SET+"";
        playersDataArray = new String[]{"[0]","[\"No data\"]","[0]","[\"No data\"]","[0]","[\"No data\"]"};
    }

    // Getters and setters v---------------------------------v
    public String getSortablePlayersTable() {
        return sortablePlayersTable;
    }

    public void setSortablePlayersTable(String sortablePlayersTable) {
        this.sortablePlayersTable = sortablePlayersTable;
    }

    /**
     * @return The Amount of players who have joined only once
     */
    public int getJoinleaver() {
        return joinleaver;
    }

    /**
     * @param joinleaver The Amount of players who have joined only once
     */
    public void setJoinleaver(int joinleaver) {
        this.joinleaver = joinleaver;
    }
    
    /**
     * @return HTML String of the Top50CommandsList
     */
    public String getTop50CommandsListHtml() {
        return commandUseTableHtml;
    }

    /**
     * @param top50CommandsListHtml HTML String of the Top50CommandsList
     */
    public void setCommandUseTableHtml(String top50CommandsListHtml) {
        this.commandUseTableHtml = top50CommandsListHtml;
    }

    /**
     * @return Amount of banned players
     */
    public int getBanned() {
        return banned;
    }

    /**
     * @param banned Amount of banned players
     */
    public void setBanned(int banned) {
        this.banned = banned;
    }

    /**
     * Retrieve the amount of active players.
     *
     * Activity is determined by AnalysisUtils.isActive()
     *
     * @return Amount of active players
     */
    public int getActive() {
        return active;
    }

    /**
     * Set the amount of active players.
     *
     * Activity is determined by AnalysisUtils.isActive()
     *
     * @param active Amount of active players
     */
    public void setActive(int active) {
        this.active = active;
    }

    /**
     * @return Amount of inactive players
     */
    public int getInactive() {
        return inactive;
    }

    /**
     * @param inactive Amount of inactive players
     */
    public void setInactive(int inactive) {
        this.inactive = inactive;
    }

    /**
     * @return Total Amount of players used to calculate activity
     */
    public int getTotal() {
        return total;
    }

    /**
     * @param total Total Amount of players used to calculate activity
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * @return Percentage of Gamemode usage time as a whole
     */
    public double getGm0Perc() {
        return gm0Perc;
    }

    /**
     * @param gm0Perc Percentage of Gamemode usage time as a whole
     */
    public void setGm0Perc(double gm0Perc) {
        this.gm0Perc = gm0Perc;
    }

    /**
     * @return Percentage of Gamemode usage time as a whole
     */
    public double getGm1Perc() {
        return gm1Perc;
    }

    /**
     * @param gm1Perc Percentage of Gamemode usage time as a whole
     */
    public void setGm1Perc(double gm1Perc) {
        this.gm1Perc = gm1Perc;
    }

    /**
     * @return Percentage of Gamemode usage time as a whole
     */
    public double getGm2Perc() {
        return gm2Perc;
    }

    /**
     * @param gm2Perc Percentage of Gamemode usage time as a whole
     */
    public void setGm2Perc(double gm2Perc) {
        this.gm2Perc = gm2Perc;
    }

    /**
     * @return Percentage of Gamemode usage time as a whole
     */
    public double getGm3Perc() {
        return gm3Perc;
    }

    /**
     * @param gm3Perc Percentage of Gamemode usage time as a whole
     */
    public void setGm3Perc(double gm3Perc) {
        this.gm3Perc = gm3Perc;
    }

    /**
     * @return Total number of players according to bukkit's data.
     */
    public int getTotalPlayers() {
        return totalPlayers;
    }

    /**
     * @param totalPlayers Total number of players according to bukkit's data.
     */
    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    /**
     * @return How long has been played, long in ms.
     */
    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    /**
     * @param totalPlayTime How long has been played, long in ms.
     */
    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    /**
     * @return Last Analysis Refresh, long in ms.
     */
    public long getRefreshDate() {
        return refreshDate;
    }

    /**
     * @return How long has been played on average, long in ms.
     */
    public long getAveragePlayTime() {
        return averagePlayTime;
    }

    /**
     * @return Average age of the players whose age has been gathered.
     */
    public double getAverageAge() {
        return averageAge;
    }

    /**
     * @return How many times players have joined.
     */
    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    /**
     * @return How many operators are on the server.
     */
    public int getOps() {
        return ops;
    }

    /**
     * @param refreshDate Last Analysis Refresh, long in ms.
     */
    public void setRefreshDate(long refreshDate) {
        this.refreshDate = refreshDate;
    }

    /**
     * @param averagePlayTime long in ms.
     */
    public void setAveragePlayTime(long averagePlayTime) {
        this.averagePlayTime = averagePlayTime;
    }

    /**
     * @param averageAge Average age of the players whose age has been gathered.
     */
    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    /**
     * @param totalLoginTimes How many times playes have logged in
     */
    public void setTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes = totalLoginTimes;
    }

    /**
     * @param ops Amount of operators.
     */
    public void setOps(int ops) {
        this.ops = ops;
    }

    public String getTop20ActivePlayers() {
        return top20ActivePlayers;
    }

    public void setTop20ActivePlayers(String top20ActivePlayers) {
        this.top20ActivePlayers = top20ActivePlayers;
    }

    public String getRecentPlayers() {
        return recentPlayers;
    }

    public void setRecentPlayers(String recentPlayers) {
        this.recentPlayers = recentPlayers;
    }

    public int getNewPlayersMonth() {
        return newPlayersMonth;
    }

    public void setNewPlayersMonth(int newPlayersMonth) {
        this.newPlayersMonth = newPlayersMonth;
    }

    public int getNewPlayersWeek() {
        return newPlayersWeek;
    }

    public void setNewPlayersWeek(int newPlayersWeek) {
        this.newPlayersWeek = newPlayersWeek;
    }

    public int getNewPlayersDay() {
        return newPlayersDay;
    }

    public void setNewPlayersDay(int newPlayersDay) {
        this.newPlayersDay = newPlayersDay;
    }

    public long getTotalkills() {
        return totalkills;
    }

    public long getTotalmobkills() {
        return totalmobkills;
    }

    public long getTotaldeaths() {
        return totaldeaths;
    }

    public void setTotalkills(long totalkills) {
        this.totalkills = totalkills;
    }

    public void setTotalmobkills(long totalmobkills) {
        this.totalmobkills = totalmobkills;
    }

    public void setTotaldeaths(long totaldeaths) {
        this.totaldeaths = totaldeaths;
    }

    public String[] getPlayersDataArray() {
        return playersDataArray;
    }

    public void setPlayersDataArray(String[] playersDataArray) {
        this.playersDataArray = playersDataArray;
    }

    public void setTotalCommands(long totalCommands) {
        this.totalCommands = totalCommands;
    }

    public long getTotalCommands() {
        return totalCommands;
    }
}
