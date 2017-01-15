package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.Date;

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
}
