package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.Hook;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Rsl1122
 */
public class API {

    private Plan plugin;
    private PlanLiteHook hook;

    /**
     * Class Construcor.
     *
     * @param plugin Current instance of Plan
     */
    public API(Plan plugin) {
        this.plugin = plugin;
        hook = plugin.getPlanLiteHook();
    }

    /**
     * Returns a user readable format of Time difference between two dates
     *
     * @param before Date with long value that is lower
     * @param after Date with long value that is higher
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeSinceDate(Date before, Date after) {
        return FormatUtils.formatTimeAmountSinceDate(before, after);
    }

    /**
     * Returns a user readable format of Time difference between two dates
     *
     * @param before String of long since Epoch 1970
     * @param after Date with long value that is higher
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeSinceString(String before, Date after) {
        return FormatUtils.formatTimeAmountSinceString(before, after);
    }

    /**
     * Returns a user readable format of Time
     *
     * @param timeInMs String of long value in milliseconds
     * @return String that is easily readable d:h:m:s
     */
    public static String formatTimeAmount(String timeInMs) {
        return FormatUtils.formatTimeAmount(timeInMs);
    }

    /**
     * Returns user readable format of a Date.
     *
     * @param timeInMs String of long since Epoch 1970
     * @return String that is easily readable date.
     */
    public static String formatTimeStamp(String timeInMs) {
        return FormatUtils.formatTimeStamp(timeInMs);
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getDebug() throws NullPointerException {
        return hook.getDebug();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleEssentials() throws NullPointerException {
        return hook.getVisibleEssentials();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleOnTime() throws NullPointerException {
        return hook.getVisibleOnTime();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleFactions() throws NullPointerException {
        return hook.getVisibleFactions();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleSuperbVote() throws NullPointerException {
        return hook.getVisibleSuperbVote();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleTowny() throws NullPointerException {
        return hook.getVisibleTowny();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleVault() throws NullPointerException {
        return hook.getVisibleVault();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisibleAdvancedAchievements() throws NullPointerException {
        return hook.getVisibleAdvancedAchievements();
    }

    /**
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public boolean getVisiblePlaceholderAPI() throws NullPointerException {
        return hook.getVisiblePlaceholderAPI();
    }

    /**
     * @param playerName
     * @param dataPoint variable to differentiate between DataPoint and String
     * return
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) throws NullPointerException {
        return hook.getData(playerName, dataPoint);
    }

    /**
     * @param playerName
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public HashMap<String, String> getData(String playerName) throws NullPointerException {
        return hook.getData(playerName);
    }

    /**
     * @param playerName
     * @param dataPoint variable to differentiate between DataPoint and String
     * return
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) throws NullPointerException {
        return hook.getAllData(playerName, dataPoint);
    }

    /**
     * @param playerName
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public HashMap<String, String> getAllData(String playerName) throws NullPointerException {
        return hook.getAllData(playerName);
    }

    /**
     * @param oldData
     * @return @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public HashMap<String, DataPoint> transformOldDataFormat(HashMap<String, String> oldData) throws NullPointerException {
        return hook.transformOldDataFormat(oldData);
    }

    /**
     * @param name name of the plugin registering the hook
     * @param hook Hook that is registered
     * @throws NullPointerException if PlanLite not installed
     * @deprecated Moved to PlanLite
     */
    @Deprecated
    public void addExtraHook(String name, Hook hook) throws NullPointerException {
        plugin.addExtraHook(name, hook);
    }
}
