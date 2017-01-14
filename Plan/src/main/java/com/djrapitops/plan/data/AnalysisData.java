package com.djrapitops.plan.data;

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

    private double gm0Perc;
    private double gm1Perc;
    private double gm2Perc;
    private double gm3Perc;

    private int banned;
    private int active;
    private int inactive;
    private int total;

    private int totalPlayers;
    private long totalLoginTimes;
    private int ops;

    public AnalysisData() {
    }

    // Getters and setters v---------------------------------v
    public String getPlayersChartImgHtmlMonth() {
        return playersChartImgHtmlMonth;
    }

    public void setPlayersChartImgHtmlMonth(String playersChartImgHtmlMonth) {
        this.playersChartImgHtmlMonth = playersChartImgHtmlMonth;
    }

    public String getPlayersChartImgHtmlWeek() {
        return playersChartImgHtmlWeek;
    }

    public void setPlayersChartImgHtmlWeek(String playersChartImgHtmlWeek) {
        this.playersChartImgHtmlWeek = playersChartImgHtmlWeek;
    }

    public String getPlayersChartImgHtmlDay() {
        return playersChartImgHtmlDay;
    }

    public void setPlayersChartImgHtmlDay(String playersChartImgHtmlDay) {
        this.playersChartImgHtmlDay = playersChartImgHtmlDay;
    }

    public String getTop50CommandsListHtml() {
        return top50CommandsListHtml;
    }

    public void setTop50CommandsListHtml(String top50CommandsListHtml) {
        this.top50CommandsListHtml = top50CommandsListHtml;
    }

    public int getBanned() {
        return banned;
    }

    public void setBanned(int banned) {
        this.banned = banned;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getInactive() {
        return inactive;
    }

    public void setInactive(int inactive) {
        this.inactive = inactive;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public double getGm0Perc() {
        return gm0Perc;
    }

    public void setGm0Perc(double gm0Perc) {
        this.gm0Perc = gm0Perc;
    }

    public double getGm1Perc() {
        return gm1Perc;
    }

    public void setGm1Perc(double gm1Perc) {
        this.gm1Perc = gm1Perc;
    }

    public double getGm2Perc() {
        return gm2Perc;
    }

    public void setGm2Perc(double gm2Perc) {
        this.gm2Perc = gm2Perc;
    }

    public double getGm3Perc() {
        return gm3Perc;
    }

    public void setGm3Perc(double gm3Perc) {
        this.gm3Perc = gm3Perc;
    }

    public int getTotalPlayers() {
        return totalPlayers;
    }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public long getRefreshDate() {
        return refreshDate;
    }

    public long getAveragePlayTime() {
        return averagePlayTime;
    }

    public double getAverageAge() {
        return averageAge;
    }

    public String getGmTimesChartImgHtml() {
        return gmTimesChartImgHtml;
    }

    public String getActivityChartImgHtml() {
        return activityChartImgHtml;
    }

    public long getTotalLoginTimes() {
        return totalLoginTimes;
    }

    public int getOps() {
        return ops;
    }

    public void setRefreshDate(long refreshDate) {
        this.refreshDate = refreshDate;
    }

    public void setAveragePlayTime(long averagePlayTime) {
        this.averagePlayTime = averagePlayTime;
    }

    public void setAverageAge(double averageAge) {
        this.averageAge = averageAge;
    }

    public void setGmTimesChartImgHtml(String gmTimesChartImgHtml) {
        this.gmTimesChartImgHtml = gmTimesChartImgHtml;
    }

    public void setActivityChartImgHtml(String activityChartImgHtml) {
        this.activityChartImgHtml = activityChartImgHtml;
    }

    public void setTotalLoginTimes(long totalLoginTimes) {
        this.totalLoginTimes = totalLoginTimes;
    }

    public void setOps(int ops) {
        this.ops = ops;
    }

}
