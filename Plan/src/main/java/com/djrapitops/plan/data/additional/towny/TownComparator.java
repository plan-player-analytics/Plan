package main.java.com.djrapitops.plan.data.additional.towny;

import com.palmergames.bukkit.towny.object.Town;
import java.util.Comparator;

/**
 *
 * @author Rsl1122
 */
public class TownComparator implements Comparator<Town> {

    /**
     * Used to compare two Town objects.
     *
     * This method should only be used if TownyHook.isEnabled() returns true.
     *
     * Note: this comparator imposes orderings that are inconsistent with
     * equals.
     *
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
