package main.java.com.djrapitops.plan.data;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class UserData {

    private int accessing;

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

    public UserData(Player player, DemographicsData demData) {
        accessing = 0;
        uuid = player.getUniqueId();
        registered = player.getFirstPlayed();
        location = player.getLocation();
        isOp = player.isOp();
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
        lastGamemode = player.getGameMode();
        this.demData = demData;
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Plan plugin = getPlugin(Plan.class);
            plugin.logError("Error getting ban date from Bukkit files. "+uuid.toString());
            plugin.toLog(this.getClass().getName(), e);
        }
        name = player.getName();
        isOnline = player.isOnline();
        sessions = new ArrayList<>();
        lastNick = "";
        playerKills = new ArrayList<>();
    }

    public UserData(OfflinePlayer player, DemographicsData demData) {
        accessing = 0;
        uuid = player.getUniqueId();
        registered = player.getFirstPlayed();
        isOp = player.isOp();
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
        this.demData = demData;
        try {
            isBanned = player.isBanned();
        } catch (Exception e) {
            Plan plugin = getPlugin(Plan.class);
            plugin.logError("Error getting ban date from Bukkit files. "+uuid.toString());
            plugin.toLog(this.getClass().getName(), e);
        }
        name = player.getName();
        isOnline = player.isOnline();
        sessions = new ArrayList<>();
        lastNick = "";
        playerKills = new ArrayList<>();
    }

    
    public void addIpAddress(InetAddress ip) {
        if (!ips.contains(ip)) {
            ips.add(ip);
        }
    }

    public void addIpAddresses(Collection<InetAddress> addIps) {
        ips.addAll(addIps);
    }

    public void addLocation(Location loc) {
        locations.add(loc);
        location = loc;
    }

    public void addLocations(Collection<Location> addLocs) {
        locations.addAll(addLocs);
        if (!locations.isEmpty()) {
            location = locations.get(locations.size() - 1);
        }
    }

    public boolean addNickname(String nick) {
        if (!nicknames.contains(nick)) {
            if (!nick.isEmpty()) {
                nicknames.add(nick);
                return true;
            }
        }
        return false;
    }

    public void addNicknames(Collection<String> addNicks) {
        nicknames.addAll(addNicks);
    }

    public void setGMTime(GameMode gm, long time) {
        gmTimes.put(gm, time);
    }

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

    public void addSession(SessionData session) {
        if (session != null) {
            sessions.add(session);
        }
    }

    public void addSessions(Collection<SessionData> sessions) {
        Collection<SessionData> filteredSessions = sessions.parallelStream()
                .filter(session -> session != null)
                .collect(Collectors.toList());
        this.sessions.addAll(filteredSessions);
    }

    public void setCurrentSession(SessionData session) {
        currentSession = session;
    }

    public void updateBanned(boolean isBanned) {
        this.isBanned = isBanned;
    }

    public boolean isAccessed() {
        return accessing > 0;
    }

    public void access() {
        accessing++;
    }
    
    public void stopAccessing() {
        accessing--;
    }

    // Getters -------------------------------------------------------------

    public UUID getUuid() {
        return uuid;
    }

    public Location getLocation() {
        return location;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public HashSet<InetAddress> getIps() {
        return ips;
    }

    public HashSet<String> getNicknames() {
        return nicknames;
    }

    public long getRegistered() {
        return registered;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public long getPlayTime() {
        return playTime;
    }

    public int getLoginTimes() {
        return loginTimes;
    }

    public int getTimesKicked() {
        return timesKicked;
    }

    public HashMap<GameMode, Long> getGmTimes() {
        return gmTimes;
    }

    public long getLastGmSwapTime() {
        return lastGmSwapTime;
    }

    public GameMode getLastGamemode() {
        return lastGamemode;
    }

    public boolean isOp() {
        return isOp;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public DemographicsData getDemData() {
        return demData;
    }

    public String getName() {
        return name;
    }

    // Setters -------------------------------------------------------------
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void setIps(HashSet<InetAddress> ips) {
        this.ips = ips;
    }

    public void setNicknames(HashSet<String> nicknames) {
        this.nicknames = nicknames;
    }

    public void setRegistered(long registered) {
        this.registered = registered;
    }

    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public void setLoginTimes(int loginTimes) {
        this.loginTimes = loginTimes;
    }

    public void setTimesKicked(int timesKicked) {
        this.timesKicked = timesKicked;
    }

    public void setGmTimes(HashMap<GameMode, Long> gmTimes) {
        this.gmTimes = gmTimes;
    }

    public void setLastGmSwapTime(long lastGmSwapTime) {
        this.lastGmSwapTime = lastGmSwapTime;
    }

    public void setLastGamemode(GameMode lastGamemode) {
        this.lastGamemode = lastGamemode;
    }

    public void setIsOp(boolean isOp) {
        this.isOp = isOp;
    }

    public void setDemData(DemographicsData demData) {
        this.demData = demData;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public int getMobKills() {
        return mobKills;
    }

    public void setMobKills(int mobKills) {
        this.mobKills = mobKills;
    }

    public List<KillData> getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(List<KillData> playerKills) {
        this.playerKills = playerKills;
    }
    
    public void addPlayerKill(KillData kill) {
        playerKills.add(kill);
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public List<SessionData> getSessions() {
        return sessions;
    }

    public String getLastNick() {
        return lastNick;
    }

    public void setLastNick(String lastNick) {
        this.lastNick = lastNick;
    }
}
