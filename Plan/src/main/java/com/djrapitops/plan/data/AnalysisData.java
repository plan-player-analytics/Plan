package main.java.com.djrapitops.plan.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.RecentPlayersButtonsCreator;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import main.java.com.djrapitops.plan.ui.tables.SortableCommandUseTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortablePlayersTableCreator;
import main.java.com.djrapitops.plan.utilities.analysis.Analysis;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.PlaceholderUtils;

/**
 * This class is used to store result data from Analysis at runtime.
 *
 * Most of the variables need to be set with various set methods, as they are
 * not initialized in a constructor.
 *
 * @author Rsl1122
 * @since 2.0.0
 * @see Analysis
 * @see PlaceholderUtils
 */
public class AnalysisData {

    private long refreshDate;
    private Map<String, String> additionalDataReplaceMap;

    private long averagePlayTime;
    private long totalPlayTime;
    private double averageAge;
    private String commandUseTableHtml;
    private long totalCommands;
    private String recentPlayers;
    private String sortablePlayersTable;
    private String[] playersDataArray;
    private String punchCardData;
    private String[] sessionDistributionData;
    private String[] playtimeDistributionData;
    private String[] tpsData;
    private double averageTPS;

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

    private long sessionAverage;

    private String geomapCountries;
    private String geomapZ;
    private String geomapCodes;

    private int avgUniqJoins;
    private int avgUniqJoinsDay;
    private int avgUniqJoinsWeek;
    private int avgUniqJoinsMonth;

    private int uniqueJoinsDay;
    private int uniqueJoinsWeek;
    private int uniqueJoinsMonth;

    /**
     * Class constructor.
     *
     * All data has to be set with setters to avoid NPEs.
     */
    public AnalysisData() {
        avgUniqJoins = 0;
        avgUniqJoinsDay = 0;
        avgUniqJoinsWeek = 0;
        avgUniqJoinsMonth = 0;
        uniqueJoinsDay = 0;
        uniqueJoinsWeek = 0;
        uniqueJoinsMonth = 0;
        averageTPS = 0;
        sortablePlayersTable = Html.ERROR_NOT_SET + "";
        commandUseTableHtml = Html.ERROR_NOT_SET + "";
        recentPlayers = Html.ERROR_NOT_SET + "";
        geomapCountries = Html.ERROR_NOT_SET + "";
        geomapZ = Html.ERROR_NOT_SET + "";
        geomapCodes = Html.ERROR_NOT_SET + "";
        punchCardData = "[]";
        sessionDistributionData = new String[]{"[]", "[]"};
        playtimeDistributionData = new String[]{"[]", "[]"};
        tpsData = new String[]{"[]", "[]", "[]"};
        playersDataArray = new String[]{"[0]", "[\"No data\"]", "[0]", "[\"No data\"]", "[0]", "[\"No data\"]"};
        additionalDataReplaceMap = new HashMap<>();
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
        final AnalysisData other = (AnalysisData) obj;
        if (this.averagePlayTime != other.averagePlayTime) {
            return false;
        }
        if (this.totalPlayTime != other.totalPlayTime) {
            return false;
        }
        if (Double.doubleToLongBits(this.averageAge) != Double.doubleToLongBits(other.averageAge)) {
            return false;
        }
        if (this.totalCommands != other.totalCommands) {
            return false;
        }
        if (this.newPlayersMonth != other.newPlayersMonth) {
            return false;
        }
        if (this.newPlayersWeek != other.newPlayersWeek) {
            return false;
        }
        if (this.newPlayersDay != other.newPlayersDay) {
            return false;
        }
        if (Double.doubleToLongBits(this.gm0Perc) != Double.doubleToLongBits(other.gm0Perc)) {
            return false;
        }
        if (Double.doubleToLongBits(this.gm1Perc) != Double.doubleToLongBits(other.gm1Perc)) {
            return false;
        }
        if (Double.doubleToLongBits(this.gm2Perc) != Double.doubleToLongBits(other.gm2Perc)) {
            return false;
        }
        if (Double.doubleToLongBits(this.gm3Perc) != Double.doubleToLongBits(other.gm3Perc)) {
            return false;
        }
        if (this.banned != other.banned) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (this.inactive != other.inactive) {
            return false;
        }
        if (this.joinleaver != other.joinleaver) {
            return false;
        }
        if (this.total != other.total) {
            return false;
        }
        if (this.totalPlayers != other.totalPlayers) {
            return false;
        }
        if (this.totalLoginTimes != other.totalLoginTimes) {
            return false;
        }
        if (this.ops != other.ops) {
            return false;
        }
        if (this.totalkills != other.totalkills) {
            return false;
        }
        if (this.totalmobkills != other.totalmobkills) {
            return false;
        }
        if (this.totaldeaths != other.totaldeaths) {
            return false;
        }
        if (this.sessionAverage != other.sessionAverage) {
            return false;
        }
        if (!Objects.equals(this.commandUseTableHtml, other.commandUseTableHtml)) {
            return false;
        }
        if (!Objects.equals(this.recentPlayers, other.recentPlayers)) {
            return false;
        }
        if (!Objects.equals(this.sortablePlayersTable, other.sortablePlayersTable)) {
            return false;
        }
        if (!Objects.equals(this.geomapCountries, other.geomapCountries)) {
            return false;
        }
        if (!Objects.equals(this.geomapZ, other.geomapZ)) {
            return false;
        }
        if (!Objects.equals(this.geomapCodes, other.geomapCodes)) {
            return false;
        }
        if (!Arrays.deepEquals(this.playersDataArray, other.playersDataArray)) {
            return false;
        }
        return true;
    }

    /**
     * Set the additional data replaceMap used with Analysis replacerules.
     *
     * @param additionalDataReplaceMap Map with placeholder keys %key%, value
     * @see PlaceholderUtils
     */
    public void setAdditionalDataReplaceMap(Map<String, String> additionalDataReplaceMap) {
        this.additionalDataReplaceMap = additionalDataReplaceMap;
    }

    /**
     * Get the additional data replaceMap used with Analysis replacerules.
     *
     * @return a Map with placeholder keys %key%, value
     */
    public Map<String, String> getAdditionalDataReplaceMap() {
        return additionalDataReplaceMap;
    }

    /**
     * Used to get the toString representation of a String[] containing all
     * countries on the Plotly.js Chloropleth map.
     *
     * @return ["Finland","Sweden","Etc.."]
     */
    public String getGeomapCountries() {
        return geomapCountries;
    }

    /**
     * Used to set the toString representation of a String[] containing all
     * countries on the Plotly.js Chloropleth map.
     *
     * Incorrect value will break the Chloropleth map on analysis.html page.
     *
     * @param geomapCountries ["Finland","Sweden","Etc.."]
     */
    public void setGeomapCountries(String geomapCountries) {
        this.geomapCountries = geomapCountries;
    }

    /**
     * Used to get the toString representation of a int[] containing all player
     * amounts on the Plotly.js Chloropleth map.
     *
     * Must contain same amount of numbers as countries in GeomapCountries.
     *
     * @return [0,0,0,3,0,Etc..]
     */
    public String getGeomapZ() {
        return geomapZ;
    }

    /**
     * Used to set the toString representation of a int[] containing all player
     * amounts on the Plotly.js Chloropleth map.
     *
     * Must contain same amount of numbers as countries in GeomapCountries.
     * Incorrect amount will break the Chloropleth map on analysis.html page.
     *
     * @param geomapZ [0,0,0,3,0,Etc..]
     */
    public void setGeomapZ(String geomapZ) {
        this.geomapZ = geomapZ;
    }

    /**
     * Used to get the toString representation of a String[] containing all
     * country codes on the Plotly.js Chloropleth map.
     *
     * Must contain same amount of numbers as countries in GeomapCountries.
     *
     * @return ["PNG","KHM","KAZ","PRY","SYR","SLB","MLI","Etc.."]
     */
    public String getGeomapCodes() {
        return geomapCodes;
    }

    /**
     * Used to set the toString representation of a String[] containing all
     * country codes on the Plotly.js Chloropleth map.
     *
     * Must contain same amount of numbers as countries in GeomapCountries.
     *
     * @param geomapCodes ["PNG","KHM","KAZ","PRY","SYR","SLB","MLI","Etc.."]
     */
    public void setGeomapCodes(String geomapCodes) {
        this.geomapCodes = geomapCodes;
    }

    /**
     * Used to get the html for players table.
     *
     * @return Html string.
     * @see SortablePlayersTableCreator
     */
    public String getSortablePlayersTable() {
        return sortablePlayersTable;
    }

    /**
     * Used to set the html for players table.
     *
     * @param sortablePlayersTable Html string.
     * @see SortablePlayersTableCreator
     */
    public void setSortablePlayersTable(String sortablePlayersTable) {
        this.sortablePlayersTable = sortablePlayersTable;
    }

    /**
     * Used to get the amount of players who have joined only once.
     *
     * @return Number from 0 to Integer.MAX
     */
    public int getJoinleaver() {
        return joinleaver;
    }

    /**
     * Used to set the amount of players who have joined only once.
     *
     * No check for correct value.
     *
     * @param joinleaver Number from 0 to Integer.MAX
     */
    public void setJoinleaver(int joinleaver) {
        this.joinleaver = joinleaver;
    }

    /**
     * Used to get the html for the commands table.
     *
     * @return Html string.
     * @see SortableCommandUseTableCreator
     */
    public String getCommandUseListHtml() {
        return commandUseTableHtml;
    }

    /**
     * Used to get the html for the commands table.
     *
     * @param commandsTableHtml Html string.
     * @see SortableCommandUseTableCreator
     */
    public void setCommandUseTableHtml(String commandsTableHtml) {
        this.commandUseTableHtml = commandsTableHtml;
    }

    /**
     * Used to get the amount of banned players.
     *
     * @return 0 to Integer.MAX
     */
    public int getBanned() {
        return banned;
    }

    /**
     * Used to set the amount of banned players.
     *
     * No check for correct value.
     *
     * @param banned 0 to Integer.MAX
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
     * @see AnalysisUtils
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
     * @see AnalysisUtils
     */
    public void setActive(int active) {
        this.active = active;
    }

    /**
     * Set the amount of inactive players.
     *
     * Activity is determined by AnalysisUtils.isActive()
     *
     * @return Amount of inactive players
     * @see AnalysisUtils
     */
    public int getInactive() {
        return inactive;
    }

    /**
     * Set the amount of inactive players.
     *
     * Activity is determined by AnalysisUtils.isActive()
     *
     * @param inactive Amount of inactive players
     * @see AnalysisUtils
     */
    public void setInactive(int inactive) {
        this.inactive = inactive;
    }

    /**
     * Get the total amount of players used to calculate activity.
     *
     * @return 0 to Integer.MAX
     */
    public int getTotal() {
        return total;
    }

    /**
     * Set the total amount of players used to calculate activity.
     *
     * No check for correct value.
     *
     * @param total 0 to Integer.MAX
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * Get percentage of Gamemode usage time as a whole.
     *
     * @return 0.0 to 1.0
     */
    public double getGm0Perc() {
        return gm0Perc;
    }

    /**
     * Set percentage of Gamemode usage time as a whole.
     *
     * No check for correct value.
     *
     * @param gm0Perc 0.0 to 1.0
     */
    public void setGm0Perc(double gm0Perc) {
        this.gm0Perc = gm0Perc;
    }

    /**
     * Get percentage of Gamemode usage time as a whole.
     *
     * @return 0.0 to 1.0
     */
    public double getGm1Perc() {
        return gm1Perc;
    }

    /**
     * Set percentage of Gamemode usage time as a whole.
     *
     * No check for correct value.
     *
     * @param gm1Perc 0.0 to 1.0
     */
    public void setGm1Perc(double gm1Perc) {
        this.gm1Perc = gm1Perc;
    }

    /**
     * Get percentage of Gamemode usage time as a whole.
     *
     * @return 0.0 to 1.0
     */
    public double getGm2Perc() {
        return gm2Perc;
    }

    /**
     * Set percentage of Gamemode usage time as a whole.
     *
     * No check for correct value.
     *
     * @param gm2Perc 0.0 to 1.0
     */
    public void setGm2Perc(double gm2Perc) {
        this.gm2Perc = gm2Perc;
    }

    /**
     * Get percentage of Gamemode usage time as a whole.
     *
     * @return 0.0 to 1.0
     */
    public double getGm3Perc() {
        return gm3Perc;
    }

    /**
     * Set percentage of Gamemode usage time as a whole.
     *
     * No check for correct value.
     *
     * @param gm3Perc 0.0 to 1.0
     */
    public void setGm3Perc(double gm3Perc) {
        this.gm3Perc = gm3Perc;
    }

    /**
     * Get percentage of Gamemode usage time as a whole.
     *
     * @return 0.0 to 1.0
     */
    public int getTotalPlayers() {
        return totalPlayers;
    }

    /**
     * Get the Total number of players according to bukkit's data.
     *
     * @param totalPlayers 0 to Integer.MAX
     */
    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    /**
     * Get how long time has been played, long in ms.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    /**
     * Set how long time has been played, long in ms.
     *
     * No check for correct value.
     *
     * @param totalPlayTime 0 to Long.MAX
     */
    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    /**
     * Retrieve the refresh Epoch millisecond this object's data was calculated.
     *
     * @return the refresh Epoch millisecond.
     */
    public long getRefreshDate() {
        return refreshDate;
    }

    /**
     * Get How long players have played on average.
     *
     * @return long in ms.
     */
    public long getAveragePlayTime() {
        return averagePlayTime;
    }

    /**
     * Get the average age of the players whose age has been gathered.
     *
     * -1 if none have been gathered.
     *
     * @return -1 or from 1.0 to 99.0
     */
    public double getAverageAge() {
        return averageAge;
    }

    /**
     * Get How many times players have joined in total.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    /**
     * Get How many operators are on the server.
     *
     * @return 0 to Integer.MAX
     */
    public int getOps() {
        return ops;
    }

    /**
     * Set the refresh Epoch millisecond this object's data was calculated.
     *
     * @param refreshDate Epoch millisecond.
     */
    public void setRefreshDate(long refreshDate) {
        this.refreshDate = refreshDate;
    }

    /**
     * Set the average playtime of all players.
     *
     * @param averagePlayTime long in ms.
     */
    public void setAveragePlayTime(long averagePlayTime) {
        this.averagePlayTime = averagePlayTime;
    }

    /**
     * Set the average age of the players whose age has been gathered.
     *
     * No check for correct value.
     *
     * @param averageAge 1.0 to 99.0 or -1 if none have been gathered.
     */
    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    /**
     * Set How many times playes have logged in.
     *
     * @param totalLoginTimes 0 to Long.MAX
     */
    public void setTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes = totalLoginTimes;
    }

    /**
     * Set the amount of operators.
     *
     * @param ops 0 to Integer.MAX
     */
    public void setOps(int ops) {
        this.ops = ops;
    }

    /**
     * Get the html for Recent player buttons.
     *
     * @return html string.
     * @see RecentPlayersButtonsCreator
     */
    public String getRecentPlayers() {
        return recentPlayers;
    }

    /**
     * Set the html for Recent player buttons.
     *
     * @param recentPlayers html string.
     * @see RecentPlayersButtonsCreator
     */
    public void setRecentPlayers(String recentPlayers) {
        this.recentPlayers = recentPlayers;
    }

    /**
     * Get the amount of registered players in last 30 days.
     *
     * @return 0 to Integer.MAX
     */
    public int getNewPlayersMonth() {
        return newPlayersMonth;
    }

    /**
     * Set the amount of registered players in last 30 days.
     *
     * No check for correct value.
     *
     * @param newPlayersMonth 0 to Integer.MAX
     */
    public void setNewPlayersMonth(int newPlayersMonth) {
        this.newPlayersMonth = newPlayersMonth;
    }

    /**
     * Get the amount of registered players in last 7 days.
     *
     * @return 0 to Integer.MAX
     */
    public int getNewPlayersWeek() {
        return newPlayersWeek;
    }

    /**
     * Set the amount of registered players in last 7 days.
     *
     * No check for correct value.
     *
     * @param newPlayersWeek 0 to Integer.MAX
     */
    public void setNewPlayersWeek(int newPlayersWeek) {
        this.newPlayersWeek = newPlayersWeek;
    }

    /**
     * Get the amount of registered players in last 24 hours.
     *
     * @return 0 to Integer.MAX
     */
    public int getNewPlayersDay() {
        return newPlayersDay;
    }

    /**
     * Set the amount of registered players in last 24 hours.
     *
     * No check for correct value.
     *
     * @param newPlayersDay 0 to Integer.MAX
     */
    public void setNewPlayersDay(int newPlayersDay) {
        this.newPlayersDay = newPlayersDay;
    }

    /**
     * Get the amount of times players have killed each other.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalPlayerKills() {
        return totalkills;
    }

    /**
     * Get the amount of mob kills the players have.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalMobKills() {
        return totalmobkills;
    }

    /**
     * Get how many times the playes have died.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalDeaths() {
        return totaldeaths;
    }

    /**
     * Set the amount of times players have killed each other.
     *
     * No check for correct value.
     *
     * @param totalkills 0 to Long.MAX
     */
    public void setTotalkills(long totalkills) {
        this.totalkills = totalkills;
    }

    /**
     * Set the amount of mob kills the players have.
     *
     * No check for correct value.
     *
     * @param totalmobkills 0 to Long.MAX
     */
    public void setTotalmobkills(long totalmobkills) {
        this.totalmobkills = totalmobkills;
    }

    /**
     * Set how many times the playes have died.
     *
     * No check for correct value.
     *
     * @param totaldeaths 0 to Long.MAX
     */
    public void setTotaldeaths(long totaldeaths) {
        this.totaldeaths = totaldeaths;
    }

    /**
     * Used to store all arrays created in
     * Analysis#createPlayerActivityGraphs().
     *
     * 0, 2, 4 contain data. 1, 3, 5 contain labels.
     *
     * 0, 1 day; 2, 3 week; 4, 5 month
     *
     * @return String array containing multiple toString representations of
     * number and label arrays.
     * @see PlayerActivityGraphCreator
     * @see Analysis
     */
    public String[] getPlayersDataArray() {
        return playersDataArray;
    }

    /**
     * Used to store all arrays created in
     * Analysis#createPlayerActivityGraphs().
     *
     * 0, 2, 4 contain data. 1, 3, 5 contain labels.
     *
     * 0, 1 day; 2, 3 week; 4, 5 month
     *
     * @param playersDataArray String array containing multiple toString
     * representations of number and label arrays.
     * @see PlayerActivityGraphCreator
     * @see Analysis
     */
    public void setPlayersDataArray(String[] playersDataArray) {
        this.playersDataArray = playersDataArray;
    }

    /**
     * Set the total number of unique commands.
     *
     * No check for correct value.
     *
     * @param totalCommands 0 to Long.MAX
     */
    public void setTotalCommands(long totalCommands) {
        this.totalCommands = totalCommands;
    }

    /**
     * Get the total number of unique commands.
     *
     * @return 0 to Long.MAX
     */
    public long getTotalCommands() {
        return totalCommands;
    }

    /**
     * Get the average length of every session on the server.
     *
     * @return long in ms.
     */
    public long getSessionAverage() {
        return sessionAverage;
    }

    /**
     * Set the average length of every session on the server.
     *
     * @param sessionAverage 0 to Long.MAX
     */
    public void setSessionAverage(long sessionAverage) {
        this.sessionAverage = sessionAverage;
    }

    /**
     * Get the data for the Session Punchcard.
     *
     * @return Array of x y r coordinates: [{x: 4, y: 5, r: 4}]
     */
    public String getPunchCardData() {
        return punchCardData;
    }

    /**
     * Set the data for the Session Punchcard.
     *
     * @param punchCardData Array of x y r coordinates: [{x: 4, y: 5, r: 4}]
     */
    public void setPunchCardData(String punchCardData) {
        this.punchCardData = punchCardData;
    }

    /**
     * Get the data and labels for the session distribution barchart.
     *
     * @return index 0: [0, 5, 4], 1: ["0-5", "5-10", "10-15"]
     */
    public String[] getSessionDistributionData() {
        return sessionDistributionData;
    }

    /**
     * Set the data and labels for the session distribution barchart.
     *
     * @param sessionDistributionData index 0: [0, 5, 4], 1: ["0-5", "5-10",
     * "10-15"]
     */
    public void setSessionDistributionData(String[] sessionDistributionData) {
        this.sessionDistributionData = sessionDistributionData;
    }

    /**
     * Get the data and labels for the playtime distribution barchart.
     *
     * @return index 0: [0, 5, 4], 1: ["0-5", "5-10", "10-15"]
     */
    public String[] getPlaytimeDistributionData() {
        return playtimeDistributionData;
    }

    /**
     * Set the data and labels for the playtime distribution barchart.
     *
     * @param playtimeDistributionData index 0: [0, 5, 4], 1: ["0-5", "5-10",
     * "10-15"]
     */
    public void setPlaytimeDistributionData(String[] playtimeDistributionData) {
        this.playtimeDistributionData = playtimeDistributionData;
    }

    /**
     *
     * @return
     */
    public int getAvgUniqJoins() {
        return avgUniqJoins;
    }

    /**
     *
     * @return
     */
    public int getAvgUniqJoinsDay() {
        return avgUniqJoinsDay;
    }

    /**
     *
     * @return
     */
    public int getAvgUniqJoinsWeek() {
        return avgUniqJoinsWeek;
    }

    /**
     *
     * @return
     */
    public int getAvgUniqJoinsMonth() {
        return avgUniqJoinsMonth;
    }

    /**
     *
     * @param avgUniqJoins
     */
    public void setAvgUniqJoins(int avgUniqJoins) {
        this.avgUniqJoins = avgUniqJoins;
    }

    /**
     *
     * @param avgUniqJoinsDay
     */
    public void setAvgUniqJoinsDay(int avgUniqJoinsDay) {
        this.avgUniqJoinsDay = avgUniqJoinsDay;
    }

    /**
     *
     * @param avgUniqJoinsWeek
     */
    public void setAvgUniqJoinsWeek(int avgUniqJoinsWeek) {
        this.avgUniqJoinsWeek = avgUniqJoinsWeek;
    }

    /**
     *
     * @param avgUniqJoinsMonth
     */
    public void setAvgUniqJoinsMonth(int avgUniqJoinsMonth) {
        this.avgUniqJoinsMonth = avgUniqJoinsMonth;
    }

    /**
     *
     * @return
     */
    public int getUniqueJoinsDay() {
        return uniqueJoinsDay;
    }

    /**
     *
     * @param uniqueJoinsDay
     */
    public void setUniqueJoinsDay(int uniqueJoinsDay) {
        this.uniqueJoinsDay = uniqueJoinsDay;
    }

    /**
     *
     * @return
     */
    public int getUniqueJoinsWeek() {
        return uniqueJoinsWeek;
    }

    /**
     *
     * @param uniqueJoinsWeek
     */
    public void setUniqueJoinsWeek(int uniqueJoinsWeek) {
        this.uniqueJoinsWeek = uniqueJoinsWeek;
    }

    /**
     *
     * @return
     */
    public int getUniqueJoinsMonth() {
        return uniqueJoinsMonth;
    }

    /**
     *
     * @param uniqueJoinsMonth
     */
    public void setUniqueJoinsMonth(int uniqueJoinsMonth) {
        this.uniqueJoinsMonth = uniqueJoinsMonth;
    }

    public String[] getTpsData() {
        return tpsData;
    }

    public void setTpsData(String[] tpsData) {
        this.tpsData = tpsData;
    }

    public double getAverageTPS() {
        return averageTPS;
    }

    public void setAverageTPS(double averageTPS) {
        this.averageTPS = averageTPS;
    }
}
