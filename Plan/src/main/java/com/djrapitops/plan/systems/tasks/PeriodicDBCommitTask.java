/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.tasks;

import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.processing.DBCommitProcessor;

/**
 * Periodically commits changes to the SQLite Database.
 *
 * @author Rsl1122
 */
public class PeriodicDBCommitTask extends AbsRunnable {

    private Plan plugin;

    public PeriodicDBCommitTask(Plan plugin) {
        super("PeriodicDBCommitTask");
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Database db = plugin.getDB();
        if ("mysql".equals(db.getConfigName())) {
            this.cancel();
            return;
        }
        plugin.addToProcessQueue(new DBCommitProcessor(db));
    }
}