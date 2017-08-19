/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.utilities.webserver.api;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ui.webserver.response.Response;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public interface WebAPI {

    Response onResponse(Plan plan, Map<String, String> variables);
}