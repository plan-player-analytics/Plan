/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.HasDate;
import com.djrapitops.plan.system.database.tables.Actions;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;

import java.util.Objects;

/**
 * Class that represents an action made by a player.
 *
 * @author Rsl1122
 */
public class Action implements HasDate {
    private final long date;
    private final Actions doneAction;
    private final String additionalInfo;
    private int serverID;

    public Action(long date, Actions doneAction, String additionalInfo) {
        this.date = date;
        this.doneAction = doneAction;
        this.additionalInfo = additionalInfo;
    }

    public Action(long date, Actions doneAction, String additionalInfo, int serverID) {
        this.date = date;
        this.doneAction = doneAction;
        this.additionalInfo = additionalInfo;
        this.serverID = serverID;
    }

    public long getDate() {
        return date;
    }

    public Actions getDoneAction() {
        return doneAction;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * Can only be used on Action classes returned by the ActionsTable.
     *
     * @return ID of the server the action occurred on.
     */
    public int getServerID() {
        return serverID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return date == action.date &&
                serverID == action.serverID &&
                doneAction == action.doneAction &&
                Objects.equals(additionalInfo, action.additionalInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, doneAction, additionalInfo, serverID);
    }

    @Override
    public String toString() {
        return Html.TABLELINE_3.parse(FormatUtils.formatTimeStampYear(date), doneAction.toString(), additionalInfo);
    }
}