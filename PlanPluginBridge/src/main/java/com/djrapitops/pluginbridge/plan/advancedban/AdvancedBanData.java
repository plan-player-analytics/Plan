/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.advancedban;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plan.utilities.html.icon.Icons;
import com.djrapitops.plugin.api.utility.log.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.PunishmentType;

/**
 * PluginData for AdvancedBan plugin.
 *
 * @author Vankka
 */
public class AdvancedBanData extends PluginData {
    public AdvancedBanData() {
        super(ContainerSize.THIRD, "AdvancedBan");
        setPluginIcon(Icons.BANNED);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String abUuid = uuid.toString().replace("-", "");

        if (uuid.version() == 3) { // Cracked / Offline UUID
            return inspectContainer;
        }

        Punishment ban = PunishmentManager.get().getBan(abUuid);
        Punishment mute = PunishmentManager.get().getMute(abUuid);
        long warnings = PunishmentManager.get().getWarns(abUuid).stream().filter(warning -> !warning.isExpired()).count();

        inspectContainer.addValue(getWithIcon("Banned", Icons.BANNED), ban != null ? "Yes" : "No");
        inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), mute != null  ? "Yes" : "No");
        inspectContainer.addValue(getWithIcon("Warnings", Icon.called("flag").of(Color.YELLOW)), warnings);

        if (ban != null) {
            String operator = ban.getOperator();
            String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
            String reason = HtmlUtils.swapColorsToSpan(ban.getReason());
            long start = ban.getStart();
            String end = FormatUtils.formatTimeStampYear(ban.getEnd());

            if (ban.getType() == PunishmentType.BAN || ban.getType() == PunishmentType.IP_BAN) { // Permanent
                end = "Permanent ban";
            }

            if (operator.equals("CONSOLE")) {
                link = "CONSOLE";
            }

            inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.RED)), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.RED).of(Family.REGULAR)), FormatUtils.formatTimeStampYear(start));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.RED).of(Family.REGULAR)), end);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.RED).of(Family.REGULAR)), reason);
        }

        if (mute != null) {
            String operator = mute.getOperator();
            String link = Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(operator), operator);
            String reason = HtmlUtils.swapColorsToSpan(mute.getReason());
            long start = mute.getStart();
            String end = FormatUtils.formatTimeStampYear(mute.getEnd());

            if (mute.getType() == PunishmentType.MUTE) { // Permanent
                end = "Permanent mute";
            }

            if (operator.equals("CONSOLE")) {
                link = "CONSOLE";
            }

            inspectContainer.addValue("&nbsp;" + getWithIcon("Operator", Icon.called("user").of(Color.DEEP_ORANGE)), link);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Date", Icon.called("calendar").of(Color.DEEP_ORANGE).of(Family.REGULAR)), FormatUtils.formatTimeStampYear(start));
            inspectContainer.addValue("&nbsp;" + getWithIcon("Ends", Icon.called("calendar-check").of(Color.DEEP_ORANGE).of(Family.REGULAR)), end);
            inspectContainer.addValue("&nbsp;" + getWithIcon("Reason", Icon.called("comment").of(Color.DEEP_ORANGE).of(Family.REGULAR)), reason);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        return analysisContainer;
    }
}
