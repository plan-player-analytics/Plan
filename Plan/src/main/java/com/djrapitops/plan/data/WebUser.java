package main.java.com.djrapitops.plan.data;

/**
 * Object containing webserver security user information.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUser {

    private final String user;
    private final String saltedPassHash;
    private final int permLevel;

    public WebUser(String user, String saltedPassHash, int permLevel) {
        this.user = user;
        this.saltedPassHash = saltedPassHash;
        this.permLevel = permLevel;
    }

    public String getName() {
        return user;
    }

    public String getSaltedPassHash() {
        return saltedPassHash;
    }

    public int getPermLevel() {
        return permLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebUser webUser = (WebUser) o;

        return permLevel == webUser.permLevel
                && user.equals(webUser.user)
                && saltedPassHash.equals(webUser.saltedPassHash);
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + saltedPassHash.hashCode();
        result = 31 * result + permLevel;
        return result;
    }
}
