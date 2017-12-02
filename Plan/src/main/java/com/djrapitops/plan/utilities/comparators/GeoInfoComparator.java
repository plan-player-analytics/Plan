package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.container.GeoInfo;

import java.util.Comparator;

/**
 * Comparator for comparing Actions so that latest is the first component.
 *
 * @author Rsl1122
 */
public class GeoInfoComparator implements Comparator<GeoInfo> {

    @Override
    public int compare(GeoInfo o1, GeoInfo o2) {
        return -Long.compare(o1.getLastUsed(), o2.getLastUsed());
    }

}
