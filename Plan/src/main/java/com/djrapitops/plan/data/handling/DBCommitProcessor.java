/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.queue.processing.Processor;

import java.sql.SQLException;

/**
 * Processor for queueing a Database Commit after changes.
 *
 * @author Rsl1122
 */
public class DBCommitProcessor extends Processor<Database> {
    public DBCommitProcessor(Database object) {
        super(object);
    }

    @Override
    public void process() {
        try {
            object.commit();
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }
}