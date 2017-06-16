![Player Analytics](https://puu.sh/t8vin.png)
# Configuration

- [Default Config](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/config.yml)
- [Settings Enum](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/Settings.java)

This page is an in depth documentation on what each Setting does in the config.

# Settings

## Basic settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- | ---------------------------------
Debug | 3.0.0 | boolean | false | Enables debug messages on console.
Locale | 2.5.0 | String | default | Two letter Locale combination. Can be set to one of the Available locales. If a faulty combination is used, default locale will be used. [Available locales](https://github.com/Rsl1122/Plan-PlayerAnalytics/tree/master/Plan/localization)
UseTextUI | 3.0.0 | boolean | false | Redirects */plan inspect* and */plan analyze* commands to display same messages as */plan qinspect* & */plan qanalyze*
Data.GatherLocations | 2.2.0 | boolean | true | Enables [PlanPlayerMoveEventListener](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/data/listeners/PlanPlayerMoveListener.java)
Data.ChatListener | 3.4.2 | boolean | true | Enables Chat listener
Data.GatherKillData | 3.4.2 | boolean | true | Enables Death listener
Data.GamemodeChangeListener | 3.4.2 | boolean | true | Enables Gamemode Change listener
Data.GatherCommandUsage | 3.4.2 | boolean | true | Enables CommandPreprocess listener

## Analysis settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- |  ---------------------------------
LogProgressOnConsole | 2.4.0 | boolean | false | More detailed analysis progress to console.
NotifyWhenFinished | 3.0.0 | boolean | true | Enables ["Analysis Complete"-message](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/Phrase.java#L73) will be shown on the console after analysis is complete.
MinutesPlayedUntilConsidiredActive | 2.0.0 | Integer | 10 | This setting affects how the Analysis treats player's activity. Whether or not a player is active is determined with 3 values: Last Login, Playtime and Login Times. If the player has logged in in the last 2 weeks, has playtime higher than in the config, and has logged in 3 times, the player is considered active. Otherwise the player is counted as inactive.
RemoveOutliersFromVisualization | 3.4.0 | boolean | true | This setting attempts to remove big spikes from data visualization.
Export.Enabled | 3.4.0 | boolean | false | Enables export of html pages after analysis
Export.DestinationFolder | 3.4.0 | String | 'Analysis Results' | Path to the export folder. Will be created if doesn't exist. If contains ':' will be regarded as full filepath.

## Cache settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- | ---------------------------------
Processing.GetLimit | 2.8.0 | Integer | 2000 | Changes the queue size for database get actions. If queue runs out notification is given on console.
Processing.SaveLimit | 2.8.0 | Integer | 1000 | Changes the queue size for database save actions. If queue runs out notification is given on console.
Processing.ClearLimit | 2.8.0 | Integer | 1000 | Changes the queue size for clearing datacache. If queue runs out notification is given on console.
AnalysisCache.RefreshAnalysisCacheOnEnable | 2.?.0 | boolean | true | Enables Analysis refresh 30 seconds after boot/reload
AnalysisCache.RefreshEveryXMinutes | 2.4.0 | Integer | -1 | Enables periodic Analysis refresh, -1 to disable
DataCache.SaveEveryXMinutes | 2.0.0 | Integer | 2 | Determines how often cache is saved to the Database.
DataCache.ClearCacheEveryXSaves | 2.0.0 | Integer | 5 | Determines how often cache clear attempt is made. This is done in case some data is left lingering even after the player has been gone for a long time.

## WebServer settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- | ---------------------------------
Enabled | 2.1.0 | boolean | true | Enables the Webserver
Port | 2.0.0 | Integer | 8804 | Port of the Webserver
InternalIP | 3.0.0 | String | 0.0.0.0 | Internal InetAddress to start the WebSocketServer on. [Code enabling the socket server](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/ui/webserver/WebSocketServer.java#L56)
ShowAlternativeServerIP | 2.0.0 | boolean | false | Enables the use of the link below in the inspect and analyze commands.
AlternativeIP | 2.0.0 | String | `your.ip.here:%port%` | Address to use as link in inspect and analyze commands if setting above is enabled. %port% will be replaced with the Port automatically. If you have port-forwarded an alternate address to the webserver port, %port% is not required.
LinkProtocol | 3.4.3 | String | http | Changes the protocol in links given by the commands. SHOULD NOT BE CHANGED IF NOT USING EXTERNAL WEBSERVER.
Security.DisplayIPsAndUUIDs | 2.5.0 | boolean | true | Toggles visibility of UUIDs and IPs on player Inspect page.
Security.AddressSecurityCode | 2.5.0 | String | bAkEd | This string is added to the url so that outsiders can not access the `/server` and `/player/<playername>` pages just by knowing your IP and that your server uses Plan. Example: bAkEd -> `localhost:8804/bAkEd/server`

## Customization settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- | ---------------------------------
ServerName | 3.3.0 | String | 'Plan' | Changes the Name in the Header of Analysis & Inspect pages.
Graphs.PlayersOnlineGraph.UseMaxPlayersAsScale | 3.4.2 | boolean | true | Determines whether or not to use max players in server.yml as the default scale for Players Online graphs.
Formats.TimeAmount | 3.3.0 | String | '%days%d ' | Changes the format used when formatting Time Amounts. Include %zero% to add a 0 in front of single numbers.
Formats.DecimalPoints | 3.3.0 | String | #.## | Changes how many decimals are displayed after doubles. (For 3 use #.### etc.)
Colors.Commands | 2.1.0 | String |  | Color codes used with the */plan* commands. 
Colors.HTML | 2.1.0 | String |  | These HTML Color codes are used when generating the Html pages. Use without the # (hashtag)
DemographicsTriggers.Trigger | 2.1.0 | String |  | An attempt to gather info is only made if message contains one of these words.
DemographicsTriggers.IgnoreWhen | 2.1.0 | String |  | If an attempt is made and message contains one of these words, the info is disregarded.
Plugins.Enabled | 3.3.0 | boolean | true | Settings for enabling hooks to plugins (For Plugins tab)
Plugins.Factions.HideFactions | 3.1.0 | String list | - ExampleFaction | Add a list of Faction names you don't want to show up on the Analysis page.
Plugins.Towny.HideTowns | 3.1.0 | String list | - ExampleTown | Add a list of Town names you don't want to show up on the Analysis page.

## Database settings

Config.Point | Version introduced | Type | Default | Description
--- | ---- | ------ | --- | ---------------------------------
database.type | 2.0.0 | String | sqlite | Determines the type of database the plugin will use. **sqLite** - sqLite database file will be created. **MySQL** - MySQL settings are used.
mysql.host | 2.0.0 | String | localhost | IP of the MySQL-database
mysql.port | 2.0.0 | Integer | 3306 | Port of the MySQL-database
mysql.user | 2.0.0 | String | root | MySQL user
mysql.password | 2.0.0 | String | minecraft | User's password
mysql.database | 2.0.0 | String | Plan | Name of the database that has already been created. Please note, that you have to create this database in your mysql solution (eg. MariaDB)!

## End
If you don't see explanation for a config point or need help setting up the plugin, don't hesitate to ask for help! :)
- [Ask for help with an issue](https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/new)
- [Ask for help in the spigot thread](https://www.spigotmc.org/threads/plan-player-analytics.197391/)
