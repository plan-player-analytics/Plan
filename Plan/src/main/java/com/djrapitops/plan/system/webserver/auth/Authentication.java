/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.auth;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public interface Authentication {

    boolean isAuthorized(String permission) throws WebUserAuthException;

}