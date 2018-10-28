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
package com.djrapitops.pluginbridge.plan.advancedban;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

import java.util.Collection;
import java.util.UUID;

/**
 * PluginData for AdvancedBan plugin.
 *
 * @author Vankka
 */
class AdvancedBanData extends PluginData {

    private final Formatter<Long> timestampFormatter;

    AdvancedBanData(
            Formatter<Long> timestampFormatter
    ) {
        super(ContainerSize.THIRD, "AdvancedBan");
        this.timestampFormatter = timestampFormatter;
        setPluginIcon(Icons.BANNED);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String abUuid = uuid.toString().replace("-", "");

        if (uuid.version() == 3) { // Cracked / Offline UUID
            return inspectContainer;
        }

        PunishmentManager punishmentManager = PunishmentManager.get();
        Punishment ban = punishmentManager.getBan(abUuid);
        Punishment mute = punishmentManager.getMute(abUuid);
        long warnings = punishmentManager.getWarns(abUuid).stream().filter(warning -> !warning.isExpired()).count();

        inspectContainer.addValue(getWithIcon("Banned", Icons.BANNED), ban != null ? "Yes" : "No");
        if (ban != null) {
            addPunishment(inspectContainer, ban, "Permanent ban");
        }

        inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), mute != null ? "Yes" : "No");
        if (mute != null) {
            addPunishment(inspectContainer, mute, "Permanent mute");
        }

        inspectContainer.addValue(getWithIcon("Warnings", Icon.called("flag").of(Color.YELLOW)), warnings);

        return inspectContainer;
    }

    private void addPunishment(InspectContainer inspectContainer, Punishment punishment, String identifier) {
        String operator = punishment.getOperator();
        String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
        String reason = HtmlUtils.swapColorsToSpan(punishment.getReason());
        String start = timestampFormatter.apply(punishment.getStart());
        String end = timestampFormatter.apply(punishment.getEnd());

        PunishmentType type = punishment.getType();
        // Permanent
        if (type == PunishmentType.BAN
                || type == PunishmentType.IP_BAN
                || type == PunishmentType.MUTE
        ) {
            end = identifier;
        }

        if (operator.equals("CONSOLE")) {
            link = "CONSOLE";
        }

        inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.RED)), link);
        inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.RED).of(Family.REGULAR)), start);
        inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.RED).of(Family.REGULAR)), end);
        inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.RED).of(Family.REGULAR)), reason);
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        return analysisContainer;
    }
}
