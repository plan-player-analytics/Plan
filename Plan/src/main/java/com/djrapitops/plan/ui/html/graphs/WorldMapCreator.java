package main.java.com.djrapitops.plan.ui.html.graphs;

import java.util.Map;

public class WorldMapCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private WorldMapCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a data series with iso-a3 specification of Country codes.
     *
     * @param geoCodeCounts The country codes and the amount of players located in the country
     * @return The created data series
     */
    public static String createDataSeries(Map<String, Integer> geoCodeCounts) {
        StringBuilder arrayBuilder = new StringBuilder("[");

        int i = 0;
        int size = geoCodeCounts.size();
        for (Map.Entry<String, Integer> entry : geoCodeCounts.entrySet()) {
            String geoCode = entry.getKey();
            Integer players = entry.getValue();

            if (players != 0) {
                arrayBuilder.append("{'code':'").append(geoCode).append("','value':").append(players).append("}");
                if (i < size - 1) {
                    arrayBuilder.append(",");
                }
            }

            i++;
        }

        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }
}
