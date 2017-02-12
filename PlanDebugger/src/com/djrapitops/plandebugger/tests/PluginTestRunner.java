package com.djrapitops.plandebugger.tests;

import com.djrapitops.plan.Plan;
import com.djrapitops.plandebugger.PlanDebugger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rsl1122
 */
public class PluginTestRunner extends TestRunner {

    private List<Test> tests;
    
    public PluginTestRunner(PlanDebugger debug, Plan plan) {
        super(debug, plan);
    }
        
    @Override
    public List<String> runAllTests() {
        List<String> errors = new ArrayList<>();
        for (Test test : tests) {
            try {
                test.runTest();
            } catch (Exception e) {
                String msg = "Test "+test.getTestName()+" ran into ERROR: "+e.getMessage();
                debug.logError(msg);
                for (StackTraceElement x : e.getStackTrace()) {
                    debug.toLog(x+"");
                }
                errors.add(msg);
            }
        }
        return errors;
    } 
    
}
