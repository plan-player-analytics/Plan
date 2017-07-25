package com.djrapitops.pluginbridge.plan.factions;

import com.massivecraft.factions.entity.Faction;
import java.util.Comparator;

/**
 * This class is used to compare factions in terms of Power.
 *
 * Compare method should only be used if FactionsHook.isEnabled() returns true.
 *
 * Note: this comparator imposes orderings that are inconsistent with equals.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see FactionsHook
 */
public class FactionComparator implements Comparator<Faction> {

    @Override
    public int compare(Faction fac1, Faction fac2) {
        return -Double.compare(fac1.getPower(), fac2.getPower());
    }
}
