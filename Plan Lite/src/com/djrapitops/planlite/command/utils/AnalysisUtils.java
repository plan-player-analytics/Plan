package com.djrapitops.planlite.command.utils;

import java.util.ArrayList;
import java.util.List;

class AnalysisUtils {

    static String AmountAverage(List<String> dataPoints) {
        double sum = 0;

        for (String dataPoint : dataPoints) {
            try {
                sum += Double.parseDouble(dataPoint);

            } catch (Exception e) {

            }
        }
        return "" + (sum * 1.0 / dataPoints.size());
    }

    static List<String> parseWLetters(List<String> dataPoints) {
        List<String> parsed = new ArrayList<>();
        dataPoints.parallelStream().forEach((dataPoint) -> {
            parsed.add(DataFormatUtils.removeLetters(dataPoint));
        });
        return parsed;
    }

    static List<String> parseWMax(List<String> dataPoints) {
        List<String> parsed = new ArrayList<>();
        dataPoints.parallelStream().forEach((dataPoint) -> {
            parsed.add(dataPoint.split(" ")[0]);
        });
        return parsed;
    }

    static String TimeAverage(List<String> dataPoints) {
        Long time = Long.parseLong("0");
        for (String dataPoint : dataPoints) {
            try {
                time += Long.parseLong(dataPoint);
            } catch (Exception e) {
            }
        }
        return "" + (time / dataPoints.size());
    }

    static String BooleanPercent(List<String> dataPoints) {
        int amount = 0;
        for (String dataPoint : dataPoints) {
            try {
                if (Boolean.parseBoolean(dataPoint)) {
                    amount++;
                }
            } catch (Exception e) {
            }
        }
        return "" + ((amount * 1.0 / dataPoints.size())*100)+"%";
    }

    static String AmountHighest(List<String> dataPoints) {
        int highest = 0;
        for (String dataPoint : dataPoints) {
            try {
                int value = Integer.parseInt(dataPoint);
                if (value > highest) {
                    highest = value;
                }
            } catch (Exception e) {                
            }
        }
        return ""+highest;
    }

}
