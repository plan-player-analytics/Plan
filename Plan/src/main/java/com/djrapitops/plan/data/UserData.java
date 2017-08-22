package main.java.com.djrapitops.plan.data;

import java.net.InetAddress;
import java.util.*;

/**
 * This class is used for storing information about a player during runtime.
 *
 * @author Rsl1122
 */
// TODO Change to be only used for DB User Get Query responses.
public class UserData {

    private final List<Session> sessions;
    private int accessing;
    private boolean clearAfterSave;
    private UUID uuid;
    private String name; //TODO DB Update code to JoinListener
    @Deprecated
    private Set<String> nicknames; //TODO DB Update code to ChatListener
    @Deprecated
    private String lastNick; //TODO DB Update code to ChatListener
    @Deprecated
    private String geolocation; //TODO DB Update code to JoinListener
    @Deprecated
    private Set<InetAddress> ips; //TODO DB Update code to JoinListener
    @Deprecated
    private int timesKicked; //TODO DB Update code to KickListener
    @Deprecated
    private boolean isOp; //TODO DB Update code to JoinListener
    @Deprecated
    private boolean isBanned; //TODO DB Update code to JoinListener
    @Deprecated
    private long registered; //TODO DB Update code to JoinListener (When registering)
    @Deprecated
    private long lastPlayed; //TODO DB Update code to Join, Refresh, QuitListener

    /**
     * Creates a new UserData object with given values and default values.
     * <p>
     * Some variables are left uninitialized: isBanned, lastPlayed, playTime,
     * loginTimes, timesKicked, lastGmSwapTime, mobKills, deaths, lastWorldSwapTime, lastWorld
     * <p>
     * These variables need to be set with setters.
     * <p>
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     * <p>
     * gmTimes Map will contain 4 '0L' values: SURVIVAL, CREATIVE,
     * ADVENTURE, SPECTATOR
     * worldTimes Map is left empty.
     *
     * @param uuid   UUID of the player
     * @param name   Name of the player.
     */
    public UserData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        sessions = new ArrayList<>();
    }

    /**
     * Creates a new UserData object with copied values.
     *
     * @param data UserData to copy into the new object.
     */
    public UserData(UserData data) {
        this.uuid = data.getUuid();
        this.name = data.getName();
        this.sessions = new ArrayList<>();
        sessions.addAll(data.getSessions());
    }

    /**
     * Used to get the UUID of the player.
     *
     * @return UUID.
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the InetAddress Set.
     *
     * @return a HashSet of ips.
     */
    public Set<InetAddress> getIps() {
        return ips;
    }

    /**
     * Get the nickname String Set.
     *
     * @return a HashSet of Strings.
     */
    public Set<String> getNicknames() {
        return nicknames;
    }

    /**
     * Get the Epoch millisecond the player registered.
     *
     * @return long in ms.
     */
    public long getRegistered() {
        return registered;
    }

    /**
     * Get the Epoch millisecond the player was last seen.
     * <p>
     * NOT INITIALIZED BY CONSTRUCTORS. Value is updated periodically by cache
     * if the player is online.
     *
     * @return long in ms.
     */
    public long getLastPlayed() {
        return lastPlayed;
    }

    /**
     * Get how many times the player has logged in.
     * <p>
     * NOT INITIALIZED BY CONSTRUCTORS.
     *
     * @return 0 to Integer.MAX
     */
    public int getLoginTimes() {
        return sessions.size();
    }

    /**
     * Get how many times the player has been kicked.
     * <p>
     * NOT INITIALIZED BY CONSTRUCTORS.
     *
     * @return 0 to Integer.MAX
     */
    public int getTimesKicked() {
        return timesKicked;
    }

    /**
     * Is the user Operator?
     *
     * @return opped?
     */
    public boolean isOp() {
        return isOp;
    }

    /**
     * Is the user Banned?
     *
     * @return banned?
     */
    public boolean isBanned() {
        return isBanned;
    }

    /**
     * Get the username of the player.
     *
     * @return username.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the sessions of a player.
     *
     * @return a list of Session.
     */
    public List<Session> getSessions() {
        return sessions;
    }

    @Override
    public int hashCode() {
        int result = sessions.hashCode();
        result = 31 * result + accessing;
        result = 31 * result + (clearAfterSave ? 1 : 0);
        result = 31 * result + uuid.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final UserData other = (UserData) obj;

        return Objects.equals(this.name, other.name)
                && Objects.equals(this.uuid, other.uuid)
                && Objects.equals(this.sessions, other.sessions);
    }

    public long getPlayTime() {
        return 0; //TODO Use Sessions
    }

    public List<String> getGeolocations() {
        // TODO
        return null;
    }
}