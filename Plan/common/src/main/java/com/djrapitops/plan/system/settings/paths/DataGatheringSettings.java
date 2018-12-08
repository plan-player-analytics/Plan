package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;

/**
 * {@link Setting} values that are in "Data_gathering" section.
 *
 * @author Rsl1122
 */
public class DataGatheringSettings {

    public static final Setting<Boolean> GEOLOCATIONS = new BooleanSetting("Data_gathering.Geolocations");
    public static final Setting<Boolean> LOG_UNKNOWN_COMMANDS = new BooleanSetting("Data_gathering.Commands.Log_unknown");
    public static final Setting<Boolean> COMBINE_COMMAND_ALIASES = new BooleanSetting("Data_gathering.Commands.Log_aliases_as_main_command");

    private DataGatheringSettings() {
        /* static variable class */
    }

}