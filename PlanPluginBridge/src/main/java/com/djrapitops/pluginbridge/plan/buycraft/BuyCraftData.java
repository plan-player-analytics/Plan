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
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.*;

/**
 * PluginData for BuyCraft plugin.
 *
 * @author Rsl1122
 */
class BuyCraftData extends PluginData {

    private final String secret;

    private final PlanConfig config;
    private final Formatter<Long> timestampFormatter;
    private final Formatter<Double> decimalFormatter;

    BuyCraftData(
            String secret,
            PlanConfig config, Formatter<Long> timestampFormatter,
            Formatter<Double> decimalFormatter
    ) {
        super(ContainerSize.TAB, "BuyCraft");
        this.config = config;
        this.timestampFormatter = timestampFormatter;
        this.decimalFormatter = decimalFormatter;
        setPluginIcon(Icon.called("shopping-bag").of(Color.BLUE).build());

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
                getWithIcon("Date", Icon.called("calendar").of(Family.REGULAR)),
                getWithIcon("Amount", Icon.called("money-bill-wave")),
                getWithIcon("Packages", Icon.called("cube"))
        );
        payTable.setColor("blue");
        for (Payment payment : payments) {
            String name = payment.getPlayerName();
            payTable.addRow(
                    Html.LINK.parse(PlanAPI.getInstance().getPlayerInspectPageLink(name), name),
                    timestampFormatter.apply(payment.getDate()),
                    decimalFormatter.apply(payment.getAmount()) + " " + payment.getCurrency(),
                    payment.getPackages()
            );
        }
        analysisContainer.addTable("payTable", payTable);

        MoneyStackGraph moneyStackGraph = new MoneyStackGraph(payments, config);
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
            analysisContainer.addValue(
                    getWithIcon("Total " + entry.getKey(), Icon.called("money-bill-wave").of(Color.BLUE)),
                    decimalFormatter.apply(entry.getValue())
            );
        }
    }
}