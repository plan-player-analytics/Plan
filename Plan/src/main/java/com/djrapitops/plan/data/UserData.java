package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used for storing information about a player during runtime.
 *
 * @author Rsl1122
 */
// TODO Change to be only used for DB User Get Query responses.
public class UserData {

    private final List<SessionData> sessions;
    private int accessing;
    private boolean clearAfterSave;
    private UUID uuid;
    private String name; //TODO DB Update code to JoinListener
    @Deprecated private Set<String> nicknames; //TODO DB Update code to ChatListener
    @Deprecated private String lastNick; //TODO DB Update code to ChatListener
    @Deprecated private String geolocation; //TODO DB Update code to JoinListener
    @Deprecated private Set<InetAddress> ips; //TODO DB Update code to JoinListener
    @Deprecated private int loginTimes; // Moving to sessions.size
    @Deprecated private int timesKicked; //TODO DB Update code to KickListener
    @Deprecated private boolean isOp; //TODO DB Update code to JoinListener
    @Deprecated private boolean isBanned; //TODO DB Update code to JoinListener
    @Deprecated private boolean isOnline; //TODO New Class for getting online status of players
    @Deprecated private int mobKills; //TODO Move to SessionData
    @Deprecated private List<KillData> playerKills; //TODO Move to SessionData
    @Deprecated private int deaths; //TODO Move to SessionData
    @Deprecated private long registered; //TODO DB Update code to JoinListener (When registering)
    @Deprecated private long lastPlayed; //TODO DB Update code to Join, Refresh, QuitListener
    @Deprecated private long playTime; //TODO Move to SessionData
    @Deprecated private GMTimes gmTimes; //TODO Move to WorldTimes
    @Deprecated private WorldTimes worldTimes; //TODO Move to SessionData

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
     * @param reg    Epoch millisecond the player registered.
     * @param op     Is the player op? (true/false)
     * @param name   Name of the player.
     * @param online Is the player online?
     */
    public UserData(UUID uuid, long reg, boolean op, String gm, String name, boolean online) {
        accessing = 0;

        this.gmTimes = new GMTimes(gm);
        this.worldTimes = new WorldTimes();

        this.uuid = uuid;
        this.name = name;
        lastNick = "";
        nicknames = new HashSet<>();
        ips = new HashSet<>();
        geolocation = "Not Known";

        isOp = op;
        isOnline = online;
        registered = reg;

        sessions = new ArrayList<>();
        playerKills = new ArrayList<>();
    }

    /**
     * Creates a new UserData object with the variables inside a Player object.
     * <p>
     * Some variables are left uninitialized: lastPlayed, playTime, loginTimes,
     * timesKicked, lastGmSwapTime, mobKills, deaths and currentSession.
     * <p>
     * These variables need to be set with setters.
     * <p>
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     * <p>
     * gmTimes HashMap will contain 4 '0L' values: SURVIVAL, CREATIVE,
     * ADVENTURE, SPECTATOR
     *
     * @param player IPlayer object.
     */
    public UserData(IPlayer player) {
        this(player.getUuid(), player.getFirstPlayed(), player.isOp(), player.getGamemode().name(), player.getName(), player.isOnline());
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Log.error("Error getting ban date from Bukkit files. " + uuid.toString());
            Log.toLog(this.getClass().getName(), e);
            isBanned = false;
        }
    }

    /**
     * Creates a new UserData object with the variables inside a OfflinePlayer
     * object.
     * <p>
     * Some variables are left uninitialized: location, lastPlayed, playTime,
     * loginTimes, timesKicked, lastGmSwapTime, mobKills, deaths and
     * currentSession.
     * <p>
     * These variables need to be set with setters.
     * <p>
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     * <p>
     * gmTimes HashMap will contain 4 '0L' values: SURVIVAL, CREATIVE,
     * ADVENTURE, SPECTATOR
     * <p>
     * lastGM will be set as SURVIVAL
     *
     * @param player IOfflinePlayer object.
     */
    public UserData(IOfflinePlayer player) {
        this(player.getUniqueId(), player.getFirstPlayed(), player.isOp(), "SURVIVAL", player.getName(), player.isOnline());
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Log.error("Error getting ban date from Bukkit files. " + uuid.toString());
            Log.toLog(this.getClass().getName(), e);
            isBanned = false;
        }
    }

    /**
     * Creates a new UserData object with copied values.
     *
     * @param data UserData to copy into the new object.
     */
    public UserData(UserData data) {
        this.accessing = 0;
        this.uuid = data.getUuid();
        this.ips = new HashSet<>();
        ips.addAll(data.getIps());
        this.nicknames = new HashSet<>();
        nicknames.addAll(data.getNicknames());
        this.lastNick = data.getLastNick();
        this.registered = data.getRegistered();
        this.lastPlayed = data.getLastPlayed();
        this.playTime = data.getPlayTime();
        this.loginTimes = data.getLoginTimes();
        this.timesKicked = data.getTimesKicked();
        this.gmTimes = data.getGmTimes();
        this.worldTimes = data.getWorldTimes();
        this.isOp = data.isOp();
        this.isBanned = data.isBanned();
        this.geolocation = data.getGeolocation();
        this.mobKills = data.getMobKills();
        this.playerKills = data.getPlayerKills();
        this.deaths = data.getDeaths();
        this.name = data.getName();
        this.isOnline = data.isOnline();
        this.sessions = new ArrayList<>();
        sessions.addAll(data.getSessions());
    }

    @Override
    public String toString() {
        try {
            return "{" + "accessing:" + accessing + "|uuid:" + uuid + "|ips:" + ips + "|nicknames:" + nicknames + "|lastNick:" + lastNick + "|registered:" + registered + "|lastPlayed:" + lastPlayed + "|playTime:" + playTime + "|loginTimes:" + loginTimes + "|timesKicked:" + timesKicked + "|gm:" + gmTimes + "|world:" + worldTimes + "|isOp:" + isOp + "|isBanned:" + isBanned + "|geolocation:" + geolocation + "|mobKills:" + mobKills + "|playerKills:" + playerKills + "|deaths:" + deaths + "|name:" + name + "|isOnline:" + isOnline + "|sessions:" + sessions + '}';
        } catch (Exception e) {
            return "UserData: Error on toString:" + e;
        }
    }

    /**
     * Adds an to the ips Set if it is not null or the set doesn't contain it.
     *
     * @param ip InetAddress of the player.
     */
    public void addIpAddress(InetAddress ip) {
        if (Verify.notNull(ip)) {
            ips.add(ip);
        }
    }

    /**
     * Adds multiple ips to the ips set if they're not null.
     *
     * @param addIps a Collection of InetAddresses the player has logged from.
     */
    public void addIpAddresses(Collection<InetAddress> addIps) {
        if (addIps.isEmpty()) {
            return;
        }

        ips.addAll(addIps.stream().filter(Verify::notNull).collect(Collectors.toList()));
    }

    /**
     * Adds a nickname to the nicknames Set.
     * <p>
     * null or empty values filtered.
     * <p>
     * lastNick will be set as the given parameter, if accepted.
     *
     * @param nick Displayname of the player.
     * @return was lastNick updated?
     */
    public boolean addNickname(String nick) {
        if (!Verify.isEmpty(nick)) {
            boolean isNew = !nicknames.contains(nick);
            nicknames.add(nick);
            if (isNew) {
                lastNick = nick;
            }
            return isNew;
        }
        return false;
    }

    /**
     * Adds nicknames to the nicknames Set.
     * <p>
     * null or empty values filtered.
     *
     * @param addNicks Collection of nicknames.
     */
    public void addNicknames(Collection<String> addNicks) {
        nicknames.addAll(addNicks.stream().filter(nick -> !Verify.isEmpty(nick)).collect(Collectors.toList()));
    }

    /**
     * Adds a new SessionData to the sessions list.
     * <p>
     * null and invalid sessions filtered.
     *
     * @param session SessionData object
     */
    public void addSession(SessionData session) {
        if (Verify.notNull(session) && session.isValid()) {
            sessions.add(session);
        }
    }

    /**
     * Adds SessionData objects to the sessions list.
     * <p>
     * null and invalid sessions filtered.
     *
     * @param sessions Collection of SessionData objects.
     */
    public void addSessions(Collection<SessionData> sessions) {
        Collection<SessionData> filteredSessions = sessions.stream()
                .filter(Verify::notNull)
                .filter(SessionData::isValid)
                .collect(Collectors.toList());
        this.sessions.addAll(filteredSessions);
    }

    /**
     * Changes the value of isBanned.
     *
     * @param isBanned Is the player banned?
     */
    public void updateBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    /**
     * Checks whether or not the UserData object is accessed by different save
     * processes.
     *
     * @return true if accessed.
     */
    public boolean isAccessed() {
        return accessing > 0;
    }

    /**
     * Accesses the UserData object to protect it from being cleared.
     */
    public void access() {
        accessing++;
    }

    /**
     * Stops accessing the object so that it can now be cleared.
     */
    public void stopAccessing() {
        accessing--;
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
     * Set the UUID.
     *
     * @param uuid UUID
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
     * Set the ips set.
     *
     * @param ips ips of the user.
     */
    public void setIps(Set<InetAddress> ips) {
        if (Verify.notNull(ips)) {
            this.ips = ips;
        }
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
     * Set the nicknames set.
     *
     * @param nicknames nicknames of the user.
     */
    public void setNicknames(Set<String> nicknames) {
        if (Verify.notNull(nicknames)) {
            this.nicknames = nicknames;
        }
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
     * Set the time the user was registered.
     *
     * @param registered Epoch millisecond of register time.
     */
    public void setRegistered(long registered) {
        this.registered = registered;
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
     * Set the time the user was last seen.
     * <p>
     * Affects playtime calculation, playtime should be updated before updating
     * this value.
     *
     * @param lastPlayed Epoch millisecond of last seen moment.
     */
    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    /**
     * Get the playtime in milliseconds.
     * <p>
     * NOT INITIALIZED BY CONSTRUCTORS. Value is updated periodically by cache
     * if the player is online.
     *
     * @return time in ms.
     */
    public long getPlayTime() {
        return playTime;
    }

    /**
     * Set the time the user has been playing.
     *
     * @param playTime Time in ms.
     */
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    /**
     * Get how many times the player has logged in.
     * <p>
     * NOT INITIALIZED BY CONSTRUCTORS.
     *
     * @return 0 to Integer.MAX
     */
    public int getLoginTimes() {
        return loginTimes;
    }

    /**
     * Set how many times the user has logged in.
     * <p>
     * No check for input.
     *
     * @param loginTimes 0 to Int.MAX
     */
    public void setLoginTimes(int loginTimes) {
        this.loginTimes = loginTimes;
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
     * Set how many times the user has been kicked.
     * <p>
     * No check for input.
     *
     * @param timesKicked 0 to Int.MAX
     */
    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    /**
     * Get the GMTimes object.
     *
     * @return TimeKeeper object with possible keys of SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR
     */
    public GMTimes getGmTimes() {
        return gmTimes;
    }

    /**
     * Set the GM Times object containing playtime in each gamemode.
     *
     * @param gmTimes GM Times object
     */
    public void setGmTimes(GMTimes gmTimes) {
        if (Verify.notNull(gmTimes)) {
            this.gmTimes = gmTimes;
        }
    }

    public void setGmTimes(Map<String, Long> times) {
        if (Verify.notNull(times)) {
            for (Map.Entry<String, Long> entry : times.entrySet()) {
                gmTimes.setTime(entry.getKey(), entry.getValue());
            }
        }
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
     * Set the banned value.
     *
     * @param isBanned true/false
     */
    public void setBanned(boolean isBanned) {
        this.isBanned = isBanned;
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
     * Set the username of the user.
     *
     * @param name username.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set whether or not player is op.
     *
     * @param isOp operator?
     */
    public void setIsOp(boolean isOp) {
        this.isOp = isOp;
    }

    /**
     * Is the player online?
     *
     * @return true if data is cached to datacache, false if not.
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Set the online value.
     *
     * @param isOnline true/false
     */
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    /**
     * Get how many mob kills the player has.
     *
     * @return 0 to Int.MAX
     */
    public int getMobKills() {
        return mobKills;
    }

    /**
     * Get how many mob kills the player has.
     *
     * @param mobKills 0 to Int.MAX
     */
    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    /**
     * Get the player kills list.
     *
     * @return playerkills list.
     */
    public List<KillData> getPlayerKills() {
        return playerKills;
    }

    /**
     * Set the playerkills list.
     *
     * @param playerKills list of players kills.
     */
    public void setPlayerKills(List<KillData> playerKills) {
        if (Verify.notNull(playerKills)) {
            this.playerKills = playerKills;
        }
    }

    /**
     * Add a Killdata to player's kills list.
     *
     * @param kill KillData representing a player kill.
     */
    public void addPlayerKill(KillData kill) {
        playerKills.add(kill);
    }

    /**
     * Get how many times the player has died.
     *
     * @return 0 to Int.MAX
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     * Set how many times the player has died.
     *
     * @param deaths 0 to Int.MAX
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     * Get the sessions of a player.
     *
     * @return a list of SessionData.
     */
    public List<SessionData> getSessions() {
        return sessions;
    }

    /**
     * Get the last nickname the user has set.
     * <p>
     * Set when using addNickname(String)
     *
     * @return last nickname used.
     */
    public String getLastNick() {
        return lastNick;
    }

    /**
     * Set the last nickname the user has set.
     * <p>
     * Also set when using addNickname(String)
     *
     * @param lastNick last nickname used.
     */
    public void setLastNick(String lastNick) {
        this.lastNick = lastNick;
    }


    public WorldTimes getWorldTimes() {
        return worldTimes;
    }

    public void setWorldTimes(WorldTimes worldTimes) {
        this.worldTimes = worldTimes;
    }

    /**
     * Check whether or not the object should be cleared from cache after it has
     * been saved.
     *
     * @return true/false
     */
    public boolean shouldClearAfterSave() {
        return clearAfterSave;
    }

    /**
     * Set whether or not the object should be cleared from cache after it has
     * been saved.
     *
     * @param clearAfterSave true/false
     */
    public void setClearAfterSave(boolean clearAfterSave) {
        this.clearAfterSave = clearAfterSave;
    }

    public String getGeolocation() {
        return geolocation;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
    }

    @Override
    public int hashCode() {
        int result = sessions.hashCode();
        result = 31 * result + accessing;
        result = 31 * result + (clearAfterSave ? 1 : 0);
        result = 31 * result + uuid.hashCode();
        result = 31 * result + ips.hashCode();
        result = 31 * result + nicknames.hashCode();
        result = 31 * result + lastNick.hashCode();
        result = 31 * result + (int) (registered ^ (registered >>> 32));
        result = 31 * result + (int) (lastPlayed ^ (lastPlayed >>> 32));
        result = 31 * result + (int) (playTime ^ (playTime >>> 32));
        result = 31 * result + loginTimes;
        result = 31 * result + timesKicked;
        result = 31 * result + gmTimes.hashCode();
        result = 31 * result + worldTimes.hashCode();
        result = 31 * result + (isOp ? 1 : 0);
        result = 31 * result + (isBanned ? 1 : 0);
        result = 31 * result + geolocation.hashCode();
        result = 31 * result + mobKills;
        result = 31 * result + playerKills.hashCode();
        result = 31 * result + deaths;
        result = 31 * result + name.hashCode();
        result = 31 * result + (isOnline ? 1 : 0);
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

        return this.registered == other.registered
                && this.playTime == other.playTime
                && this.loginTimes == other.loginTimes
                && this.timesKicked == other.timesKicked
                && this.mobKills == other.mobKills
                && this.deaths == other.deaths
                && Objects.equals(this.lastNick, other.lastNick)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.uuid, other.uuid)
                && Objects.equals(this.ips, other.ips)
                && Objects.equals(this.nicknames, other.nicknames)
                && Objects.equals(this.gmTimes, other.gmTimes)
                && Objects.equals(this.worldTimes, other.worldTimes)
                && Objects.equals(this.playerKills, other.playerKills)
                && Objects.equals(this.sessions, other.sessions);
    }
}