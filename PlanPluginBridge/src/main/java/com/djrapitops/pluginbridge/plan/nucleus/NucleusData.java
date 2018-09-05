/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.nucleus;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.nucleusdata.*;
import io.github.nucleuspowered.nucleus.api.service.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * PluginData for Nucleus plugin.
 *
 * @author Vankka
 */
public class NucleusData extends PluginData {
    private UserStorageService userStorageService = null;

    public NucleusData() {
        super(ContainerSize.TWO_THIRDS, "Nucleus");
        setPluginIcon(Icon.called("flask").of(Color.DEEP_ORANGE).build());

        Sponge.getServiceManager().provide(UserStorageService.class).ifPresent(storageService -> userStorageService = storageService);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        User user = getUser(uuid);

        if (user == null) {
            inspectContainer.addValue("Data unavailable", "Could not get user data");
            return inspectContainer;
        }

        NucleusAPI.getMuteService().ifPresent(muteService -> addMuteData(user, muteService, inspectContainer));
        NucleusAPI.getJailService().ifPresent(jailService -> addJailData(user, jailService, inspectContainer));
        NucleusAPI.getHomeService().ifPresent(homeService -> addHomeData(user, homeService, inspectContainer));
        NucleusAPI.getNoteService().ifPresent(noteService -> addNoteData(user, noteService, inspectContainer));
        NucleusAPI.getWarningService().ifPresent(warningService -> addWarningData(user, warningService, inspectContainer));
        NucleusAPI.getInvulnerabilityService().ifPresent(invulnerabilityService -> addInvulnerabilityData(user, invulnerabilityService, inspectContainer));
        NucleusAPI.getNicknameService().ifPresent(nicknameService -> addNicknameData(user, nicknameService, inspectContainer));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        NucleusAPI.getWarpService().ifPresent(warpService -> addWarpData(warpService, analysisContainer));
        NucleusAPI.getJailService().ifPresent(jailService -> addJailData(jailService, analysisContainer));
        NucleusAPI.getKitService().ifPresent(kitService -> addKitData(kitService, analysisContainer));

        return analysisContainer;
    }

    private User getUser(UUID uuid) {
        if (Sponge.getServer().getPlayer(uuid).isPresent()) {
            return Sponge.getServer().getPlayer(uuid).get();
        } else if (userStorageService != null) {
            Optional<User> optionalUser = userStorageService.get(uuid);
            return optionalUser.orElse(null);
        } else {
            return null;
        }
    }

    private String formatTimeStampYear(Instant instant) {
        return FormatUtils.formatTimeStampYear(instant.toEpochMilli());
    }

    private String formatTimeStampYear(Duration duration) {
        return FormatUtils.formatTimeStampYear(duration.plusMillis(System.currentTimeMillis()).toMillis());
    }

    /*
     * Player Data
     */
    private void addMuteData(User user, NucleusMuteService muteService, InspectContainer inspectContainer) {
        boolean muted = muteService.isMuted(user);
        inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), muted ? "Yes" : "No");

        Optional<MuteInfo> optionalMuteInfo = muteService.getPlayerMuteInfo(user);
        if (muted && optionalMuteInfo.isPresent()) {
            MuteInfo muteInfo = optionalMuteInfo.get();

            String reason = HtmlUtils.swapColorsToSpan(muteInfo.getReason());
            String start = muteInfo.getCreationInstant().map(this::formatTimeStampYear).orElse("Unknown");
            String end = muteInfo.getRemainingTime().map(this::formatTimeStampYear).orElse("Permanent mute");
            String link = "Unknown";

            User operatorUser = muteInfo.getMuter().map(this::getUser).orElse(null);
            if (operatorUser != null) {
                String operator = operatorUser.getName();
                link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
            }

            inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.DEEP_ORANGE)), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.DEEP_ORANGE).of(Family.REGULAR)), start);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.DEEP_ORANGE).of(Family.REGULAR)), end);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.DEEP_ORANGE).of(Family.REGULAR)), reason);
        }
    }

    private void addJailData(User user, NucleusJailService jailService, InspectContainer inspectContainer) {
        boolean jailed = jailService.isPlayerJailed(user);
        inspectContainer.addValue(getWithIcon("Jailed", Icon.called("bars").of(Color.YELLOW).of(Family.SOLID)), jailed ? "Yes" : "No");

        if (jailed && jailService.getPlayerJailData(user).isPresent()) {
            Inmate inmate = jailService.getPlayerJailData(user).get();

            String reason = inmate.getReason();
            String start = inmate.getCreationInstant().map(this::formatTimeStampYear).orElse("Unknown");
            String end = inmate.getRemainingTime().map(this::formatTimeStampYear).orElse("Permanent jail sentence");
            String link = "Unknown";

            User operatorUser = inmate.getJailer().map(this::getUser).orElse(null);
            if (operatorUser != null) {
                String operator = operatorUser.getName();
                link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
            }

            inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.YELLOW)), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.YELLOW).of(Family.REGULAR)), start);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.YELLOW).of(Family.REGULAR)), end);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.YELLOW).of(Family.REGULAR)), reason);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Jail", Icon.called("bars").of(Color.YELLOW).of(Family.SOLID)), inmate.getJailName());
        }
    }

    private void addHomeData(User user, NucleusHomeService homeService, InspectContainer inspectContainer) {
        int homeCount = homeService.getHomeCount(user);
        int maxHomes = homeService.getMaximumHomes(user);

        inspectContainer.addValue("&nbsp;" + getWithIcon("Homes", Icon.called("home").of(Color.GREEN).of(Family.SOLID)), homeCount + "/" + maxHomes);

        List<Home> homes = homeService.getHomes(user);

        if (!homes.isEmpty()) {
            TableContainer homesTable = new TableContainer(getWithIcon("Home", Icon.called("home").of(Family.SOLID)));
            homesTable.setColor("light-green");

            for (Home home : homes) {
                homesTable.addRow(home.getName());
            }

            inspectContainer.addTable("Homes", homesTable);
        }
    }

    private void addNoteData(User user, NucleusNoteService noteService, InspectContainer inspectContainer) {
        List<Note> notes = noteService.getNotes(user);

        if (!notes.isEmpty()) {
            TableContainer notesTable = new TableContainer(
                    getWithIcon("Noter", Icon.called("pen").of(Family.SOLID)),
                    getWithIcon("Note", Icon.called("sticky-note").of(Family.REGULAR))
            );

            notesTable.setColor("light-blue");

            for (Note note : notes) {
                String noter = "Unknown";

                User noterUser = note.getNoter().map(this::getUser).orElse(null);
                if (noterUser != null) {
                    noter = noterUser.getName();
                }

                notesTable.addRow(noter, note.getNote());
            }

            inspectContainer.addTable("Notes", notesTable);
        }
    }

    private void addWarningData(User user, NucleusWarningService warningService, InspectContainer inspectContainer) {
        List<Warning> warnings = warningService.getWarnings(user);
        inspectContainer.addValue(getWithIcon("Warning count", Icon.called("flag").of(Color.AMBER)), warnings.size());

        if (!warnings.isEmpty()) {
            TableContainer warningsTable = new TableContainer(
                    getWithIcon("Warner", Icon.called("exclamation").of(Family.SOLID)),
                    getWithIcon("Reason", Icon.called("sticky-note").of(Family.SOLID))
            );

            warningsTable.setColor("amber");

            for (Warning warning : warnings) {
                String warner = "Unknown";

                User warnerUser = warning.getWarner().map(this::getUser).orElse(null);
                if (warnerUser != null) {
                    warner = warnerUser.getName();
                }

                warningsTable.addRow(warner, warning.getReason());
            }

            inspectContainer.addTable("Warnings", warningsTable);
        }
    }

    private void addInvulnerabilityData(User user, NucleusInvulnerabilityService invulnerabilityService, InspectContainer inspectContainer) {
        boolean invulnerable = invulnerabilityService.isInvulnerable(user);
        inspectContainer.addValue(getWithIcon("Invulnerable", Icon.called("crosshairs").of(Color.BLUE).of(Family.SOLID)), invulnerable ? "Yes" : "No");
    }

    private void addNicknameData(User user, NucleusNicknameService nicknameService, InspectContainer inspectContainer) {
        Optional<Text> nickname = nicknameService.getNickname(user);

        if (nickname.isPresent()) {
            String nicknameString = HtmlUtils.swapColorsToSpan(TextSerializers.FORMATTING_CODE.serialize(nickname.get()));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Nickname", Icon.called("id-badge").of(Color.GREEN).of(Family.REGULAR)), nicknameString);
        }
    }

    /*
     * Server Data
     */
    private void addWarpData(NucleusWarpService warpService, AnalysisContainer analysisContainer) {
        List<Warp> warps = warpService.getAllWarps();
        analysisContainer.addValue(getWithIcon("Warp count", Icon.called("map-marker-alt").of(Color.BLUE)), warps.size());

        if (!warps.isEmpty()) {
            TableContainer warpsTable = new TableContainer(
                    getWithIcon("Name", Icon.called("map-marker-alt").of(Family.SOLID)),
                    getWithIcon("Description", Icon.called("sticky-note").of(Family.REGULAR)),
                    getWithIcon("Category", Icon.called("list").of(Family.SOLID))
            );

            for (Warp warp : warps) {
                String description = warp.getDescription().map(desc -> HtmlUtils.swapColorsToSpan(TextSerializers.FORMATTING_CODE.serialize(desc))).orElse("None");
                String category = warp.getCategory().orElse("None");

                warpsTable.addRow(warp.getName(), description, category);
            }

            analysisContainer.addTable("Warps", warpsTable);
        }
    }

    private void addJailData(NucleusJailService jailService, AnalysisContainer analysisContainer) {
        Map<String, NamedLocation> jails = jailService.getJails();
        analysisContainer.addValue(getWithIcon("Jail count", Icon.called("bars").of(Family.SOLID).of(Color.TEAL)), jails.size());

        if (!jails.isEmpty()) {
            TableContainer jailsTable = new TableContainer(getWithIcon("Jail", Icon.called("bars").of(Family.SOLID)));

            for (String jail : jails.keySet()) {
                jailsTable.addRow(jail);
            }

            analysisContainer.addTable("Jails", jailsTable);
        }
    }

    private void addKitData(NucleusKitService kitService, AnalysisContainer analysisContainer) {
        Set<String> kits = kitService.getKitNames();
        analysisContainer.addValue(getWithIcon("Kit count", Icon.called("box").of(Family.SOLID)), kits.size());

        if (!kits.isEmpty()) {
            TableContainer kitsTable = new TableContainer(getWithIcon("Kit", Icon.called("box").of(Family.SOLID)));

            for (String kit : kits) {
                kitsTable.addRow(kit);
            }

            analysisContainer.addTable("Kits", kitsTable);
        }
    }
}
