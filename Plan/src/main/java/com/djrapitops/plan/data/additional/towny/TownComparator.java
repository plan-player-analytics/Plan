package main.java.com.djrapitops.plan.data.additional.towny;

import com.palmergames.bukkit.towny.object.Town;
import java.util.Comparator;

/**
 * This class is used to compare towns in terms of Amount of residents.
 *
 * Compare method should only be used if TownyHook.isEnabled() returns true.
 *
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see TownyHook
 */
public class TownComparator implements Comparator<Town> {

    /**
     * Used to compare two Town objects.
     *
     * This method should only be used if TownyHook.isEnabled() returns true.
     *
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     * @param tow1 Town 1
     * @param tow2 Town 2
     */
    @Override
    public int compare(Town tow1, Town tow2) {
        if (tow1.equals(tow2)) {
            return 0;
        }
        int tow1res = tow1.getNumResidents();
        int tow2res = tow2.getNumResidents();
        if (tow1res == tow2res) {
            return 0;
        } else if (tow1res > tow2res) {
            return 1;
        }
        return -1;
    }
}
