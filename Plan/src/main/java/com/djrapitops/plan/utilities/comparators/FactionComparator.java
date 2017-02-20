package main.java.com.djrapitops.plan.utilities.comparators;

import com.massivecraft.factions.entity.Faction;
import java.util.Comparator;

/**
 *
 * @author Rsl1122
 */
public class FactionComparator implements Comparator<Faction> {

    // This method should only be used if FactionsHook.isEnabled() returns true.
    // Note: this comparator imposes orderings that are inconsistent with equals.
    @Override
    public int compare(Faction fac1, Faction fac2) {
        if (fac1.getPower() > fac2.getPower()) {
            return 1;
        }
        return -1;
    }
}
