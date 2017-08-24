/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;

import java.sql.SQLException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class CommandProcessor extends Processor<String> {

    public CommandProcessor(String object) {
        super(object);
    }

    @Override
    public void process() {
        try {
            Plan.getInstance().getDB().getCommandUseTable().commandUsed(object);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}