/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webapi;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.webserver.response.Response;

import java.util.Map;

/**
 * @author Fuzzlemann
 */
public interface WebAPI {

    Response onResponse(Plan plan, Map<String, String> variables);
}