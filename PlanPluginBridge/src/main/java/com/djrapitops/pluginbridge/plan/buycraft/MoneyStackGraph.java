/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.buycraft;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.html.graphs.stack.AbstractStackGraph;
import com.djrapitops.plan.utilities.html.graphs.stack.StackDataSet;
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
public class MoneyStackGraph {

    private final AbstractStackGraph stackGraph;

    private MoneyStackGraph(AbstractStackGraph stackGraph) {
        this.stackGraph = stackGraph;
    }

    public static MoneyStackGraph create(List<Payment> payments) {
        long now = MiscUtils.getTime();
        long oldestDate = payments.isEmpty() ? now : payments.get(payments.size() - 1).getDate();

        String[] labels = getLabels(now, oldestDate);
        Map<String, List<Payment>> stacks = getStacks(payments);

        StackDataSet[] dataSets = getDataSets(labels, stacks);

        return new MoneyStackGraph(new AbstractStackGraph(labels, dataSets));
    }

    private static StackDataSet[] getDataSets(String[] labels, Map<String, List<Payment>> stacks) {
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

        return stackDataSets.toArray(new StackDataSet[stackDataSets.size()]);
    }

    private static List<Double> sortValuesByLabels(String[] labels, Map<String, Double> valueMap) {
        List<Double> values = new ArrayList<>();
        for (String label : labels) {
            values.add(valueMap.getOrDefault(label, 0.0));
        }
        return values;
    }

    private static Map<String, Double> getValueMap(List<Payment> payments) {
        Map<String, Double> valueMap = new HashMap<>();
        for (Payment payment : payments) {
            String label = getLabel(payment.getDate());
            Double value = valueMap.getOrDefault(label, 0.0);
            valueMap.put(label, value + payment.getAmount());
        }
        return valueMap;
    }

    private static Map<String, List<Payment>> getStacks(List<Payment> payments) {
        Map<String, List<Payment>> stacks = new HashMap<>();
        for (Payment payment : payments) {
            String currency = payment.getCurrency();

            List<Payment> dataSetPayments = stacks.getOrDefault(currency, new ArrayList<>());
            dataSetPayments.add(payment);
            stacks.put(currency, dataSetPayments);
        }
        return stacks;
    }

    private static String[] getLabels(long now, long oldestDate) {
        long oneYearAgo = now - TimeAmount.YEAR.ms();

        long leftLimit = Math.max(oldestDate, oneYearAgo);

        List<String> labels = new ArrayList<>();
        for (long time = leftLimit; time < now; time += TimeAmount.MONTH.ms()) {
            labels.add(getLabel(time));
        }

        return labels.toArray(new String[labels.size()]);
    }

    private static String getLabel(long time) {
        String locale = Settings.LOCALE.toString();
        Locale usedLocale = locale.equalsIgnoreCase("default") ? Locale.ENGLISH : Locale.forLanguageTag(locale);
        ZoneId usedTimeZone = Settings.USE_SERVER_TIME.isTrue() ? ZoneId.systemDefault() : ZoneOffset.UTC;

        LocalDate date = Instant.ofEpochMilli(time).atZone(usedTimeZone).toLocalDate();
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