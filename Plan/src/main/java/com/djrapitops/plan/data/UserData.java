package main.java.com.djrapitops.plan.data;

import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.Log;

import java.net.InetAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used for storing information about a player during runtime.
 *
 * @author Rsl1122
 */
public class UserData {

    private int accessing;
    private boolean clearAfterSave;

    private UUID uuid;
    private Set<InetAddress> ips;
    private Set<String> nicknames;
    private String lastNick;
    private long registered;
    private long lastPlayed;
    private long playTime;
    private int loginTimes;
    private int timesKicked;
    private long lastGmSwapTime;
    private String lastGamemode;
    private Map<String, Long> gmTimes;
    private boolean isOp;
    private boolean isBanned;
    private String geolocation;

    private int mobKills;
    private List<KillData> playerKills;
    private int deaths;

    private String name;
    private boolean isOnline;

    private SessionData currentSession;
    private List<SessionData> sessions;

    /**
     * Creates a new UserData object with given values and default values.
     *
     * Some variables are left uninitialized: isBanned, lastPlayed, playTime,
     * loginTimes, timesKicked, lastGmSwapTime, mobKills, deaths and
     * currentSession.
     *
     * These variables need to be set with setters.
     *
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     *
     * gmTimes HashMap will contain 4 '0L' values: SURVIVAL, CREATIVE,
     * ADVENTURE, SPECTATOR
     *
     * @param uuid UUID of the player
     * @param reg Epoch millisecond the player registered.
     * @param op Is the player op? (true/false)
     * @param lastGM last GameMode the player was seen in.
     * @param name Name of the player.
     * @param online Is the player online?
     */
    public UserData(UUID uuid, long reg, boolean op, String lastGM, String name, boolean online) {
        accessing = 0;
        this.uuid = uuid;
        registered = reg;
        isOp = op;
        nicknames = new HashSet<>();
        ips = new HashSet<>();
        gmTimes = new HashMap<>();
        String[] gms = new String[]{"SURVIVAL", "CREATIVE", "ADVENTURE", "SPECTATOR"};
        for (String gm : gms) {
            gmTimes.put(gm, 0L);
        }
        lastGamemode = lastGM;
        geolocation = "Not Known";
        this.name = name;
        isOnline = online;
        sessions = new ArrayList<>();
        lastNick = "";
        playerKills = new ArrayList<>();
    }

    /**
     * Creates a new UserData object with the variables inside a Player object.
     *
     * Some variables are left uninitialized: lastPlayed, playTime, loginTimes,
     * timesKicked, lastGmSwapTime, mobKills, deaths and currentSession.
     *
     * These variables need to be set with setters.
     *
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     *
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
     *
     * Some variables are left uninitialized: location, lastPlayed, playTime,
     * loginTimes, timesKicked, lastGmSwapTime, mobKills, deaths and
     * currentSession.
     *
     * These variables need to be set with setters.
     *
     * All Collections are left empty: locations, nicknames, ips, sessions,
     * playerKills. Because nicknames is empty, lastNick is an empty string.
     *
     * gmTimes HashMap will contain 4 '0L' values: SURVIVAL, CREATIVE,
     * ADVENTURE, SPECTATOR
     *
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
        this.lastGmSwapTime = data.getLastGmSwapTime();
        this.lastGamemode = data.getLastGamemode();
        this.gmTimes = new HashMap<>();
        gmTimes.putAll(data.getGmTimes());
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
            return "{" + "accessing:" + accessing + "|uuid:" + uuid + "|ips:" + ips + "|nicknames:" + nicknames + "|lastNick:" + lastNick + "|registered:" + registered + "|lastPlayed:" + lastPlayed + "|playTime:" + playTime + "|loginTimes:" + loginTimes + "|timesKicked:" + timesKicked + "|lastGmSwapTime:" + lastGmSwapTime + "|lastGamemode:" + lastGamemode + "|gmTimes:" + gmTimes + "|isOp:" + isOp + "|isBanned:" + isBanned + "|geolocation:" + geolocation + "|mobKills:" + mobKills + "|playerKills:" + playerKills + "|deaths:" + deaths + "|name:" + name + "|isOnline:" + isOnline + "|currentSession:" + currentSession + "|sessions:" + sessions + '}';
        } catch (Throwable e) {
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
     *
     * null or empty values filtered.
     *
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
     *
     * null or empty values filtered.
     *
     * @param addNicks Collection of nicknames.
     */
    public void addNicknames(Collection<String> addNicks) {
        nicknames.addAll(addNicks.stream().filter(nick -> !Verify.isEmpty(nick)).collect(Collectors.toList()));
    }

    /**
     * Set a specific GameMode's millisecond value.
     *
     * @param gm Name of Gamemode.
     * @param time Milliseconds spent in the gamemode.
     */
    public void setGMTime(String gm, long time) {
        if (!Verify.notNull(gmTimes)) {
            gmTimes = new HashMap<>();
        }
        if (Verify.notNull(gm)) {
            gmTimes.put(gm, time);
        }
    }

    /**
     * Set every GameMode's millisecond value.
     *
     * @param survivalTime ms spent in SURVIVAL
     * @param creativeTime ms spent in CREATIVE
     * @param adventureTime ms spent in ADVENTURE
     * @param spectatorTime ms spent in SPECTATOR
     */
    public void setAllGMTimes(long survivalTime, long creativeTime, long adventureTime, long spectatorTime) {
        gmTimes.clear();
        gmTimes.put("SURVIVAL", survivalTime);
        gmTimes.put("CREATIVE", creativeTime);
        gmTimes.put("ADVENTURE", adventureTime);
        gmTimes.put("SPECTATOR", spectatorTime);
    }

    /**
     * Adds a new SessionData to the sessions list.
     *
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
     *
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
     * Sets the current session.
     *
     * Currently unused.
     *
     * @param session SessionData object, no restrictions.
     */
    public void setCurrentSession(SessionData session) {
        currentSession = session;
    }

    /**
     * Gets the current session.
     *
     * Currently unused.
     *
     * @return SessionData object with a recent start.
     */
    public SessionData getCurrentSession() {
        return currentSession;
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
     *
     * NOT INITIALIZED BY CONSTRUCTORS. Value is updated periodically by cache
     * if the player is online.
     *
     * @return long in ms.
     */
    public long getLastPlayed() {
        return lastPlayed;
    }

    /**
     * Get the playtime in milliseconds.
     *
     * NOT INITIALIZED BY CONSTRUCTORS. Value is updated periodically by cache
     * if the player is online.
     *
     * @return time in ms.
     */
    public long getPlayTime() {
        return playTime;
    }

    /**
     * Get how many times the player has logged in.
     *
     * NOT INITIALIZED BY CONSTRUCTORS.
     *
     * @return 0 to Integer.MAX
     */
    public int getLoginTimes() {
        return loginTimes;
    }

    /**
     * Get how many times the player has been kicked.
     *
     * NOT INITIALIZED BY CONSTRUCTORS.
     *
     * @return 0 to Integer.MAX
     */
    public int getTimesKicked() {
        return timesKicked;
    }

    /**
     * Get the GMTimes Map.
     *
     * @return a GameMode map with 4 keys: SURVIVAL, CREATIVE, ADVENTURE,
     * SPECTATOR.
     */
    public Map<String, Long> getGmTimes() {
        if (gmTimes == null) {
            gmTimes = new HashMap<>();
        }
        return gmTimes;
    }

    /**
     * Get the last time a Gamemode time was updated.
     *
     * @return Epoch millisecond of last GM Time update.
     */
    public long getLastGmSwapTime() {
        return lastGmSwapTime;
    }

    /**
     * Get the last Gamemode that the user was seen in.
     *
     * When player changes to SURVIVAL this is set to SURVIVAL.
     *
     * @return Gamemode.
     */
    public String getLastGamemode() {
        return lastGamemode;
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
     * Set the UUID.
     *
     * @param uuid UUID
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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
     * Set the time the user was registered.
     *
     * @param registered Epoch millisecond of register time.
     */
    public void setRegistered(long registered) {
        this.registered = registered;
    }

    /**
     * Set the time the user was last seen.
     *
     * Affects playtime calculation, playtime should be updated before updating
     * this value.
     *
     * @param lastPlayed Epoch millisecond of last seen moment.
     */
    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
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
     * Set how many times the user has logged in.
     *
     * No check for input.
     *
     * @param loginTimes 0 to Int.MAX
     */
    public void setLoginTimes(int loginTimes) {
        this.loginTimes = loginTimes;
    }

    /**
     * Set how many times the user has been kicked.
     *
     * No check for input.
     *
     * @param timesKicked 0 to Int.MAX
     */
    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    /**
     * Set the GM Times map containing playtime in each gamemode.
     *
     * @param gmTimes Map containing SURVIVAL, CREATIVE, ADVENTURE and SPECTATOR
     * (After 1.8) keys.
     */
    public void setGmTimes(Map<String, Long> gmTimes) {
        if (Verify.notNull(gmTimes)) {
            this.gmTimes = gmTimes;
        }
    }

    /**
     * Set the last time a Gamemode time was updated.
     *
     * @param lastGmSwapTime Epoch millisecond a gm time was updated.
     */
    public void setLastGmSwapTime(long lastGmSwapTime) {
        this.lastGmSwapTime = lastGmSwapTime;
    }

    /**
     * Set the last gamemode the user was seen in.
     *
     * @param lastGamemode gamemode.
     */
    public void setLastGamemode(String lastGamemode) {
        this.lastGamemode = lastGamemode;
    }

    /**
     * Set whether or not player is op.
     *
     * @param isOp operator?
     */
    public void setIsOp(boolean isOp) {
        this.isOp = isOp;
    }

    public void setGeolocation(String geolocation) {
        this.geolocation = geolocation;
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
     * Is the player online?
     *
     * @return true if data is cached to datacache, false if not.
     */
    public boolean isOnline() {
        return isOnline;
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
     *
     * Set when using addNickname(String)
     *
     * @return last nickname used.
     */
    public String getLastNick() {
        return lastNick;
    }

    /**
     * Set the last nickname the user has set.
     *
     * Also set when using addNickname(String)
     *
     * @param lastNick last nickname used.
     */
    public void setLastNick(String lastNick) {
        this.lastNick = lastNick;
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
//        if (this.lastPlayed != other.lastPlayed) {
//            return false;
//        }

        return this.registered == other.registered
                && this.playTime == other.playTime
                && this.loginTimes == other.loginTimes
                && this.timesKicked == other.timesKicked
                && this.lastGmSwapTime == other.lastGmSwapTime
                && this.mobKills == other.mobKills
                && this.deaths == other.deaths
                && Objects.equals(this.lastNick, other.lastNick)
                && Objects.equals(this.name, other.name)
                && Objects.equals(this.uuid, other.uuid)
                && Objects.equals(this.ips, other.ips)
                && Objects.equals(this.nicknames, other.nicknames)
                && Objects.equals(this.lastGamemode, other.lastGamemode)
                && Objects.equals(this.gmTimes, other.gmTimes)
                && Objects.equals(this.playerKills, other.playerKills)
                && Objects.equals(this.sessions, other.sessions);
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

    /**
     * Set the banned value.
     *
     * @param isBanned true/false
     */
    public void setBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    /**
     * Set the online value.
     *
     * @param isOnline true/false
     */
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getGeolocation() {
        return geolocation;
    }
}
