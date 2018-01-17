/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.auth;

import com.djrapitops.plan.api.exceptions.PassEncryptException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.utilities.Base64Util;
import com.djrapitops.plan.utilities.PassEncryptUtil;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class BasicAuthentication implements Authentication {

    private String authenticationString;

    private WebUser user;

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
                throw new WebUserAuthException(FailReason.USER_DOES_NOT_EXIST);
            }

            WebUser webUser = database.fetch().getWebUser(user);

            boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, webUser.getSaltedPassHash());
            if (!correctPass) {
                throw new WebUserAuthException(FailReason.USER_PASS_MISMATCH);
            }
            return webUser;
        } catch (DBException | PassEncryptException e) {
            throw new WebUserAuthException(e);
        }
    }
}