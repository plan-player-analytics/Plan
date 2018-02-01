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

import java.util.*;

/**
 * PluginData for BuyCraft plugin.
 *
 * @author Rsl1122
 */
public class BuyCraftData extends PluginData {

    private final String secret;

    public BuyCraftData(String secret) {
        super(ContainerSize.TAB, "BuyCraft");
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
            Collections.sort(payments);

            addPaymentTotals(analysisContainer, payments);
            addPlayerTable(analysisContainer, payments);

        } catch (ForbiddenException e) {
            analysisContainer.addValue("Configuration error", e.getMessage());
        }
        return analysisContainer;
    }

    private void addPlayerTable(AnalysisContainer analysisContainer, List<Payment> payments) {
        TableContainer payTable = new TableContainer(
                true,
                getWithIcon("Date", "calendar"),
                getWithIcon("Amount", "money"),
                getWithIcon("Packages", "cube")
        );
        payTable.setColor("blue");
        for (Payment payment : payments) {
            String name = payment.getPlayerName();
            payTable.addRow(
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                    FormatUtils.formatTimeStampYear(payment.getDate()),
                    FormatUtils.cutDecimals(payment.getAmount()) + " " + payment.getCurrency(),
                    payment.getPackages()
            );
        }
        analysisContainer.addTable("payTable", payTable);

        MoneyStackGraph moneyStackGraph = MoneyStackGraph.create(payments);
        String graphHtml = Html.PANEL_BODY.parse("<div id=\"buycraftChart\" class=\"dashboard-flot-chart\"></div>") +
                "<script>$(function () {setTimeout(function() {" +
                "stackChart('buycraftChart', "
                + moneyStackGraph.toHighChartsLabels() + ", "
                + moneyStackGraph.toHighChartsSeries() + ", '');}, 1000)});</script>";

        analysisContainer.addHtml("moneygraph", graphHtml);
    }

    private void addPaymentTotals(AnalysisContainer analysisContainer, List<Payment> payments) {
        Map<String, Double> paymentTotals = new HashMap<>();
        for (Payment payment : payments) {
            String currency = payment.getCurrency();
            double amount = payment.getAmount();
            paymentTotals.put(currency, paymentTotals.getOrDefault(currency, 0.0) + amount);
        }
        for (Map.Entry<String, Double> entry : paymentTotals.entrySet()) {
            analysisContainer.addValue(getWithIcon("Total " + entry.getKey(), "money", "blue"), FormatUtils.cutDecimals(entry.getValue()));
        }
    }
}