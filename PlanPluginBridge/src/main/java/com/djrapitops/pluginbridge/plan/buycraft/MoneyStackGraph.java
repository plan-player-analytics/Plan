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
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.PluginSettings;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.html.graphs.stack.StackDataSet;
import com.djrapitops.plan.utilities.html.graphs.stack.StackGraph;
import com.djrapitops.plugin.api.TimeAmount;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Utility for creating Money Stack Graph.
 *
 * @author Rsl1122
 */
class MoneyStackGraph {

    private final StackGraph stackGraph;
    private final ZoneId timeZoneID;
    private String locale;

    MoneyStackGraph(List<Payment> payments, PlanConfig config) {
        timeZoneID = config.get(TimeSettings.USE_SERVER_TIME) ? ZoneId.systemDefault() : ZoneOffset.UTC;
        locale = config.get(PluginSettings.LOCALE);

        long now = System.currentTimeMillis();
        long oldestDate = payments.isEmpty() ? now : payments.get(payments.size() - 1).getDate();

        String[] labels = getLabels(now, oldestDate);
        Map<String, List<Payment>> stacks = getStacks(payments);

        StackDataSet[] dataSets = getDataSets(labels, stacks);

        this.stackGraph = new StackGraph(labels, dataSets);
    }

    private StackDataSet[] getDataSets(String[] labels, Map<String, List<Payment>> stacks) {
        String[] colors = ThemeVal.GRAPH_GM_PIE.getDefaultValue().split(", ");
        int maxCol = colors.length;

        List<StackDataSet> stackDataSets = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, List<Payment>> entry : stacks.entrySet()) {
            String currency = entry.getKey();
            List<Payment> payments = entry.getValue();

            List<Double> values = sortValuesByLabels(labels, getValueMap(payments));

            String color = colors[(i) % maxCol];

            stackDataSets.add(new StackDataSet(values, currency, color));

            i++;
        }

        return stackDataSets.toArray(new StackDataSet[0]);
    }

    private List<Double> sortValuesByLabels(String[] labels, Map<String, Double> valueMap) {
        List<Double> values = new ArrayList<>();
        for (String label : labels) {
            values.add(valueMap.getOrDefault(label, 0.0));
        }
        return values;
    }

    private Map<String, Double> getValueMap(List<Payment> payments) {
        Map<String, Double> valueMap = new HashMap<>();
        for (Payment payment : payments) {
            String label = getLabel(payment.getDate());
            Double value = valueMap.getOrDefault(label, 0.0);
            valueMap.put(label, value + payment.getAmount());
        }
        return valueMap;
    }

    private Map<String, List<Payment>> getStacks(List<Payment> payments) {
        Map<String, List<Payment>> stacks = new HashMap<>();
        for (Payment payment : payments) {
            String currency = payment.getCurrency();

            List<Payment> dataSetPayments = stacks.getOrDefault(currency, new ArrayList<>());
            dataSetPayments.add(payment);
            stacks.put(currency, dataSetPayments);
        }
        return stacks;
    }

    private String[] getLabels(long now, long oldestDate) {
        long oneYearAgo = now - TimeAmount.YEAR.toMillis(1L);

        long leftLimit = Math.max(oldestDate, oneYearAgo);

        List<String> labels = new ArrayList<>();
        for (long time = leftLimit; time < now; time += TimeAmount.MONTH.toMillis(1L)) {
            labels.add(getLabel(time));
        }

        return labels.toArray(new String[0]);
    }

    private String getLabel(long time) {
        Locale usedLocale = locale.equalsIgnoreCase("default") ? Locale.ENGLISH : Locale.forLanguageTag(locale);

        LocalDate date = Instant.ofEpochMilli(time).atZone(timeZoneID).toLocalDate();
        String month = date.getMonth().getDisplayName(TextStyle.FULL, usedLocale);
        int year = date.getYear();
        return month + " " + year;
    }

    public String toHighChartsLabels() {
        return stackGraph.toHighChartsLabels();
    }

    public String toHighChartsSeries() {
        return stackGraph.toHighChartsSeries();
    }
}