/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.auth;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.data.WebUser;

/**
 * Interface for different WebUser authentication methods used by Requests.
 *
 * @author Rsl1122
 */
public interface Authentication {

    WebUser getWebUser() throws WebUserAuthException;

}
