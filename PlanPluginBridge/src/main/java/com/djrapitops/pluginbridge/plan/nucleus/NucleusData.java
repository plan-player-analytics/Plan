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

        if (NucleusAPI.getMuteService().isPresent()) {
            NucleusMuteService muteService = NucleusAPI.getMuteService().get();

            boolean muted = muteService.isMuted(user);
            inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), muted ? "Yes" : "No");

            if (muted && muteService.getPlayerMuteInfo(user).isPresent()) {
                MuteInfo muteInfo = muteService.getPlayerMuteInfo(user).get();

                String reason = HtmlUtils.swapColorsToSpan(muteInfo.getReason());
                String start = "Unknown";
                String end;
                String link = "Unknown";

                if (muteInfo.getMuter().isPresent()) {
                    User operatorUser = getUser(muteInfo.getMuter().get());
                    if (operatorUser != null) {
                        String operator = operatorUser.getName();
                        link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
                    }
                }

                if (muteInfo.getCreationInstant().isPresent()) {
                    start = FormatUtils.formatTimeStampYear(muteInfo.getCreationInstant().get().toEpochMilli());
                }

                if (muteInfo.getRemainingTime().isPresent()) {
                    end = FormatUtils.formatTimeStampYear(muteInfo.getRemainingTime().get().plusMillis(System.currentTimeMillis()).toMillis());
                } else {
                    end = "Permanent mute";
                }

                inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.DEEP_ORANGE)), link);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.DEEP_ORANGE).of(Family.REGULAR)), start);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.DEEP_ORANGE).of(Family.REGULAR)), end);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.DEEP_ORANGE).of(Family.REGULAR)), reason);
            }
        }

        if (NucleusAPI.getJailService().isPresent()) {
            NucleusJailService jailService = NucleusAPI.getJailService().get();

            boolean jailed = jailService.isPlayerJailed(user);
            inspectContainer.addValue(getWithIcon("Jailed", Icon.called("bars").of(Color.YELLOW).of(Family.SOLID)), jailed ? "Yes" : "No");

            if (jailed && jailService.getPlayerJailData(user).isPresent()) {
                Inmate inmate = jailService.getPlayerJailData(user).get();

                String reason = inmate.getReason();
                String start = "Unknown";
                String end;
                String link = "Unknown";

                if (inmate.getJailer().isPresent()) {
                    User operatorUser = getUser(inmate.getJailer().get());
                    if (operatorUser != null) {
                        String operator = operatorUser.getName();
                        link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
                    }
                }

                if (inmate.getCreationInstant().isPresent()) {
                    start = FormatUtils.formatTimeStampYear(inmate.getCreationInstant().get().toEpochMilli());
                }

                if (inmate.getRemainingTime().isPresent()) {
                    end = FormatUtils.formatTimeStampYear(inmate.getRemainingTime().get().plusMillis(System.currentTimeMillis()).toMillis());
                } else {
                    end = "Permanent jail sentence.";
                }

                inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.YELLOW)), link);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.YELLOW).of(Family.REGULAR)), start);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.YELLOW).of(Family.REGULAR)), end);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.YELLOW).of(Family.REGULAR)), reason);
                inspectContainer.addValue("&nbsp;" + getWithIcon("Jail", Icon.called("bars").of(Color.YELLOW).of(Family.SOLID)), inmate.getJailName());
            }
        }

        if (NucleusAPI.getHomeService().isPresent()) {
            NucleusHomeService homeService = NucleusAPI.getHomeService().get();

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

        if (NucleusAPI.getInvulnerabilityService().isPresent()) {
            boolean invulnerable = NucleusAPI.getInvulnerabilityService().get().isInvulnerable(user);

            inspectContainer.addValue(getWithIcon("Invulnerable", Icon.called("crosshairs").of(Color.BLUE).of(Family.SOLID)), invulnerable ? "Yes" : "No");
        }

        if (NucleusAPI.getNicknameService().isPresent()) {
            Optional<Text> nickname = NucleusAPI.getNicknameService().get().getNickname(user);

            if (nickname.isPresent()) {
                String nicknameString = HtmlUtils.swapColorsToSpan(TextSerializers.FORMATTING_CODE.serialize(nickname.get()));
                inspectContainer.addValue("&nbsp;" + getWithIcon("Nickname", Icon.called("id-badge").of(Color.GREEN).of(Family.REGULAR)), nicknameString);
            }
        }

        if (NucleusAPI.getNoteService().isPresent()) {
            NucleusNoteService noteService = NucleusAPI.getNoteService().get();

            List<Note> notes = noteService.getNotes(user);
            if (!notes.isEmpty()) {
                TableContainer notesTable = new TableContainer(
                        getWithIcon("Noter", Icon.called("pen").of(Family.SOLID)),
                        getWithIcon("Note", Icon.called("sticky-note").of(Family.REGULAR))
                );

                notesTable.setColor("light-blue");

                for (Note note : notes) {
                    String noter = "Unknown";
                    if (note.getNoter().isPresent()) {
                        User noterUser = getUser(note.getNoter().get());
                        if (noterUser != null) {
                            noter = noterUser.getName();
                        }
                    }

                    notesTable.addRow(noter, note.getNote());
                }

                inspectContainer.addTable("Notes", notesTable);
            }
        }

        if (NucleusAPI.getWarningService().isPresent()) {
            NucleusWarningService warningService = NucleusAPI.getWarningService().get();

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
                    if (warning.getWarner().isPresent()) {
                        User warnerUser = getUser(warning.getWarner().get());
                        if (warnerUser != null) {
                            warner = warnerUser.getName();
                        }
                    }

                    warningsTable.addRow(warner, warning.getReason());
                }

                inspectContainer.addTable("Warnings", warningsTable);
            }
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        if (NucleusAPI.getWarpService().isPresent()) {
            NucleusWarpService warpService = NucleusAPI.getWarpService().get();

            List<Warp> warps = warpService.getAllWarps();
            analysisContainer.addValue(getWithIcon("Warp count", Icon.called("map-marker-alt").of(Color.BLUE)), warps.size());

            if (!warps.isEmpty()) {
                TableContainer warpsTable = new TableContainer(
                    getWithIcon("Name", Icon.called("map-marker-alt").of(Family.SOLID)),
                    getWithIcon("Description", Icon.called("sticky-note").of(Family.REGULAR)),
                    getWithIcon("Category", Icon.called("list").of(Family.SOLID))
                );

                for (Warp warp : warps) {
                    String description = "None";
                    String category = "None";

                    if (warp.getDescription().isPresent()) {
                        description = HtmlUtils.swapColorsToSpan(TextSerializers.FORMATTING_CODE.serialize(warp.getDescription().get()));
                    }

                    if (warp.getCategory().isPresent()) {
                        category = warp.getCategory().get();
                    }

                    warpsTable.addRow(warp.getName(), description, category);
                }

                analysisContainer.addTable("Warps", warpsTable);
            }
        }

        if (NucleusAPI.getJailService().isPresent()) {
            NucleusJailService jailService = NucleusAPI.getJailService().get();

            Map<String, NamedLocation> jails = jailService.getJails();
            analysisContainer.addValue(getWithIcon("Jail count", Icon.called("bars").of(Family.SOLID).of(Color.TEAL)), jails.size());

            if (!jailService.getJails().isEmpty()) {
                TableContainer jailsTable = new TableContainer(getWithIcon("Jail", Icon.called("bars").of(Family.SOLID)));

                for (Map.Entry<String, NamedLocation> jail : jails.entrySet()) {
                    jailsTable.addRow(jail.getKey());
                }

                analysisContainer.addTable("Jails", jailsTable);
            }
        }

        if (NucleusAPI.getKitService().isPresent()) {
            NucleusKitService kitService = NucleusAPI.getKitService().get();

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
}
