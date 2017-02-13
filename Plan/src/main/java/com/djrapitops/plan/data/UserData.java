package com.djrapitops.plan.data;

import com.djrapitops.plan.database.Database;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import main.java.com.djrapitops.plan.data.SessionData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class UserData {

    private boolean isAccessed;

    private UUID uuid;
    private Location location;
    private List<Location> locations;
    private HashSet<InetAddress> ips;
    private HashSet<String> nicknames;
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
    private int playerKills;
    private int deaths;

    private boolean planLiteFound;
    private PlanLitePlayerData planLiteData;

    private String name;
    private boolean isOnline;

    private SessionData currentSession;
    private List<SessionData> sessions;

    public UserData(Player player, DemographicsData demData, Database db) {
        uuid = player.getUniqueId();
        registered = player.getFirstPlayed();
        location = player.getLocation();
        isOp = player.isOp();
        locations = new ArrayList<>();
        nicknames = new HashSet<>();
        ips = new HashSet<>();
        gmTimes = new HashMap<>();
        long zero = Long.parseLong("0");
        gmTimes.put(GameMode.SURVIVAL, zero);
        gmTimes.put(GameMode.CREATIVE, zero);
        gmTimes.put(GameMode.ADVENTURE, zero);
        try {
            gmTimes.put(GameMode.SPECTATOR, zero);
        } catch (NoSuchFieldError e) {
        }
        lastGamemode = player.getGameMode();
        this.demData = demData;
        isBanned = player.isBanned();
        name = player.getName();
        isOnline = player.isOnline();
        sessions = new ArrayList<>();
    }

    public UserData(OfflinePlayer player, DemographicsData demData, Database db) {
        uuid = player.getUniqueId();
        registered = player.getFirstPlayed();
        isOp = player.isOp();
        locations = new ArrayList<>();
        nicknames = new HashSet<>();
        ips = new HashSet<>();
        gmTimes = new HashMap<>();
        long zero = Long.parseLong("0");
        gmTimes.put(GameMode.SURVIVAL, zero);
        gmTimes.put(GameMode.CREATIVE, zero);
        gmTimes.put(GameMode.ADVENTURE, zero);
        try {
            gmTimes.put(GameMode.SPECTATOR, zero);
        } catch (NoSuchFieldError e) {
        }
        this.demData = demData;
        isBanned = player.isBanned();
        name = player.getName();
        isOnline = player.isOnline();
        sessions = new ArrayList<>();
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

    public void addNickname(String nick) {
        if (!nicknames.contains(nick)) {
            nicknames.add(nick);
        }
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
        sessions.add(session);
    }

    public void addSessions(Collection<SessionData> sessions) {
        this.sessions.addAll(sessions);
    }

    public void startSession(long startTime) {
        currentSession = new SessionData(startTime);
    }

    public void endSession(long endTime) {
        if (currentSession != null) {
            currentSession.endSession(endTime);
            addSession(currentSession);
        } else {
            System.out.println("Player's session was initialized in a wrong way! (" + name + ")");
        }
    }

    public void updateBanned(Player p) {
        isBanned = p.isBanned();
    }

    public boolean isAccessed() {
        return isAccessed;
    }

    public void setAccessing(boolean value) {
        isAccessed = value;
    }

    // Getters -------------------------------------------------------------
    public boolean isPlanLiteFound() {
        return planLiteFound;
    }

    public void setPlanLiteFound(boolean planLiteFound) {
        this.planLiteFound = planLiteFound;
    }

    public PlanLitePlayerData getPlanLiteData() {
        return planLiteData;
    }

    public void setPlanLiteData(PlanLitePlayerData planLiteData) {
        this.planLiteData = planLiteData;
    }

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

    public int getPlayerKills() {
        return playerKills;
    }

    public void setPlayerKills(int playerKills) {
        this.playerKills = playerKills;
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
}
