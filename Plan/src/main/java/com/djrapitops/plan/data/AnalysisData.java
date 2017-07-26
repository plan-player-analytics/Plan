package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.analysis.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Big container object for Data.
 *
 * Contains parts that can be analysed. Each part has their own purpose.
 *
 * Parts contain variables that can be added to. These variables are then
 * analysed using the analysis method.
 *
 * After being analysed the ReplaceMap can be retrieved for replacing
 * placeholders on the analysis.html file.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisData extends RawData<AnalysisData> {

    private long refreshDate;

    private String planVersion;
    private String pluginsTabLayout;
    private Map<String, String> additionalDataReplaceMap;

    private String playersTable;

    private final ActivityPart activityPart;
    private final CommandUsagePart commandUsagePart;
    private final GamemodePart gamemodePart;
    private final GeolocationPart geolocationPart;
    private final JoinInfoPart joinInfoPart;
    private final KillPart killPart;
    private final PlayerCountPart playerCountPart;
    private final PlaytimePart playtimePart;
    private final TPSPart tpsPart;

    public AnalysisData(Map<String, Integer> commandUsage, List<TPS> tpsData) {
        commandUsagePart = new CommandUsagePart(commandUsage);
        geolocationPart = new GeolocationPart();
        joinInfoPart = new JoinInfoPart();
        playerCountPart = new PlayerCountPart();
        playtimePart = new PlaytimePart(playerCountPart);
        killPart = new KillPart(playerCountPart);
        gamemodePart = new GamemodePart();
        tpsPart = new TPSPart(tpsData);
        activityPart = new ActivityPart(joinInfoPart, tpsPart);
    }

    public ActivityPart getActivityPart() {
        return activityPart;
    }

    public CommandUsagePart getCommandUsagePart() {
        return commandUsagePart;
    }

    public GamemodePart getGamemodePart() {
        return gamemodePart;
    }

    public GeolocationPart getGeolocationPart() {
        return geolocationPart;
    }

    public JoinInfoPart getJoinInfoPart() {
        return joinInfoPart;
    }

    public KillPart getKillPart() {
        return killPart;
    }

    public PlayerCountPart getPlayerCountPart() {
        return playerCountPart;
    }

    public PlaytimePart getPlaytimePart() {
        return playtimePart;
    }

    public TPSPart getTpsPart() {
        return tpsPart;
    }

    public List<RawData> getAllParts() {
        return Arrays.asList(new RawData[]{
            activityPart, commandUsagePart, gamemodePart,
            geolocationPart, joinInfoPart, killPart,
            playerCountPart, playtimePart, tpsPart
        });
    }

    public String getPlanVersion() {
        return planVersion;
    }

    public void setPlanVersion(String planVersion) {
        this.planVersion = planVersion;
    }

    public String getPluginsTabLayout() {
        return pluginsTabLayout;
    }

    public void setPluginsTabLayout(String pluginsTabLayout) {
        this.pluginsTabLayout = pluginsTabLayout;
    }

    public Map<String, String> getAdditionalDataReplaceMap() {
        return additionalDataReplaceMap;
    }

    public void setAdditionalDataReplaceMap(Map<String, String> additionalDataReplaceMap) {
        this.additionalDataReplaceMap = additionalDataReplaceMap;
    }

    public void setRefreshDate(long refreshDate) {
        this.refreshDate = refreshDate;
    }

    public void setPlayersTable(String playersTable) {
        this.playersTable = playersTable;
    }

    @Override
    protected void analyse() {
        Verify.nullCheck(playersTable);
        Verify.nullCheck(pluginsTabLayout);
        Verify.nullCheck(planVersion);

        addValue("sortabletable", playersTable);
        addValue("version", planVersion);

        final List<RawData> parts = getAllParts();
        parts.forEach((part) -> {
            try {
                Benchmark.start("Analysis Phase: " + part.getClass().getSimpleName());
                part.analyseData();
                Benchmark.stop("Analysis Phase: " + part.getClass().getSimpleName());
                if (part.isAnalysed()) {
                    addValues(part.getReplaceMap());
                }
            } catch (Exception e) {
                Log.toLog(this.getClass().getName(), e);
            }
        });
        refreshDate = MiscUtils.getTime();
    }

    public String replacePluginsTabLayout() {
        String replacedOnce = HtmlUtils.replacePlaceholders(pluginsTabLayout, additionalDataReplaceMap);
        return HtmlUtils.replacePlaceholders(replacedOnce, additionalDataReplaceMap);
    }

    public long getRefreshDate() {
        return refreshDate;
    }
}
