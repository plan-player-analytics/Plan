package main.java.com.djrapitops.plan.ui.webserver;

import com.sun.net.httpserver.BasicAuthenticator;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.database.tables.SecurityTable;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;

import java.sql.SQLException;

public class Authenticator extends BasicAuthenticator {

    private final Plan plugin;

    public Authenticator(Plan plugin, String realm) {
        super(realm);
        this.plugin = plugin;
    }

    @Override
    public boolean checkCredentials(String user, String pwd) {
        try {
            return isAuthorized(user, pwd, this.realm);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
    }

    private boolean isAuthorized(String user, String passwordRaw, String target) throws PassEncryptUtil.CannotPerformOperationException, PassEncryptUtil.InvalidHashException, SQLException {
        SecurityTable securityTable = plugin.getDB().getSecurityTable();
        if (!securityTable.userExists(user)) {
            return false;
        }
        WebUser securityInfo = securityTable.getSecurityInfo(user);

        boolean correctPass = PassEncryptUtil.verifyPassword(passwordRaw, securityInfo.getSaltedPassHash());
        if (!correctPass) {
            return false;
    }
        int permLevel = securityInfo.getPermLevel(); // Lower number has higher clearance.
        int required = getRequiredPermLevel(target, securityInfo.getName());
        return permLevel <= required;
    }

    private int getRequiredPermLevel(String target, String user) {
        String[] t = target.split("/");
        if (t.length < 3) {
            return 0;
        }
        final String wantedUser = t[2].toLowerCase().trim();
        final String theUser = user.trim().toLowerCase();
        if (t[1].equals("players")) {
            return 1;
        }
        if (t[1].equals("player")) {
            if (wantedUser.equals(theUser)) {
                return 2;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
