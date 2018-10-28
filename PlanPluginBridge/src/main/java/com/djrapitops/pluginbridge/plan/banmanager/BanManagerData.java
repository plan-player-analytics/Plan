/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan.banmanager;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.BanData;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
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
class BanManagerData extends PluginData implements BanData {

    private final Formatter<Long> timestampFormatter;
    
    BanManagerData(Formatter<Long> timestampFormatter) {
        super(ContainerSize.THIRD, "BanManager");
        this.timestampFormatter = timestampFormatter;
        setPluginIcon(Icons.BANNED);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        boolean banned = BmAPI.isBanned(uuid);
        boolean muted = BmAPI.isMuted(uuid);

        inspectContainer.addValue(getWithIcon("Banned", Icons.BANNED), banned ? "Yes" : "No");
        if (banned) {
            addBanInformation(uuid, inspectContainer);
        }

        inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), muted ? "Yes" : "No");
        if (muted) {
            addMuteInformation(uuid, inspectContainer);
        }

        return inspectContainer;
    }

    private void addBanInformation(UUID uuid, InspectContainer inspectContainer) {
        PlayerBanData currentBan = BmAPI.getCurrentBan(uuid);
        String bannedBy = currentBan.getActor().getName();
        String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(bannedBy), bannedBy);
        long date = currentBan.getCreated();
        long ends = currentBan.getExpires();
        String reason = currentBan.getReason();

        inspectContainer.addValue("&nbsp;" + getWithIcon("Banned by", Icon.called("user").of(Color.RED)), link);
        inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.RED).of(Family.REGULAR)), timestampFormatter.apply(date));
        inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.RED).of(Family.REGULAR)), timestampFormatter.apply(ends));
        inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.RED).of(Family.REGULAR)), reason);
    }

    private void addMuteInformation(UUID uuid, InspectContainer inspectContainer) {
        PlayerMuteData currentMute = BmAPI.getCurrentMute(uuid);
        String mutedBy = currentMute.getActor().getName();
        String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(mutedBy), mutedBy);
        long date = currentMute.getCreated();
        long ends = currentMute.getExpires();
        String reason = currentMute.getReason();

        inspectContainer.addValue("&nbsp;" + getWithIcon("Muted by", Icon.called("user").of(Color.DEEP_ORANGE)), link);
        inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.DEEP_ORANGE).of(Family.REGULAR)), timestampFormatter.apply(date));
        inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.DEEP_ORANGE).of(Family.REGULAR)), timestampFormatter.apply(ends));
        inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.DEEP_ORANGE).of(Family.REGULAR)), reason);
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
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