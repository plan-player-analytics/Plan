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
     * @param geoCodeCounts
     * @return
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

    public static String[] choroplethMapValues(Map<String, Integer> geoLocations, Map<String, String> geoCodes) {
        StringBuilder locations = new StringBuilder("[");
        StringBuilder z = new StringBuilder("[");
        StringBuilder text = new StringBuilder("[");

        int i = 0;
        int size = geoLocations.size();
        for (Map.Entry<String, Integer> entrySet : geoLocations.entrySet()) {
            String country = entrySet.getKey();
            String code = geoCodes.getOrDefault(country, "UNK");
            int amount = entrySet.getValue();

            z.append("\"").append(amount).append("\"");
            locations.append("\"").append(country).append("\"");
            text.append("\"").append(code).append("\"");

            if (i < size - 1) {
                locations.append(",");
                z.append(",");
                text.append(",");
            }
        }

        locations.append("]");
        z.append("]");
        text.append("]");

        return new String[]{z.toString(), locations.toString(), text.toString()};
    }
}
