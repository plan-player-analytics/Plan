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
    private String playersChartImgHtml;
    private String activityChartImgHtml;

    private int gm0Perc;
    private int gm1Perc;
    private int gm2Perc;
    private int gm3Perc;

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

    public int getGm0Perc() {
        return gm0Perc;
    }

    public void setGm0Perc(int gm0Perc) {
        this.gm0Perc = gm0Perc;
    }

    public int getGm1Perc() {
        return gm1Perc;
    }

    public void setGm1Perc(int gm1Perc) {
        this.gm1Perc = gm1Perc;
    }

    public int getGm2Perc() {
        return gm2Perc;
    }

    public void setGm2Perc(int gm2Perc) {
        this.gm2Perc = gm2Perc;
    }

    public int getGm3Perc() {
        return gm3Perc;
    }

    public void setGm3Perc(int gm3Perc) {
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

    public String getPlayersChartImgHtml() {
        return playersChartImgHtml;
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

    public void setPlayersChartImgHtml(String playersChartImgHtml) {
        this.playersChartImgHtml = playersChartImgHtml;
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
