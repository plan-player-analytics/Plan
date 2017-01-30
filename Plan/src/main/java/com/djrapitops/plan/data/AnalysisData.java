package com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.data.PlanLiteAnalyzedData;

/**
 *
 * @author Rsl1122
 */
public class AnalysisData {

    private long refreshDate;

    private long averagePlayTime;
    private long totalPlayTime;
    private double averageAge;
    private String gmTimesChartImgHtml;
    private String playersChartImgHtmlMonth;
    private String playersChartImgHtmlWeek;
    private String playersChartImgHtmlDay;
    private String activityChartImgHtml;
    private String top50CommandsListHtml;
    private String top20ActivePlayers;
    private String recentPlayers;

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

    private boolean planLiteEnabled;
    private PlanLiteAnalyzedData planLiteData;

    /**
     * Class constructor.
     * 
     * All data has to be set with setters to avoid NPE.
     */
    public AnalysisData() {
    }

    // Getters and setters v---------------------------------v
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
     * @return true if PlanLite was enabled at the time of Analysis
     */
    public boolean isPlanLiteEnabled() {
        return planLiteEnabled;
    }

    /**
     * @param planLiteEnabled true if PlanLite was enabled at the time of Analysis
     */
    public void setPlanLiteEnabled(boolean planLiteEnabled) {
        this.planLiteEnabled = planLiteEnabled;
    }

    /**
     * Retrieve the PlanLiteAnalyzedData.
     * 
     * null if planLiteEnabled = false
     * @return Seperate object used to save PlanLiteData
     */
    public PlanLiteAnalyzedData getPlanLiteData() {
        return planLiteData;
    }

    /**
     * Set the PlanLiteAnalyzedData.
     * @param planLiteData Seperate object used to save PlanLiteData
     */
    public void setPlanLiteData(PlanLiteAnalyzedData planLiteData) {
        this.planLiteData = planLiteData;
    }

    /**
     * @return HTML String of the Month Activity graph
     */
    public String getPlayersChartImgHtmlMonth() {
        return playersChartImgHtmlMonth;
    }

    /**
     * @param playersChartImgHtmlMonth HTML String of the Month Activity graph
     */
    public void setPlayersChartImgHtmlMonth(String playersChartImgHtmlMonth) {
        this.playersChartImgHtmlMonth = playersChartImgHtmlMonth;
    }

    /**
     * @return HTML String of the Week Activity graph
     */
    public String getPlayersChartImgHtmlWeek() {
        return playersChartImgHtmlWeek;
    }

    /**
     * @param playersChartImgHtmlWeek HTML String of the Week Activity graph
     */
    public void setPlayersChartImgHtmlWeek(String playersChartImgHtmlWeek) {
        this.playersChartImgHtmlWeek = playersChartImgHtmlWeek;
    }

    /**
     * @return HTML String of the Day Activity graph
     */
    public String getPlayersChartImgHtmlDay() {
        return playersChartImgHtmlDay;
    }

    /**
     * @param playersChartImgHtmlDay HTML String of the Day Activity graph
     */
    public void setPlayersChartImgHtmlDay(String playersChartImgHtmlDay) {
        this.playersChartImgHtmlDay = playersChartImgHtmlDay;
    }

    /**
     * @return HTML String of the Top50CommandsList
     */
    public String getTop50CommandsListHtml() {
        return top50CommandsListHtml;
    }

    /**
     * @param top50CommandsListHtml HTML String of the Top50CommandsList
     */
    public void setTop50CommandsListHtml(String top50CommandsListHtml) {
        this.top50CommandsListHtml = top50CommandsListHtml;
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
     * @return HTML String of the GMTimes Piechart
     */
    public String getGmTimesChartImgHtml() {
        return gmTimesChartImgHtml;
    }

    /**
     * @return HTML String of the Activity Piechart
     */
    public String getActivityChartImgHtml() {
        return activityChartImgHtml;
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
     * @param gmTimesChartImgHtml HTML String of the GMTimes Piechart
     */
    public void setGmTimesChartImgHtml(String gmTimesChartImgHtml) {
        this.gmTimesChartImgHtml = gmTimesChartImgHtml;
    }

    /**
     * @param activityChartImgHtml HTML String of the Activity Piechart
     */
    public void setActivityChartImgHtml(String activityChartImgHtml) {
        this.activityChartImgHtml = activityChartImgHtml;
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
    
    

}
