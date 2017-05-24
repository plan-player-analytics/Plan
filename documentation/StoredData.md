![Player Analytics](https://puu.sh/t8vin.png)
# Stored Data & Data format

This Article is about the information stored in the Database.

## Stored Data & Format

Data | Format in DB | Location in DB | Description
-- | -- | ---- | --
UUID | varchar | plan_users | UUID of the player, used for saving & processing all the information
Age | integer | plan_users | Age of the player, gathered from chat. -1 if not known
Gender | varchar | plan_users | String representation of the [Gender Enum](/Plan/src/main/java/com/djrapitops/plan/api/Gender.java)
Geolocation | varchar | plan_users | Geolocation of the player
Last Gamemode | varchar | plan_users | Last Gamemode the user was seen in. Used for GMTime calculations.
Last GM Swap Time | bigint | plan_users | Playtime ms the user swapped their gamemode. Used for GMTime calculations.
Playtime | bigint | plan_users | Total playtime of the player
Login Times | integer | plan_users | How many times the player has joined.
Last Played | bigint | plan_users | Last time the player's data was processed, used for Playtime calculations
Nicknames | varchar | plan_nicknames | All the nicknames the player has used
Last nickname | boolean | plan_nicknames | The last nickname known stated as a boolean.
IPs | varchar | plan_ips | InetAddress the player connected from
GM Times | bigint | plan_gamemodetimes | Time spent in each Gamemode
Locations | integer, integer, String | plan_locations | X, Z & World name the player has visited.
Mob kills | integer | plan_users | Mobs the player has slain
Deaths | integer | plan_users | How many times the player has died
Player kills & Weapon used | integer, varchar, bigint, string | plan_kills | User the player killed, the time of the kill & the name of MATERIAL of the weapon.
SessionData (Start & End of session) | bigint, bigint | plan_sessions | Used for all sorts of activity calculation.
Command Usage | varchar | plan_commandusages | Each base command & how many times they have been used.

### Bukkit Player Data
Some information is taken from Bukkit's Player or OfflinePlayer objects:
- UUID (Used for saving & processing)
- Register Date
- Is the player banned
- Is the player opped
- Is the player online
