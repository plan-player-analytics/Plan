package main.java.com.djrapitops.plan.systems.webserver.response;

import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.data.container.GeoInfo;
import main.java.com.djrapitops.plan.data.container.Session;
import main.java.com.djrapitops.plan.data.container.UserInfo;
import main.java.com.djrapitops.plan.data.element.TableContainer;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import main.java.com.djrapitops.plan.utilities.comparators.UserInfoLastPlayedComparator;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.Html;
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

            StringBuilder html = new StringBuilder("<table class=\"table table-bordered table-striped table-hover player-table dataTable\">");

            TableContainer tableContainer = new TableContainer(userS, playtimeS, sessionsS, registeredS, lastSeenS, geolocationS);
            String header = tableContainer.parseHeader();
            html.append(header);
            if (Settings.PLAYERTABLE_FOOTER.isTrue()) {
                html.append(header.replace("thead", "tfoot"));
            }

            try {
                if (users.isEmpty()) {
                    tableContainer.addRow("<b>No Players</b>");
                    throw new IllegalArgumentException("No players");
                }

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
                    String playerName = userInfo.getName();

                    String link = Html.LINK_EXTERNAL.parse("../player/" + playerName, playerName);

                    List<Session> sessions = sessionsByUser.getOrDefault(uuid, new ArrayList<>());
                    int sessionCount = sessions.size();
                    long playtime = sessionCount != 0 ? sessions.stream().map(Session::getLength)
                            .mapToLong(p -> p)
                            .sum() : 0L;
                    long registered = userInfo.getRegistered();
                    long lastSeen = lastSeenForAllPlayers.getOrDefault(uuid, 0L);
                    List<GeoInfo> geoInfoList = geoInfos.getOrDefault(uuid, new ArrayList<>());
                    geoInfoList.sort(new GeoInfoComparator());
                    String geolocation = geoInfoList.isEmpty() ? "Not Known" : geoInfoList.get(0).getGeolocation();

                    html.append(Html.TABLELINE_PLAYERS_PLAYERS_PAGE.parse(
                            link,
                            playtime, FormatUtils.formatTimeAmount(playtime),
                            sessionCount + "",
                            FormatUtils.formatTimeStampYear(registered),
                            lastSeen != 0 ? FormatUtils.formatTimeStampYear(lastSeen) : "-",
                            geolocation
                    ));
                    i++;
                }

            } catch (IllegalArgumentException ignored) {
            }
            return html.append("</tbody></table>").toString();
        } catch (SQLException e) {
            Log.toLog(PlayersPageResponse.class.getClass().getName(), e);
            return new InternalErrorResponse(e, "/players").getContent();
        }
    }
}
