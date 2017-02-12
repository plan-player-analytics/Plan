
package com.djrapitops.plandebugger.tests;

import com.djrapitops.plan.Plan;
import com.djrapitops.plandebugger.PlanDebugger;

/**
 *
 * @author Rsl1122
 */
public abstract class Test {
    Plan plan;
    PlanDebugger debug;
    private String testName;

    public Test(Plan plan, PlanDebugger debug, String testName) {
        this.plan = plan;
        this.debug = debug;
    }
    
    public abstract boolean runTest() throws Exception;
    
    public void pass() {
        debug.log("Test "+testName+": PASSED");
    }
    
    public void fail() {
        debug.logError("Test "+testName+": FAILED");
    }

    public String getTestName() {
        return testName;
    }
    
}
