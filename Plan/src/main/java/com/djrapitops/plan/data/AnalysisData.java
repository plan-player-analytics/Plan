package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.api.TimeAmount;
import com.google.common.base.Objects;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.database.tables.Actions;
import main.java.com.djrapitops.plan.systems.webserver.theme.Colors;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;
import main.java.com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import main.java.com.djrapitops.plan.utilities.html.graphs.*;
import main.java.com.djrapitops.plan.utilities.html.structure.SessionTabStructureCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.CommandUseTableCreator;
import main.java.com.djrapitops.plan.utilities.html.tables.SessionsTableCreator;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

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

    private long refreshDate;
    private String pluginsTabLayout;
    private Map<String, Serializable> additionalDataReplaceMap;
    @Deprecated
    private String playersTable;

    public AnalysisData() {
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

    private void addConstants() {
        addValue("version", MiscUtils.getIPlan().getVersion());
        addValue("worldPieColors", Settings.THEME_GRAPH_WORLD_PIE.toString());
        addValue("gmPieColors", Settings.THEME_GRAPH_GM_PIE.toString());
        addValue("serverName", Settings.SERVER_NAME.toString());
        addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());
        addValue("refresh", FormatUtils.formatTimeStamp(refreshDate));

        addValue("activityPieColors", Settings.THEME_GRAPH_ACTIVITY_PIE.toString());
        addValue("playersGraphColor", Colors.PLAYERS_ONLINE.getColor());
        addValue("tpsHighColor", Colors.TPS_HIGH.getColor());
        addValue("tpsMediumColor", Colors.TPS_MED.getColor());
        addValue("tpsLowColor", Colors.TPS_LOW.getColor());
        addValue("tpsMedium", Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        addValue("tpsHigh", Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addValue("playersMax", ServerProfile.getPlayersMax());
        addValue("playersOnline", ServerProfile.getPlayersOnline());
    }

    public String replacePluginsTabLayout() {
        return HtmlUtils.replacePlaceholders(pluginsTabLayout, additionalDataReplaceMap);
    }

    public long getRefreshDate() {
        return refreshDate;
    }

    public void analyze(ServerProfile profile) {
        addConstants();
        long now = MiscUtils.getTime();
        refreshDate = now;
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        Map<UUID, List<Session>> sessions = profile.getSessions();
        List<Session> allSessions = profile.getAllSessions();
        allSessions.sort(new SessionStartComparator());

        List<PlayerProfile> players = profile.getPlayers();
        List<PlayerProfile> ops = profile.getOps().collect(Collectors.toList());
        int playersTotal = players.size();

        List<TPS> tpsData = profile.getTPSData(0, now).collect(Collectors.toList());
        List<TPS> tpsDataDay = profile.getTPSData(dayAgo, now).collect(Collectors.toList());
        List<TPS> tpsDataWeek = profile.getTPSData(weekAgo, now).collect(Collectors.toList());
        List<TPS> tpsDataMonth = profile.getTPSData(monthAgo, now).collect(Collectors.toList());

        List<String> geoLocations = profile.getGeoLocations();
        Map<String, Integer> commandUsage = profile.getCommandUsage();

        directProfileVariables(profile);
        performanceTab(tpsData, tpsDataDay, tpsDataWeek, tpsDataMonth);
        sessionData(monthAgo, sessions, allSessions);
        onlineActivityNumbers(profile, sessions, players);
        geolocationsTab(geoLocations);
        commandUsage(commandUsage);

        addValue("ops", ops.size());
        addValue("playersTotal", playersTotal);

        // TODO Rewrite Activity Pie
        addValue("playersActive", 0);
        addValue("active", 0);
        addValue("inactive", 0);
        addValue("joinLeaver", 0);
        addValue("banned", 0);

        addValue("playtimeTotal", FormatUtils.formatTimeAmount(profile.getTotalPlaytime()));
        addValue("playtimeAverage", FormatUtils.formatTimeAmount(profile.getAveragePlayTime()));
    }

    private void commandUsage(Map<String, Integer> commandUsage) {
        addValue("commandUniqueCount", String.valueOf(commandUsage.size()));
        addValue("commandCount", MathUtils.sumInt(commandUsage.values().stream().map(i -> (int) i)));
        addValue("tableBodyCommands", HtmlUtils.removeXSS(CommandUseTableCreator.createTable(commandUsage)));
    }

    private void geolocationsTab(List<String> geoLocations) {
        Map<String, String> geoCodes = new HashMap<>();
        Map<String, Integer> geoCodeCounts = new HashMap<>();
        String[] countries = new String[]{"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas, The", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Democratic Republic of the", "Congo, Republic of the", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Curacao", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Islas Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Polynesia", "Gabon", "Gambia, The", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guam", "Guatemala", "Guernsey", "Guinea-Bissau", "Guinea", "Guyana", "Haiti", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea, South", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia, Federated States of", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Namibia", "Nepal", "Netherlands", "New Caledonia", "New Zealand", "Nicaragua", "Nigeria", "Niger", "Niue", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Puerto Rico", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Vietnam", "Virgin Islands", "West Bank", "Yemen", "Zambia", "Zimbabwe"};
        String[] codes = new String[]{"AFG", "ALB", "DZA", "ASM", "AND", "AGO", "AIA", "ATG", "ARG", "ARM", "ABW", "AUS", "AUT", "AZE", "BHM", "BHR", "BGD", "BRB", "BLR", "BEL", "BLZ", "BEN", "BMU", "BTN", "BOL", "BIH", "BWA", "BRA", "VGB", "BRN", "BGR", "BFA", "MMR", "BDI", "CPV", "KHM", "CMR", "CAN", "CYM", "CAF", "TCD", "CHL", "CHN", "COL", "COM", "COD", "COG", "COK", "CRI", "CIV", "HRV", "CUB", "CUW", "CYP", "CZE", "DNK", "DJI", "DMA", "DOM", "ECU", "EGY", "SLV", "GNQ", "ERI", "EST", "ETH", "FLK", "FRO", "FJI", "FIN", "FRA", "PYF", "GAB", "GMB", "GEO", "DEU", "GHA", "GIB", "GRC", "GRL", "GRD", "GUM", "GTM", "GGY", "GNB", "GIN", "GUY", "HTI", "HND", "HKG", "HUN", "ISL", "IND", "IDN", "IRN", "IRQ", "IRL", "IMN", "ISR", "ITA", "JAM", "JPN", "JEY", "JOR", "KAZ", "KEN", "KIR", "KOR", "PRK", "KSV", "KWT", "KGZ", "LAO", "LVA", "LBN", "LSO", "LBR", "LBY", "LIE", "LTU", "LUX", "MAC", "MKD", "MDG", "MWI", "MYS", "MDV", "MLI", "MLT", "MHL", "MRT", "MUS", "MEX", "FSM", "MDA", "MCO", "MNG", "MNE", "MAR", "MOZ", "NAM", "NPL", "NLD", "NCL", "NZL", "NIC", "NGA", "NER", "NIU", "MNP", "NOR", "OMN", "PAK", "PLW", "PAN", "PNG", "PRY", "PER", "PHL", "POL", "PRT", "PRI", "QAT", "ROU", "RUS", "RWA", "KNA", "LCA", "MAF", "SPM", "VCT", "WSM", "SMR", "STP", "SAU", "SEN", "SRB", "SYC", "SLE", "SGP", "SXM", "SVK", "SVN", "SLB", "SOM", "ZAF", "SSD", "ESP", "LKA", "SDN", "SUR", "SWZ", "SWE", "CHE", "SYR", "TWN", "TJK", "TZA", "THA", "TLS", "TGO", "TON", "TTO", "TUN", "TUR", "TKM", "TUV", "UGA", "UKR", "ARE", "GBR", "USA", "URY", "UZB", "VUT", "VEN", "VNM", "VGB", "WBG", "YEM", "ZMB", "ZWE"};
        for (int i = 0; i < countries.length; i++) {
            String country = countries[i];
            String countryCode = codes[i];

            geoCodes.put(country, countryCode);
            geoCodeCounts.put(countryCode, 0);
        }
        for (String geoLocation : geoLocations) {
            String countryCode = geoCodes.get(geoLocation);
            if (countryCode != null) {
                geoCodeCounts.computeIfPresent(countryCode, (computedCountry, amount) -> amount + 1);
            }
        }
        addValue("geoMapSeries", WorldMapCreator.createDataSeries(geoCodeCounts));
    }

    private void onlineActivityNumbers(ServerProfile profile, Map<UUID, List<Session>> sessions, List<PlayerProfile> players) {
        long now = MiscUtils.getTime();
        long dayAgo = now - TimeAmount.DAY.ms();
        long weekAgo = now - TimeAmount.WEEK.ms();
        long monthAgo = now - TimeAmount.MONTH.ms();

        List<PlayerProfile> newDay = profile.getPlayersWhoRegistered(dayAgo, now).collect(Collectors.toList());
        List<PlayerProfile> newWeek = profile.getPlayersWhoRegistered(weekAgo, now).collect(Collectors.toList());
        List<PlayerProfile> newMonth = profile.getPlayersWhoRegistered(monthAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueDay = profile.getPlayersWhoPlayedBetween(dayAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueWeek = profile.getPlayersWhoPlayedBetween(weekAgo, now).collect(Collectors.toList());
        List<PlayerProfile> uniqueMonth = profile.getPlayersWhoPlayedBetween(monthAgo, now).collect(Collectors.toList());

        int uniqD = uniqueDay.size();
        int uniqW = uniqueWeek.size();
        int uniqM = uniqueMonth.size();
        int newD = newDay.size();
        int newW = newWeek.size();
        int newM = newMonth.size();
        int playersTotal = players.size();

        addValue("playersDay", uniqD);
        addValue("playersWeek", uniqW);
        addValue("playersMonth", uniqM);
        addValue("playersNewDay", newD);
        addValue("playersNewWeek", newW);
        addValue("playersNewMonth", newM);

        addValue("playersAverage", AnalysisUtils.getUniqueJoinsPerDay(sessions, -1));
        addValue("playersAverageDay", AnalysisUtils.getUniqueJoinsPerDay(sessions, dayAgo));
        addValue("playersAverageWeek", AnalysisUtils.getUniqueJoinsPerDay(sessions, weekAgo));
        addValue("playersAverageMonth", AnalysisUtils.getUniqueJoinsPerDay(sessions, monthAgo));
        addValue("playersNewAverage", AnalysisUtils.getNewUsersPerDay(toRegistered(players), -1, playersTotal));
        addValue("playersNewAverageDay", AnalysisUtils.getNewUsersPerDay(toRegistered(newDay), -1, newD));
        addValue("playersNewAverageWeek", AnalysisUtils.getNewUsersPerDay(toRegistered(newWeek), -1, newW));
        addValue("playersNewAverageMonth", AnalysisUtils.getNewUsersPerDay(toRegistered(newMonth), -1, newM));

        stickiness(now, weekAgo, monthAgo, newDay, newWeek, newMonth, newD, newW, newM);
    }

    private void stickiness(long now, long weekAgo, long monthAgo,
                            List<PlayerProfile> newDay, List<PlayerProfile> newWeek, List<PlayerProfile> newMonth,
                            int newD, int newW, int newM) {
        long fourDaysAgo = now - TimeAmount.DAY.ms() * 4L;
        long twoWeeksAgo = now - TimeAmount.WEEK.ms() * 2L;

        List<PlayerProfile> playersStuckPerMonth = newMonth.stream()
                .filter(p -> p.playedBetween(monthAgo, twoWeeksAgo) && p.playedBetween(twoWeeksAgo, now))
                .collect(Collectors.toList());
        List<PlayerProfile> playersStuckPerWeek = newWeek.stream()
                .filter(p -> p.playedBetween(weekAgo, fourDaysAgo) && p.playedBetween(fourDaysAgo, now))
                .collect(Collectors.toList());

        int stuckPerM = playersStuckPerMonth.size();
        int stuckPerW = playersStuckPerWeek.size();

        addValue("playersStuckMonth", stuckPerM);
        addValue("playersStuckWeek", stuckPerW);
        addValue("playersStuckPercMonth", newM != 0 ? FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerM, newM)) + "%" : "-");
        addValue("playersStuckPercWeek", newW != 0 ? FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerW, newW)) + "%" : "-");

        if (newD != 0) {
            // New Players
            Set<StickyData> stickyM = newMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            Set<StickyData> stickyW = playersStuckPerMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            // New Players who stayed
            Set<StickyData> stickyStuckM = newMonth.stream().map(StickyData::new).distinct().collect(Collectors.toSet());
            Set<StickyData> stickyStuckW = playersStuckPerWeek.stream().map(StickyData::new).distinct().collect(Collectors.toSet());

            int stuckPerD = 0;
            for (PlayerProfile playerProfile : newDay) {
                StickyData data = new StickyData(playerProfile);

                Set<StickyData> similarM = new HashSet<>();
                Set<StickyData> similarW = new HashSet<>();
                for (StickyData stickyData : stickyM) {
                    if (stickyData.distance(data) < 2.5) {
                        similarM.add(stickyData);
                    }
                }
                for (StickyData stickyData : stickyW) {
                    if (stickyData.distance(data) < 2.5) {
                        similarW.add(stickyData);
                    }
                }

                double probability = 1.0;

                int stickM = 0;
                for (StickyData stickyData : stickyStuckM) {
                    if (similarM.contains(stickyData)) {
                        stickM++;
                    }
                }

                probability *= (stickM / similarM.size());

                int stickW = 0;
                for (StickyData stickyData : stickyStuckW) {
                    if (similarW.contains(stickyData)) {
                        stickW++;
                    }
                }

                probability *= (stickW / similarW.size());

                if (probability >= 0.5) {
                    stuckPerD++;
                }
            }
            addValue("playersStuckDay", stuckPerD);
            addValue("playersStuckPercDay", FormatUtils.cutDecimals(MathUtils.averageDouble(stuckPerD, newD)) + "%");
        } else {
            addValue("playersStuckDay", 0);
            addValue("playersStuckPercDay", "-");
        }
    }

    private List<Long> toRegistered(List<PlayerProfile> players) {
        return players.stream().map(PlayerProfile::getRegistered).collect(Collectors.toList());
    }

    private void sessionData(long monthAgo, Map<UUID, List<Session>> sessions, List<Session> allSessions) {
        List<Session> sessionsMonth = allSessions.stream()
                .filter(s -> s.getSessionStart() >= monthAgo)
                .collect(Collectors.toList());
        String[] tables = SessionsTableCreator.createTable(sessions, allSessions);
        String[] sessionContent = SessionTabStructureCreator.creteStructure(sessions, allSessions);

        addValue("sessionCount", allSessions.size());
        addValue("accordionSessions", sessionContent[0]);
        addValue("sessionTabGraphViewFunctions", sessionContent[1]);
        addValue("tableBodySessions", tables[0]);
        addValue("listRecentLogins", tables[1]);
        addValue("sessionAverage", FormatUtils.formatTimeAmount(MathUtils.averageLong(allSessions.stream().map(Session::getLength))));
        addValue("punchCardSeries", PunchCardGraphCreator.createDataSeries(sessionsMonth));

        addValue("deathCount", ServerProfile.getDeathCount(allSessions));
        addValue("mobKillCount", ServerProfile.getMobKillCount(allSessions));
        addValue("killCount", ServerProfile.getPlayerKills(allSessions).size());
    }

    private void directProfileVariables(ServerProfile profile) {
        WorldTimes worldTimes = profile.getServerWorldtimes();
        long allTimePeak = profile.getAllTimePeak();
        long lastPeak = profile.getLastPeakDate();

        addValue("tablePlayerlist", Html.TABLE_PLAYERS.parse(profile.createPlayersTableBody()));
        addValue("worldTotal", FormatUtils.formatTimeAmount(worldTimes.getTotal()));
        String[] seriesData = WorldPieCreator.createSeriesData(worldTimes);
        addValue("worldSeries", seriesData[0]);
        addValue("gmSeries", seriesData[1]);
        addValue("lastPeakTime", lastPeak != -1 ? FormatUtils.formatTimeStampYear(lastPeak) : "No Data");
        addValue("playersLastPeak", lastPeak != -1 ? profile.getLastPeakPlayers() : "-");
        addValue("bestPeakTime", allTimePeak != -1 ? FormatUtils.formatTimeStampYear(allTimePeak) : "No Data");
        addValue("playersBestPeak", allTimePeak != -1 ? profile.getAllTimePeakPlayers() : "-");
    }

    private void performanceTab(List<TPS> tpsData, List<TPS> tpsDataDay, List<TPS> tpsDataWeek, List<TPS> tpsDataMonth) {
        addValue("tpsSpikeMonth", ServerProfile.getLowSpikeCount(tpsDataMonth));
        addValue("tpsSpikeWeek", ServerProfile.getLowSpikeCount(tpsDataWeek));
        addValue("tpsSpikeDay", ServerProfile.getLowSpikeCount(tpsDataDay));

        addValue("playersOnlineSeries", PlayerActivityGraphCreator.buildSeriesDataString(tpsData));
        addValue("tpsSeries", TPSGraphCreator.buildSeriesDataString(tpsData));
        addValue("cpuSeries", CPUGraphCreator.buildSeriesDataString(tpsData));
        addValue("ramSeries", RamGraphCreator.buildSeriesDataString(tpsData));
        addValue("entitySeries", WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsData));
        addValue("chunkSeries", WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsData));

        double averageCPUMonth = MathUtils.averageDouble(tpsDataMonth.stream().map(TPS::getCPUUsage).filter(i -> i != 0));
        double averageCPUWeek = MathUtils.averageDouble(tpsDataWeek.stream().map(TPS::getCPUUsage).filter(i -> i != 0));
        double averageCPUDay = MathUtils.averageDouble(tpsDataDay.stream().map(TPS::getCPUUsage).filter(i -> i != 0));

        addValue("tpsAverageMonth", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataMonth.stream().map(TPS::getTicksPerSecond))));
        addValue("tpsAverageWeek", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataWeek.stream().map(TPS::getTicksPerSecond))));
        addValue("tpsAverageDay", FormatUtils.cutDecimals(MathUtils.averageDouble(tpsDataDay.stream().map(TPS::getTicksPerSecond))));

        addValue("cpuAverageMonth", averageCPUMonth >= 0 ? FormatUtils.cutDecimals(averageCPUMonth) + "%" : "Unavailable");
        addValue("cpuAverageWeek", averageCPUWeek >= 0 ? FormatUtils.cutDecimals(averageCPUWeek) + "%" : "Unavailable");
        addValue("cpuAverageDay", averageCPUDay >= 0 ? FormatUtils.cutDecimals(averageCPUDay) + "%" : "Unavailable");

        addValue("ramAverageMonth", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataMonth.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));
        addValue("ramAverageWeek", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataWeek.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));
        addValue("ramAverageDay", FormatUtils.cutDecimals(MathUtils.averageLong(tpsDataDay.stream().map(TPS::getUsedMemory).filter(i -> i != 0))));

        addValue("entityAverageMonth", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataMonth.stream().map(TPS::getEntityCount).filter(i -> i != 0))));
        addValue("entityAverageWeek", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataWeek.stream().map(TPS::getEntityCount).filter(i -> i != 0))));
        addValue("entityAverageDay", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataDay.stream().map(TPS::getEntityCount).filter(i -> i != 0))));

        addValue("chunkAverageMonth", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataMonth.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
        addValue("chunkAverageWeek", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataWeek.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
        addValue("chunkAverageDay", FormatUtils.cutDecimals(MathUtils.averageInt(tpsDataDay.stream().map(TPS::getChunksLoaded).filter(i -> i != 0))));
    }
}

class StickyData {
    private final double activityIndex;
    private Integer messagesSent;
    private Integer onlineOnJoin;

    public StickyData(PlayerProfile player) {
        activityIndex = player.getActivityIndex(player.getRegistered() + TimeAmount.DAY.ms());
        for (Action action : player.getActions()) {
            if (messagesSent == null && action.getDoneAction() == Actions.FIRST_LOGOUT) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(": ");
                if (split.length == 2) {
                    try {
                        messagesSent = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (onlineOnJoin == null && action.getDoneAction() == Actions.FIRST_SESSION) {
                String additionalInfo = action.getAdditionalInfo();
                String[] split = additionalInfo.split(" ");
                if (split.length == 3) {
                    try {
                        onlineOnJoin = Integer.parseInt(split[1]);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    public double distance(StickyData data) {
        double num = 0;
        num += Math.abs(data.activityIndex - activityIndex) * 2.0;
        num += Math.abs(data.onlineOnJoin - onlineOnJoin) / 10.0;
        num += Math.abs(data.messagesSent - messagesSent) / 10.0;

        return num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StickyData that = (StickyData) o;
        return Double.compare(that.activityIndex, activityIndex) == 0 &&
                Objects.equal(messagesSent, that.messagesSent) &&
                Objects.equal(onlineOnJoin, that.onlineOnJoin);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(activityIndex, messagesSent, onlineOnJoin);
    }
}
