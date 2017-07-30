/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class SessionLengthDistributionGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private SessionLengthDistributionGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a data series of session lengths for Sessions bar chart.
     * <p>
     * Contains values from 0 up to 120 minutes
     *
     * @param lengths Lengths of all sessions in a list.
     * @return Data for Highcharts series.
     */
    public static String createDataSeries(List<Long> lengths) {
        Map<Long, Integer> bars = getValues(lengths);
        List<Long> keys = new ArrayList<>(bars.keySet());
        Collections.sort(keys);

        StringBuilder arrayBuilder = new StringBuilder("[");
        int i = 0;
        int size = keys.size();
        for (Long key : keys) {
            if (key > 120) {
                break;
            }
            Integer value = bars.get(key);
            arrayBuilder.append("['").append(key - 5).append(" - ").append(key).append(" min',").append(value).append("]");
            if (i < size) {
                arrayBuilder.append(", ");
            }
            i++;
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }

    /**
     * @param data
     * @return
     */
    public static String[] generateDataArraySessions(Collection<SessionData> data) {
        List<Long> lengths = AnalysisUtils.transformSessionDataToLengths(data);
        return generateDataArray(lengths);
    }

    /**
     * @param lengths
     * @return
     */
    public static String[] generateDataArray(Collection<Long> lengths) {
        Map<Long, Integer> values = getValues(lengths);
        StringBuilder arrayBuilder = buildString(values);
        StringBuilder labelBuilder = buildLabels(values);

        return new String[]{arrayBuilder.toString(), labelBuilder.toString()};
    }

    private static StringBuilder buildString(Map<Long, Integer> scaled) {
        StringBuilder arrayBuilder = new StringBuilder("[");

        long big = MathUtils.getBiggestLong(scaled.keySet());
        for (long key = 0; key <= big; key++) {
            Integer value = scaled.get(key);
            if (value == null) {
                continue;
            }
            arrayBuilder.append(value);
            if (key != big) {
                arrayBuilder.append(", ");
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder;
    }

    private static StringBuilder buildLabels(Map<Long, Integer> scaled) {
        StringBuilder arrayBuilder = new StringBuilder("[");

        long big = MathUtils.getBiggestLong(scaled.keySet());
        for (long key = 0; key <= big; key++) {
            Integer value = scaled.get(key);
            if (value == null) {
                continue;
            }
            arrayBuilder.append("\'").append(key - 5).append(" - ").append(key).append(" min").append("\'");
            if (key != big) {
                arrayBuilder.append(", ");
            }
        }
        arrayBuilder.append("]");
        return arrayBuilder;
    }

    private static Map<Long, Integer> getValues(Collection<Long> lengths) {
        List<Long> unused = new ArrayList<>(lengths);
        Map<Long, Integer> values = new HashMap<>();
        long lengthInMinutes = 5;

        while (!unused.isEmpty() || lengthInMinutes <= 120) {
            long length = lengthInMinutes * 60 * 1000;
            List<Long> lessThan = unused.stream().filter(l -> l < length).collect(Collectors.toList());
            int amount = lessThan.size();
            if (amount != 0) {
                values.put(lengthInMinutes, amount);
                unused.removeAll(lessThan);
            }
            lengthInMinutes += 5;
        }
        return values;
    }
}
