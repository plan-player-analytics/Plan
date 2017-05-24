![Player Analytics](https://puu.sh/t8vin.png)
# Gathering the Data & Datacache

This article is an in depth explanation how the data Plan stores is gathered.

[Stored Data & Data Format](/documentation/StoredData.md)

## Bukkit Player data
Some information is taken from Bukkit's Player or OfflinePlayer objects:
- UUID (Used for saving & processing)
- Register Date
- Is the player banned
- Is the player opped
- Is the player online

## Listeners
[Listener classes](/Plan/src/main/java/com/djrapitops/plan/data/listeners)

Inside Plan, there are 6 listeners:
- Chat Listener*, Nickname & Demographics information
- Command Listener, Command Usage
- Death Listener*, Kills & Deaths
- Gamemode Change Listener*, Gamemode time calculation
- Player Listener*, Join, Leave & Kick
- Move Listener, Locations

*When an event is fired, the information contained within it is placed inside  [HandlingInfo objects](/Plan/src/main/java/com/djrapitops/plan/data/handling/info) related to the event.
This object is passed to the [DataCacheProcessQueue](/Plan/src/main/java/com/djrapitops/plan/data/cache/queue/DataCacheProcessQueue.java), where it will be processed.

## Processing
The HandlinInfo object has a process(UserData)-method to modify UserData.  
When the processing queue starts processing the data, it asks the cache for the UserData object of the player. If the Cache doesn't have the object, it will be fetched from the dabase & placed into the cache.  
After the modification, the data will be saved back into the database sometime in the future.

## Cache
[Cache classes](/Plan/src/main/java/com/djrapitops/plan/data/cache)

There are 3 Caches.
- DataCache, The active cache where data can be modified, fetched to & saved from while the player is online.
- InspectCache, Cache that stores the data of individual players when they need to be viewed on the web or for Analysis. Changes are not saved.
- AnalysisCache, Cache that stores the results of Analysis for fast access for the webserver.

### Storage in memory:
- Players data: [UserData object](/Plan/src/main/java/com/djrapitops/plan/data/UserData.java)
- Analysis data: [AnalysisData object](/Plan/src/main/java/com/djrapitops/plan/data/AnalysisData.java)
- Command Usage: [DataCacheHandler](/Plan/src/main/java/com/djrapitops/plan/data/cache/DataCacheHandler.java#L52)
- Locations: [LocationCache](/Plan/src/main/java/com/djrapitops/plan/data/cache/LocationCache.java)
- Active Sessions: [SessionCache](/Plan/src/main/java/com/djrapitops/plan/data/cache/SessionCache.java)
