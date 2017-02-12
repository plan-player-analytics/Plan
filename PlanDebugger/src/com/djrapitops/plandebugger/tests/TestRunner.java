package com.djrapitops.plandebugger.tests;

import com.djrapitops.plan.Plan;
import com.djrapitops.plandebugger.PlanDebugger;
import java.util.List;

/**
 *
 * @author Rsl1122
 */
public abstract class TestRunner {
    Plan plan;
    PlanDebugger debug;
    private int testsRun;
    private int testsFailed;
    private int testsError;

    public TestRunner(PlanDebugger debug, Plan plan) {
        this.plan = plan;
        this.debug = debug;
        testsRun = 0;
        testsFailed = 0;
        testsError = 0;
    }
    
    public abstract List<String> runAllTests();

    public int getTestsRun() {
        return testsRun;
    }

    public int getTestsFailed() {
        return testsFailed;
    }

    public int getTestsError() {
        return testsError;
    }
    
    
}
