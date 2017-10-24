package main.java.com.djrapitops.plan.data;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.analysis.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Big container object for Data.
 * <p>
 * Contains parts that can be analysed. Each part has their own purpose.
 * <p>
 * Parts contain variables that can be added to. These variables are then
 * analysed using the analysis method.
 * <p>
 * After being analysed the ReplaceMap can be retrieved for replacing
 * placeholders on the server.html file.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisData extends RawData {

    private final ActivityPart activityPart;
    private final CommandUsagePart commandUsagePart;
    private final GeolocationPart geolocationPart;
    private final JoinInfoPart joinInfoPart;
    private final KillPart killPart;
    private final PlayerCountPart playerCountPart;
    private final PlaytimePart playtimePart;
    private final TPSPart tpsPart;
    private final WorldPart worldPart;
    private long refreshDate;
    private String pluginsTabLayout;
    private Map<String, Serializable> additionalDataReplaceMap;
    private String playersTable;

    public AnalysisData() {
        commandUsagePart = new CommandUsagePart();
        geolocationPart = new GeolocationPart();
        joinInfoPart = new JoinInfoPart();
        playerCountPart = new PlayerCountPart();
        playtimePart = new PlaytimePart();
        killPart = new KillPart(joinInfoPart);
        tpsPart = new TPSPart();
        activityPart = new ActivityPart(playerCountPart, joinInfoPart, tpsPart);
        worldPart = new WorldPart();
    }

    public ActivityPart getActivityPart() {
        return activityPart;
    }

    public CommandUsagePart getCommandUsagePart() {
        return commandUsagePart;
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

    public WorldPart getWorldPart() {
        return worldPart;
    }

    public List<RawData> getAllParts() {
        return Arrays.asList(activityPart, commandUsagePart, geolocationPart,
                joinInfoPart, killPart, playerCountPart, playtimePart, tpsPart,
                worldPart);
    }

    public void setPluginsTabLayout(String pluginsTabLayout) {
        this.pluginsTabLayout = pluginsTabLayout;
    }

    public void setAdditionalDataReplaceMap(Map<String, Serializable> additionalDataReplaceMap) {
        this.additionalDataReplaceMap = additionalDataReplaceMap;
    }

    public void setPlayersTable(String playersTable) {
        this.playersTable = playersTable;
    }

    @Override
    protected void analyse() {
        if (playersTable == null) {
            playersTable = "";
        }
        if (pluginsTabLayout == null) {
            pluginsTabLayout = "";
        }

        addValue("tableBodyPlayerList", playersTable);
        addValue("version", MiscUtils.getIPlan().getVersion());

        final List<RawData> parts = getAllParts();
        parts.forEach(part -> {
            try {
                Benchmark.start(part.getClass().getSimpleName());
                part.analyseData();
                Benchmark.stop("Analysis", part.getClass().getSimpleName());
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
        return HtmlUtils.replacePlaceholders(pluginsTabLayout, additionalDataReplaceMap);
    }

    public long getRefreshDate() {
        return refreshDate;
    }
}
