package main.java.com.djrapitops.plan.data;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class UserData {

    private int accessing;
    private boolean clearAfterSave;

    private UUID uuid;
    private Location location;
    private List<Location> locations;
    private HashSet<InetAddress> ips;
    private HashSet<String> nicknames;
    private String lastNick;
    private long registered;
    private long lastPlayed;
    private long playTime;
    private int loginTimes;
    private int timesKicked;
    private long lastGmSwapTime;
    private GameMode lastGamemode;
    private HashMap<GameMode, Long> gmTimes;
    private boolean isOp;
    private boolean isBanned;
    private DemographicsData demData;

    private int mobKills;
    private List<KillData> playerKills;
    private int deaths;

    private String name;
    private boolean isOnline;

    private SessionData currentSession;
    private List<SessionData> sessions;

    /**
     *
     * @param uuid
     * @param reg
     * @param loc
     * @param op
     * @param lastGM
     * @param demData
     * @param name
     * @param online
     */
    public UserData(UUID uuid, long reg, Location loc, boolean op, GameMode lastGM, DemographicsData demData, String name, boolean online) {
        accessing = 0;
        this.uuid = uuid;
        registered = reg;
        location = loc;
        isOp = op;
        locations = new ArrayList<>();
        nicknames = new HashSet<>();
        ips = new HashSet<>();
        gmTimes = new HashMap<>();
        long zero = 0;
        gmTimes.put(GameMode.SURVIVAL, zero);
        gmTimes.put(GameMode.CREATIVE, zero);
        gmTimes.put(GameMode.ADVENTURE, zero);
        try {
            gmTimes.put(GameMode.SPECTATOR, zero);
        } catch (NoSuchFieldError e) {
        }
        lastGamemode = lastGM;
        this.demData = demData;
        this.name = name;
        isOnline = online;
        sessions = new ArrayList<>();
        lastNick = "";
        playerKills = new ArrayList<>();
    }

    /**
     *
     * @param player
     * @param demData
     */
    public UserData(Player player, DemographicsData demData) {
        this(player.getUniqueId(), player.getFirstPlayed(), player.getLocation(), player.isOp(), player.getGameMode(), demData, player.getName(), player.isOnline());
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Log.errorMsg("Error getting ban date from Bukkit files. " + uuid.toString());
            Log.toLog(this.getClass().getName(), e);
            isBanned = false;
        }
    }

    /**
     *
     * @param player
     * @param demData
     */
    public UserData(OfflinePlayer player, DemographicsData demData) {
        this(player.getUniqueId(), player.getFirstPlayed(), null, player.isOp(), GameMode.SURVIVAL, demData, player.getName(), player.isOnline());
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Log.errorMsg("Error getting ban date from Bukkit files. " + uuid.toString());
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
        this.location = data.getLocation();
        this.locations = new ArrayList<>();
        locations.addAll(data.getLocations());
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
        DemographicsData dem = data.getDemData();
        if (dem == null) {
            dem = new DemographicsData();
        }
        this.demData = new DemographicsData(dem.getAge(), dem.getGender(), dem.getGeoLocation());
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
        return "{" + "accessing:" + accessing + "|uuid:" + uuid + "|location:" + location + "|locations:" + locations.size() + "|ips:" + ips + "|nicknames:" + nicknames + "|lastNick:" + lastNick + "|registered:" + registered + "|lastPlayed:" + lastPlayed + "|playTime:" + playTime + "|loginTimes:" + loginTimes + "|timesKicked:" + timesKicked + "|lastGmSwapTime:" + lastGmSwapTime + "|lastGamemode:" + lastGamemode + "|gmTimes:" + gmTimes + "|isOp:" + isOp + "|isBanned:" + isBanned + "|demData:" + demData + "|mobKills:" + mobKills + "|playerKills:" + playerKills + "|deaths:" + deaths + "|name:" + name + "|isOnline:" + isOnline + "|currentSession:" + currentSession + "|sessions:" + sessions + '}';
    }

    /**
     *
     * @param ip
     */
    public void addIpAddress(InetAddress ip) {
        if (ip != null && !ips.contains(ip)) {
            ips.add(ip);
        }
    }

    /**
     *
     * @param addIps
     */
    public void addIpAddresses(Collection<InetAddress> addIps) {
        ips.addAll(addIps.stream().filter(ip -> ip != null).collect(Collectors.toList()));

    }

    /**
     *
     * @param loc
     */
    public void addLocation(Location loc) {
        if (loc != null) {
            locations.add(loc);
            location = loc;
        }
    }

    /**
     *
     * @param addLocs
     */
    public void addLocations(Collection<Location> addLocs) {
        if (!addLocs.isEmpty()) {
            List<Location> locs = addLocs.stream().filter(l -> l != null).collect(Collectors.toList());
            locations.addAll(locs);
            location = locations.get(locations.size() - 1);
        }
    }

    /**
     *
     * @param nick
     * @return
     */
    public boolean addNickname(String nick) {
        if (!nicknames.contains(nick)) {
            if (nick != null) {
                if (!nick.isEmpty()) {
                    nicknames.add(nick);
                    lastNick = nick;
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param addNicks
     */
    public void addNicknames(Collection<String> addNicks) {
        nicknames.addAll(addNicks.stream().filter(nick -> nick != null).collect(Collectors.toList()));
    }

    /**
     *
     * @param gm
     * @param time
     */
    public void setGMTime(GameMode gm, long time) {
        if (gmTimes == null) {
            gmTimes = new HashMap<>();
        }
        if (gm != null) {
            gmTimes.put(gm, time);
        }
    }

    /**
     *
     * @param survivalTime
     * @param creativeTime
     * @param adventureTime
     * @param spectatorTime
     */
    public void setAllGMTimes(long survivalTime, long creativeTime, long adventureTime, long spectatorTime) {
        gmTimes.clear();
        gmTimes.put(GameMode.SURVIVAL, survivalTime);
        gmTimes.put(GameMode.CREATIVE, creativeTime);
        gmTimes.put(GameMode.ADVENTURE, adventureTime);
        try {
            gmTimes.put(GameMode.SPECTATOR, spectatorTime);
        } catch (NoSuchFieldError e) {
        }
    }

    /**
     *
     * @param session
     */
    public void addSession(SessionData session) {
        if (session != null && session.isValid()) {
            sessions.add(session);
        }
    }

    /**
     *
     * @param sessions
     */
    public void addSessions(Collection<SessionData> sessions) {
        Collection<SessionData> filteredSessions = sessions.stream()
                .filter(session -> session != null)
                .filter(session -> session.isValid())
                .collect(Collectors.toList());
        if (sessions.size() != filteredSessions.size()) {
            Log.debug("Some sessions were filtered! "+getUuid()+": Org:"+sessions.size()+" Fil:"+filteredSessions.size());
        }
        this.sessions.addAll(filteredSessions);
    }

    /**
     *
     * @param session
     */
    public void setCurrentSession(SessionData session) {
        currentSession = session;
    }

    /**
     *
     * @return
     */
    public SessionData getCurrentSession() {
        return currentSession;
    }

    /**
     *
     * @param isBanned
     */
    public void updateBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    /**
     *
     * @return
     */
    public boolean isAccessed() {
        return accessing > 0;
    }

    /**
     *
     */
    public void access() {
        accessing++;
    }

    /**
     *
     */
    public void stopAccessing() {
        accessing--;
    }

    // Getters -------------------------------------------------------------
    /**
     *
     * @return
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     *
     * @return
     */
    public Location getLocation() {
        return location;
    }

    /**
     *
     * @return
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     *
     * @return
     */
    public HashSet<InetAddress> getIps() {
        return ips;
    }

    /**
     *
     * @return
     */
    public HashSet<String> getNicknames() {
        return nicknames;
    }

    /**
     *
     * @return
     */
    public long getRegistered() {
        return registered;
    }

    /**
     *
     * @return
     */
    public long getLastPlayed() {
        return lastPlayed;
    }

    /**
     *
     * @return
     */
    public long getPlayTime() {
        return playTime;
    }

    /**
     *
     * @return
     */
    public int getLoginTimes() {
        return loginTimes;
    }

    /**
     *
     * @return
     */
    public int getTimesKicked() {
        return timesKicked;
    }

    /**
     *
     * @return
     */
    public HashMap<GameMode, Long> getGmTimes() {
        if (gmTimes == null) {
            gmTimes = new HashMap<>();
        }
        return gmTimes;
    }

    /**
     *
     * @return
     */
    public long getLastGmSwapTime() {
        return lastGmSwapTime;
    }

    /**
     *
     * @return
     */
    public GameMode getLastGamemode() {
        return lastGamemode;
    }

    /**
     *
     * @return
     */
    public boolean isOp() {
        return isOp;
    }

    /**
     *
     * @return
     */
    public boolean isBanned() {
        return isBanned;
    }

    /**
     *
     * @return
     */
    public DemographicsData getDemData() {
        return demData;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    // Setters -------------------------------------------------------------
    /**
     *
     * @param uuid
     */
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @param location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     *
     * @param locations
     */
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    /**
     *
     * @param ips
     */
    public void setIps(HashSet<InetAddress> ips) {
        this.ips = ips;
    }

    /**
     *
     * @param nicknames
     */
    public void setNicknames(HashSet<String> nicknames) {
        this.nicknames = nicknames;
    }

    /**
     *
     * @param registered
     */
    public void setRegistered(long registered) {
        this.registered = registered;
    }

    /**
     *
     * @param lastPlayed
     */
    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    /**
     *
     * @param playTime
     */
    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    /**
     *
     * @param loginTimes
     */
    public void setLoginTimes(int loginTimes) {
        this.loginTimes = loginTimes;
    }

    /**
     *
     * @param timesKicked
     */
    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    /**
     *
     * @param gmTimes
     */
    public void setGmTimes(HashMap<GameMode, Long> gmTimes) {
        this.gmTimes = gmTimes;
    }

    /**
     *
     * @param lastGmSwapTime
     */
    public void setLastGmSwapTime(long lastGmSwapTime) {
        this.lastGmSwapTime = lastGmSwapTime;
    }

    /**
     *
     * @param lastGamemode
     */
    public void setLastGamemode(GameMode lastGamemode) {
        this.lastGamemode = lastGamemode;
    }

    /**
     *
     * @param isOp
     */
    public void setIsOp(boolean isOp) {
        this.isOp = isOp;
    }

    /**
     *
     * @param demData
     */
    public void setDemData(DemographicsData demData) {
        this.demData = demData;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     *
     * @return
     */
    public int getMobKills() {
        return mobKills;
    }

    /**
     *
     * @param mobKills
     */
    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    /**
     *
     * @return
     */
    public List<KillData> getPlayerKills() {
        return playerKills;
    }

    /**
     *
     * @param playerKills
     */
    public void setPlayerKills(List<KillData> playerKills) {
        this.playerKills = playerKills;
    }

    /**
     *
     * @param kill
     */
    public void addPlayerKill(KillData kill) {
        playerKills.add(kill);
    }

    /**
     *
     * @return
     */
    public int getDeaths() {
        return deaths;
    }

    /**
     *
     * @param deaths
     */
    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    /**
     *
     * @return
     */
    public List<SessionData> getSessions() {
        return sessions;
    }

    /**
     *
     * @return
     */
    public String getLastNick() {
        return lastNick;
    }

    /**
     *
     * @param lastNick
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
        if (this.registered != other.registered) {
            return false;
        }
//        if (this.lastPlayed != other.lastPlayed) {
//            return false;
//        }
        if (this.playTime != other.playTime) {
            return false;
        }
        if (this.loginTimes != other.loginTimes) {
            return false;
        }
        if (this.timesKicked != other.timesKicked) {
            return false;
        }
        if (this.lastGmSwapTime != other.lastGmSwapTime) {
            return false;
        }
        if (this.mobKills != other.mobKills) {
            return false;
        }
        if (this.deaths != other.deaths) {
            return false;
        }
        if (!Objects.equals(this.lastNick, other.lastNick)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.ips, other.ips)) {
            return false;
        }
        if (!Objects.equals(this.nicknames, other.nicknames)) {
            return false;
        }
        if (this.lastGamemode != other.lastGamemode) {
            return false;
        }
        if (!Objects.equals(this.gmTimes, other.gmTimes)) {
            return false;
        }
        if (!Objects.equals(this.playerKills, other.playerKills)) {
            return false;
        }
        if (!Objects.equals(this.sessions, other.sessions)) {
            return false;
        }
        return true;
    }

    public boolean shouldClearAfterSave() {
        return clearAfterSave;
    }

    public void setClearAfterSave(boolean clearAfterSave) {
        this.clearAfterSave = clearAfterSave;
    }
}
