/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.exceptions.connection.ForbiddenException;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for BuyCraft plugin.
 *
 * @author Rsl1122
 */
public class BuyCraftPluginData extends PluginData {

    private final String secret;

    public BuyCraftPluginData(String secret) {
        super(ContainerSize.TWO_THIRDS, "BuyCraft");
        super.setIconColor("blue");
        super.setPluginIcon("shopping-bag");

        this.secret = secret;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        try {

            List<Payment> payments = new ListPaymentRequest(secret).makeRequest();
            TableContainer payTable = new TableContainer(true, getWithIcon("Date", "calendar"), getWithIcon("Donation", "money"));
            payTable.setColor("blue");

            for (Payment payment : payments) {
                String name = payment.getPlayerName();
                payTable.addRow(
                        Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                        FormatUtils.formatTimeStampYear(payment.getDate()),
                        FormatUtils.cutDecimals(payment.getAmount()) + payment.getCurrency()
                );
            }

            analysisContainer.addTable("payTable", payTable);

            Map<UUID, String> playerTableValues = payments.stream()
                    .collect(Collectors.toMap(Payment::getUuid, payment -> payment.getAmount() + payment.getCurrency()));
            analysisContainer.addPlayerTableValues(getWithIcon("Donation", "money"), playerTableValues);

        } catch (IllegalStateException | NullPointerException e) {
            analysisContainer.addValue("JSON error", e.getMessage());
        } catch (ForbiddenException e) {
            analysisContainer.addValue("Configuration error", e.getMessage());
        }
        return analysisContainer;
    }
}