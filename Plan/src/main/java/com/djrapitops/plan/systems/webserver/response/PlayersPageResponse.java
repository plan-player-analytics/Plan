package main.java.com.djrapitops.plan.systems.webserver.response;

import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.GeoInfo;
import main.java.com.djrapitops.plan.data.PlayerProfile;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.data.additional.TableContainer;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import main.java.com.djrapitops.plan.utilities.comparators.UserInfoLastPlayedComparator;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.Html;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends Response {

    public PlayersPageResponse() {
        super.setHeader("HTTP/1.1 200 OK");
        try {
            IPlan plugin = MiscUtils.getIPlan();
            List<String> names = new ArrayList<>(plugin.getDB().getUsersTable().getPlayerNames().values());
            Collections.sort(names);
            Map<String, String> replace = new HashMap<>();
            if (Check.isBukkitAvailable()) {
                replace.put("networkName", Settings.SERVER_NAME.toString());
            } else {
                replace.put("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            }
            replace.put("playersTable", buildPlayersTable(plugin.getDB()));
            replace.put("version", plugin.getVersion());
            super.setContent(Theme.replaceColors(StrSubstitutor.replace(FileUtil.getStringFromResource("web/players.html"), replace)));
        } catch (SQLException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            setContent(new InternalErrorResponse(e, "/players").getContent());
        }
    }

    public static String buildPlayersTable(Database db) {
        try {
            List<UserInfo> users = new ArrayList<>(db.getUsersTable().getUsers().values());
            users.sort(new UserInfoLastPlayedComparator());
            Map<UUID, Long> lastSeenForAllPlayers = db.getSessionsTable().getLastSeenForAllPlayers();
            Map<UUID, List<Session>> sessionsByUser = AnalysisUtils.sortSessionsByUser(db.getSessionsTable().getAllSessions(false));
            Map<UUID, List<GeoInfo>> geoInfos = db.getIpsTable().getAllGeoInfo();

            String userS = Html.FONT_AWESOME_ICON.parse("user") + " Player";
            String playtimeS = Html.FONT_AWESOME_ICON.parse("clock-o") + " Playtime";
            String sessionsS = Html.FONT_AWESOME_ICON.parse("calendar-plus-o") + " Sessions";
            String registeredS = Html.FONT_AWESOME_ICON.parse("user-plus") + " Registered";
            String lastSeenS = Html.FONT_AWESOME_ICON.parse("calendar-check-o") + " Last Seen";
            String geolocationS = Html.FONT_AWESOME_ICON.parse("globe") + " Geolocation";

            TableContainer tableContainer = new TableContainer(userS, playtimeS, sessionsS, registeredS, lastSeenS, geolocationS);

            try {
                if (users.isEmpty()) {
                    tableContainer.addRow("<b>No Players</b>");
                    throw new IllegalArgumentException("No players");
                }

                List<String[]> sortedData = new ArrayList<>();
                API planAPI = Plan.getPlanAPI();

                int i = 0;
                int maxPlayers = Settings.MAX_PLAYERS_PLAYERS_PAGE.getNumber();
                if (maxPlayers <= 0) {
                    maxPlayers = 25000;
                }
                for (UserInfo userInfo : users) {
                    if (i >= maxPlayers) {
                        break;
                    }
                    UUID uuid = userInfo.getUuid();
                    String[] playerData = new String[6];
                    String playerName = userInfo.getName();

                    String link = Html.LINK_EXTERNAL.parse(planAPI.getPlayerInspectPageLink(playerName), playerName);

                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());
                    int sessionCount = sessions.size();
                    long playtime = sessionCount != 0 ? PlayerProfile.getPlaytime(sessions.stream()) : 0L;
                    long registered = userInfo.getRegistered();
                    long lastSeen = lastSeenForAllPlayers.getOrDefault(uuid, 0L);
                    List<GeoInfo> geoInfoList = geoInfos.getOrDefault(uuid, new ArrayList<>());
                    geoInfoList.sort(new GeoInfoComparator());
                    String geolocation = geoInfoList.isEmpty() ? "Not Known" : geoInfoList.get(0).getGeolocation();

                    playerData[0] = link;
                    playerData[1] = FormatUtils.formatTimeAmount(playtime);
                    playerData[2] = sessionCount + "";
                    playerData[3] = FormatUtils.formatTimeStampYear(registered);
                    playerData[4] = lastSeen != 0 ? FormatUtils.formatTimeStampYear(lastSeen) : "-";
                    playerData[5] = geolocation;
                    sortedData.add(playerData);
                    i++;
                }

                for (String[] playerData : sortedData) {
                    tableContainer.addRow(ArrayUtils.addAll(playerData));
                }
            } catch (IllegalArgumentException ignored) {
            }
            return tableContainer.parseHtml().replace(Html.TABLE_SCROLL.parse(),
                    "<table class=\"table table-bordered table-striped table-hover player-table dataTable\">");
        } catch (SQLException e) {
            Log.toLog(PlayersPageResponse.class.getClass().getName(), e);
            return new InternalErrorResponse(e, "/players").getContent();
        }
    }
}
