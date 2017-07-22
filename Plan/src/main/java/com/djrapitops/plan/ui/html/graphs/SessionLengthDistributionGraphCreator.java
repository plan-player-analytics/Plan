/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.ui.html.graphs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

/**
 *
 * @author Rsl1122
 */
public class SessionLengthDistributionGraphCreator {

    /**
     *
     * @param data
     * @return
     */
    public static String[] generateDataArraySessions(Collection<SessionData> data) {
        List<Long> lengths = AnalysisUtils.transformSessionDataToLengths(data);
        return generateDataArray(lengths);
    }

    /**
     *
     * @param lengths
     * @return
     */
    public static String[] generateDataArray(Collection<Long> lengths) {
        Map<Long, Integer> values = getValues(lengths);
//        Map<Long, Integer> scaled = scale(values);
        StringBuilder arrayBuilder = buildString(values);
        StringBuilder labelBuilder = buildLabels(values);
        return new String[]{arrayBuilder.toString(), labelBuilder.toString()};
    }

    private static StringBuilder buildString(Map<Long, Integer> scaled) {
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append("[");

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
        StringBuilder arrayBuilder = new StringBuilder();
        arrayBuilder.append("[");

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
        while (!unused.isEmpty() && lengthInMinutes < 120) {
            long length = lengthInMinutes * 60 * 1000;
            List<Long> lessThan = unused.stream().filter(l -> l < length).collect(Collectors.toList());
            values.put(lengthInMinutes, lessThan.size());
            unused.removeAll(lessThan);
            lengthInMinutes += 5;
        }
        return values;
    }

    private static Map<Long, Integer> scale(Map<Long, Integer> values) {
        Map<Long, Integer> scaled = new HashMap<>();
        int total = MathUtils.sumInt(values.values().stream().map(i -> (Serializable) i));
        for (long key : values.keySet()) {
            scaled.put(key, (int) ((1.0 * values.get(key) / total) * 100));
        }
        return scaled;
    }
}
