package com.djrapitops.pluginbridge.plan.towny;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.additional.PluginData;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for Towny-plugin.
 *
 * Registered to the plugin by TownyHook
 *
 * Gives Town name as String.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see TownyHook
 */
public class TownyTown extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public TownyTown() {
        super("Towny", "town");
        super.setAnalysisOnly(false);
        super.setIcon("bank");
        super.setPrefix("Town: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return parseContainer(modifierPrefix, "Not in a Town");
        }
        String name = data.getName();
        try {
            Resident res = TownyUniverse.getDataSource().getResident(name);
            String town;
            if (res.hasTown()) {
                town = res.getTown().getName();
            } else {
                town = "Not in a Town";
            }
            return parseContainer("", town);
        } catch (NotRegisteredException ex) {
            return parseContainer(modifierPrefix, "Not in a Town");
        }
    }

    @Override
    public Serializable getValue(UUID uuid) {
        UserData data = Plan.getPlanAPI().getInspectCachedUserDataMap().get(uuid);
        if (data == null) {
            return "Not in a Town";
        }
        String name = data.getName();
        try {
            Resident res = TownyUniverse.getDataSource().getResident(name);
            String town;
            if (res.hasTown()) {
                town = res.getTown().getName();
            } else {
                town = "Not in a Town";
            }
            return town;
        } catch (NotRegisteredException ex) {
            return ex + "";
        }
    }

}
