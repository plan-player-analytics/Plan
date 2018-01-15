package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import com.djrapitops.plan.utilities.comparators.UserInfoLastPlayedComparator;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.IOException;
import java.util.*;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends Response {

    public PlayersPageResponse() {
        super.setHeader("HTTP/1.1 200 OK");
        try {
            PlanSystem system = PlanSystem.getInstance();

            PlanPlugin plugin = PlanPlugin.getInstance();
            Database db = system.getDatabaseSystem().getActiveDatabase();
            List<String> names = new ArrayList<>(db.fetch().getPlayerNames().values());
            Collections.sort(names);
            Map<String, String> replace = new HashMap<>();
            if (Check.isBukkitAvailable()) {
                replace.put("networkName", Settings.SERVER_NAME.toString().replaceAll("[^a-zA-Z0-9_\\s]", "_"));
            } else {
                replace.put("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            }
            replace.put("playersTable", buildPlayersTable(db));
            replace.put("version", plugin.getVersion());
            super.setContent(Theme.replaceColors(StrSubstitutor.replace(FileUtil.getStringFromResource("web/players.html"), replace)));
        } catch (DBException | IOException e) {
            Log.toLog(this.getClass().getName(), e);
            setContent(new InternalErrorResponse("/players", e).getContent());
        }
    }

    private String buildPlayersTable(Database db) {
        try {
            List<UserInfo> users = new ArrayList<>(db.fetch().getUsers().values());
            users.sort(new UserInfoLastPlayedComparator());
            Map<UUID, Long> lastSeenForAllPlayers = db.fetch().getLastSeenForAllPlayers();
            Map<UUID, List<Session>> sessionsByUser = AnalysisUtils.sortSessionsByUser(db.fetch().getSessionsWithNoExtras());
            Map<UUID, List<GeoInfo>> geoInfos = db.fetch().getAllGeoInfo();

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
        } catch (DBException e) {
            Log.toLog(PlayersPageResponse.class.getClass().getName(), e);
            return new InternalErrorResponse(e, "/players").getContent();
        }
    }
}
