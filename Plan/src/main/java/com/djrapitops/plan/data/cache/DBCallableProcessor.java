package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.UserInfo;

/**
 * This interface can be extended with anything as the process method and
 * given to the Database.
 * <p>
 * The process method will be called with the UserInfo object fetched from the
 * database.
 *
 * @author Rsl1122
 * @since 2.6.0
 */
@Deprecated
public interface DBCallableProcessor {

    /**
     * Method used to do multiple things to UserInfo objects such as Caching,
     * changing properties etc.
     *
     * @param data UserInfo object given to the DBCallableProcessor by the
     *             method it was given as parameter to.
     */
    void process(UserInfo data);
}
