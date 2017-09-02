/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.importing.importers;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.processing.importing.ServerImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportRefiner;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.util.List;

/**
 * @author Fuzzlemann
 * @since 4.0.0
 */
public abstract class Importer {

    public abstract List<String> getNames();

    public abstract ServerImportData getServerImportData();

    public abstract List<UserImportData> getUserImportData();

    public final void processImport() {
        String benchmarkName = "Import processing";
        String serverBenchmarkName = "Server Data processing";
        String userDataBenchmarkName = "User Data processing";

        Benchmark.start(benchmarkName);

        Benchmark.start(serverBenchmarkName);
        processServerData();
        Benchmark.stop(serverBenchmarkName);

        Benchmark.start(userDataBenchmarkName);
        processUserData();
        Benchmark.stop(userDataBenchmarkName);

        Benchmark.stop(benchmarkName);
    }

    private void processServerData() {
        String benchmarkName = "Processing Server Data";
        String getDataBenchmarkName = "Getting Server Data";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        ServerImportData serverImportData = getServerImportData();

        Benchmark.stop(getDataBenchmarkName);

        if (serverImportData == null) {
            Log.debug("Server Import Data null, skipping");
            return;
        }

        Database db = Plan.getInstance().getDB();

        //TODO

        Benchmark.start(benchmarkName);
    }

    private void processUserData() {
        String benchmarkName = "Processing User Data";
        String getDataBenchmarkName = "Getting User Data";

        Benchmark.start(benchmarkName);
        Benchmark.start(getDataBenchmarkName);

        List<UserImportData> userImportData = getUserImportData();

        Benchmark.stop(getDataBenchmarkName);

        if (Verify.isEmpty(userImportData)) {
            Log.debug("User Import Data null or empty, skipping");
            return;
        }

        UserImportRefiner userImportRefiner = new UserImportRefiner(Plan.getInstance(), userImportData);
        userImportData = userImportRefiner.refineData();

        //TODO

        Benchmark.stop(benchmarkName);
    }
}
