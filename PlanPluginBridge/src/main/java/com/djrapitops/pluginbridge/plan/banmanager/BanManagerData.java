/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.banmanager;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.*;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;
import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerMuteData;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for BanManager plugin.
 *
 * @author Rsl1122
 */
public class BanManagerData extends PluginData implements BanData {

    public BanManagerData() {
        super(ContainerSize.THIRD, "BanManager");
        super.setIconColor("red");
        super.setPluginIcon("gavel");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        boolean banned = BmAPI.isBanned(uuid);
        boolean muted = BmAPI.isMuted(uuid);

        inspectContainer.addValue(getWithIcon("Banned", "gavel", "red"), banned ? "Yes" : "No");

        if (banned) {
            PlayerBanData currentBan = BmAPI.getCurrentBan(uuid);
            String bannedBy = currentBan.getActor().getName();
            String link = Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(bannedBy), bannedBy);
            long date = currentBan.getCreated();
            long ends = currentBan.getExpires();
            String reason = currentBan.getReason();

            inspectContainer.addValue("&nbsp;" + getWithIcon("Banned by", "user", "red"), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", "calendar", "red"), FormatUtils.formatTimeStampYear(date));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", "calendar-check-o", "red"), FormatUtils.formatTimeStampYear(ends));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", "comment", "red"), reason);
        }
        inspectContainer.addValue(getWithIcon("Muted", "bell-slash-o", "deep-orange"), muted ? "Yes" : "No");
        if (muted) {
            PlayerMuteData currentMute = BmAPI.getCurrentMute(uuid);
            String mutedBy = currentMute.getActor().getName();
            String link = Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(mutedBy), mutedBy);
            long date = currentMute.getCreated();
            long ends = currentMute.getExpires();
            String reason = currentMute.getReason();

            inspectContainer.addValue("&nbsp;" + getWithIcon("Muted by", "user", "deep-orange"), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", "calendar", "deep-orange"), FormatUtils.formatTimeStampYear(date));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", "calendar-check-o", "deep-orange"), FormatUtils.formatTimeStampYear(ends));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", "comment", "deep-orange"), reason);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        return analysisContainer;
    }

    @Override
    public boolean isBanned(UUID uuid) {
        return BmAPI.isBanned(uuid);
    }

    @Override
    public Collection<UUID> filterBanned(Collection<UUID> collection) {
        return collection.stream().filter(BmAPI::isBanned).collect(Collectors.toList());
    }
}