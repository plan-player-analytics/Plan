![Player Analytics](https://puu.sh/t8vin.png)
# Configuration

- [Default Config](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/config.yml)

This page is an in depth documentation on what each Setting does in the config.

## Specific settings
Config.Point (Version introduced)

### Locale (2.5.0)
This setting can be set to a two letter combination of the Available locales.
If a faulty combination is used, default locale will be used.  
[Available locales](https://github.com/Rsl1122/Plan-PlayerAnalytics/tree/master/Plan/localization)

### UseTextUI (3.0.0)
Redirects */plan inspect* and */plan analyze* commands to display same messages as */plan qinspect* & */plan qanalyze*

### Data.GatherLocations (2.2.0)
This setting enables saving of Locations to the database with the PlayerMoveEventListener.

----

### Analysis.LogProgressOnConsole (2.4.0)
When this setting is enabled, the plugin logs analysis phases in more detail to the console.

### Analysis.NotifyWhenFinished (3.0.0)
When this setting is enabled ["Analysis Complete"-message](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/Phrase.java#L73) will be shown on the console after analysis is complete.

### Analysis.MinutesPlayedUntilConsidiredActive (2.0.0)
This setting affects how the Analysis treats player's activity. Whether or not a player is active is determined with 3 values: Last Login, Playtime and Login Times.  
If the player has logged in in the last 2 weeks, has playtime higher than in the config, and has logged in 3 times, the player is considered active.  
Otherwise the player is counted as inactive.  
[Code responsible for determening activity](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/utilities/AnalysisUtils.java#L27)

Set to 0 to disable Playtime check & use only the other two.

----

### Cache.AnalysisCache.RefreshAnalysisCacheOnEnable (2.?.0)
This setting determines whether or not Plan will perform an Analysis 30 seconds after the server is booted/reloaded.  
Analysis Cache is calculated only if last refresh was more than 60 seconds ago when using the */plan analyze* command.  
If this setting is enabled you can view server.ip:port/server right away after the server is booted without using /plan analyze.

### Cache.AnalysisCache.RefreshEveryXMinutes (2.4.0)
This setting can be used to automatically refresh the AnalysisData in the cache, visible in the browser.  
-1 to disable.

### Cache.InspectCache.ClearFromInspectCacheAfterXMinutes
This setting affects how long the Inspect results are visible at *server.ip:port/player/<playername>* after the */plan inspect <playername>* command is used.  
After the time has passed, the data will be cleared from the InspectCache to save RAM.

### Cache.DataCache.SaveEveryXMinutes
Determines how often the UserData is saved to the Database.

### Cache.DataCache.ClearCacheEveryXSaves
The DataCache is used to save the data while the players are online.  
This data is used actively by the listeners, and is used to avoid excess stress on the database. Player's data is added to the cache upon login and removed on logout.
  
This setting tells how many saves will be done without clearing the DataCache.  
After Cache has been saved enough times, it will clear itself after a successful save.  
This is done in case some data is left lingering even after the player has been gone for a long time.

----

### WebServer.Enabled (2.1.0)
This setting is used to turn off the Webserver if multiple servers are used to collect the data (with MySQL)  
You can also use this if you want to only use the text UI.

### WebServer.Port (2.0.0)
This setting determines the Port that the webserver will be opened on. Remember to Open the Port in the server's firewall so that you can access the webserver.  
Default: 8804

### Webserver.InternalIP (3.0.0)
This setting is used to change the internal ip used when enabling the web socket server.  
Default: 0.0.0.0 (localhost)  
[Code enabling the socket server](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/ui/webserver/WebSocketServer.java#L56)

### WebServer.ShowAlternativeServerIP (2.0.0)
This setting determines whether or not the setting below is used to show alternative IP to the player, in case you don't want them to see just numbers.

### WebServer.AlternativeIP (2.0.0)
This IP is used as the address on /plan analyze and /plan inspect <playername> if the setting above is true.  
%port% will be replaced automatically with Webserver.Port  
If you have port-forwarded an alternate address to the webserver port, %port% is not required.

### WebServer.Security.DisplayIPsAndUUIDs (2.5.0)
If true, IPs and UUIDs will be visible on the player Inspect page. Otherwise "Hidden" takes their place.

### WebServer.Security.AddressSecurityCode (2.5.0)
This string is added to the url so that outsiders can not access the /server and /player/<playername> pages just by knowing your IP and that your server uses Plan.
Example: bAkEd -> localhost:8804/bAkEd/server

----

### Customization.Colors.Commands (2.1.0)
These color codes are used with all the /plan commands. 

### Customization.Colors.HTML (2.1.0)
These HTML Color codes are used when generating the graphs and piecharts.
Use without the # (hashtag)
Change requires plugin restart

### Customization.DemographicsTriggers (2.1.0)
These lists of words are used when detecting the Age & gender of the player from the chatbox. Seperate words with a comma ( , ).
*Trigger*: An attempt to gather info is only made if message contains one of these words.
*IgnoreWhen*: If an attempt is made and message contains one of these words, the info is disregarded.

----

### database.type (2.0.0)
This setting determines what type of database the plugin will use.  
sqLite - sqLite database file will be created to the /plugins/Plan/ folder and used as the save location for all data.  
MySQL - MySQL settings are used and the data is saved to the MySQL database you have installed.  

### mysql (2.0.0)
These settings are used to connect to the database when database.type is set to 'mysql'.
In order to use MySQL the table in the mysql.database has to be created 
separately

**mysql.host**  
IP of the MySQL database.  
**mysql.port**  
Port of the MySQL database  
**mysql.user**  
MySQL User  
**mysql.password**  
User's password  

### mysql.database (2.0.0)
Name of the database that has already been created.
Please note, that you have to create this database in your mysql solution (eg. MariaDB)!

## End
If you don't see explanation for a config point or need help setting up the plugin, don't hesitate to ask for help! :)
- [Ask for help with an issue](https://github.com/Rsl1122/Plan-PlayerAnalytics/issues)
- [Ask for help in the spigot thread](https://www.spigotmc.org/threads/plan-player-analytics.197391/)
