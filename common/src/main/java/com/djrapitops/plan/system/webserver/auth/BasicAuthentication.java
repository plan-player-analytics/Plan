/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.auth;

import com.djrapitops.plan.api.exceptions.PassEncryptException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.PassEncryptUtil;

/**
 * Authentication handling for Basic Auth.
 * <p>
 * Basic access authentication (Wikipedia):
 * https://en.wikipedia.org/wiki/Basic_access_authentication
 *
 * @author Rsl1122
 */
public class BasicAuthentication implements Authentication {

    private String authenticationString;

    public BasicAuthentication(String authenticationString) {
        this.authenticationString = authenticationString;
    }

    @Override
    public WebUser getWebUser() throws WebUserAuthException {
        String decoded = Base64Util.decode(authenticationString);

        String[] userInfo = decoded.split(":");
        if (userInfo.length != 2) {
            throw new WebUserAuthException(FailReason.USER_AND_PASS_NOT_SPECIFIED);
        }

        String user = userInfo[0];
        String passwordRaw = userInfo[1];

        try {

            Database database = Database.getActive();
            if (!database.check().doesWebUserExists(user)) {
                throw new WebUserAuthException(FailReason.USER_DOES_NOT_EXIST, user);
            }

            WebUser webUser = database.fetch().getWebUser(user);

            boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, webUser.getSaltedPassHash());
            if (!correctPass) {
                throw new WebUserAuthException(FailReason.USER_PASS_MISMATCH, user);
            }
            return webUser;
        } catch (DBOpException | PassEncryptException e) {
            throw new WebUserAuthException(e);
        }
    }
}
