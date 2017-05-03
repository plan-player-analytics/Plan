![Player Analytics](https://puu.sh/t8vin.png)
# Html Customization
The html web pages of the plugin can be completely customized.
The plugin uses two .html files: `analysis.html` and `player.html`
If the `/plugins/Plan/` folder contains either of the files, they will be used instead of the ones found inside the .jar.
This means you can copy the html files from the jar to the folder and edit them.

## Placeholders
The plugin uses placeholders to place the values into the html. Here I will go through each placeholder.
- [PlaceholderUtils.java](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/utilities/PlaceholderUtils.java)

## Inspect placeholders

Placeholder | Description | Example
---------- | ------------------------------------- | -----
%uuid% | Players UUID or 'Hidden' if config setting for UUID visibility is disabled. | 88493cd1-567a-49aa-acaa-84197b5de595
%lastseen% | A formatted version of the last Epoch second the user was seen. | Feb 02 18:03:12
%logintimes% | How many times the user has logged in | 34
%geoloc% | Demographics geolocation of the user. | United States
%active% | 'Player is Active' or 'Player is Inactive' depending on [isActive](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/utilities/AnalysisUtils.java#L27)- method. | 
%age% | 'Not Known' if age is not known (-1) or the users age. | 14
%gender% | 'Unknown', 'Male' or 'Female' | 
%gm0% | A formatted version of milliseconds spent in SURVIVAL. | 1h 30m 4s
%gm1% | A formatted version of milliseconds spent in CREATIVE. | 1h 30m 4s
%gm2% | A formatted version of milliseconds spent in ADVENTURE. | 1h 30m 4s
%gm3% | A formatted version of milliseconds spent in SPECTATOR. | 1h 30m 4s
%gmdata% | Number array of seconds spent in each gamemode, used by piechart. | [32423, 5436, 432543, 23]
%gmlables% | Array of labels used by piechart. | ["Survival", "Creative", "Adventure", "Spectator"] 
%gmcolors% | List of html color codes that depend on config values. | "#ffffff","#eeeeee","#000000","#213123"
%gmtotal% | A formatted version of milliseconds spent in All gamemodes. | 1h 30m 4s
%ips& | An array of users ip addresses or 'Hidden'. | [127.0.0.1/]
%nicknames% | Formatted Array of Users nicknames, with `<span class="color_#"></span>` wrapped to represent §#-color tags. | [Steve, `<span class="color_4">Steve</span>`]
%name% | User's username | Steve
%registered% | A formatted version of the Epoch second the user registered. | Feb 02 18:03:12
%timeskicked% | Number how many times the user was kicked. | 5
%playtime% | A formatted version of milliseconds spent on the server. | 1h 30m 4s
%banned% | `<span class="color_4">Banned</span>` or nothing. | 
%op% | ', Operator (Op)' or nothing | 
%isonline& | `| <span class="color_2">Online</span>` or `| <span class="color_4">Offline</span>`
%deaths% | Number of deaths. | 24
%playerkills% | Number of Player kills the user has (Size of KillData list) | 14
%sessionstable% | Table containing up to 10 of the most recent online sessions. Example contains one line. | `<table class="sortable table"><thead><tr><th>Session Started</th><th>Session Ended</th><th>Session Length</th></tr></thead><tbody><tr><td sorttable_customkey="32674576">FORMATTED_TIME</td><td sorttable_customkey="432525345">FORMATTED_TIME</td><td sorttable_customkey="32213">FORMATTED_TIME</td></tr></tbody></table>`
%sessionaverage% | A formatted version of the average length of all of the sessions. | 10m 23s
%killstable% | Table containing up to 10 of the most recent player kills. Example contains one line. | `<table class="sortable table"><thead><tr><th>Date</th><th>Killed</th><th>With</th></tr></thead><tbody><tr><td sorttable_customkey="324123421">FORMATTED_TIME</td><td>Rsl1122</td><td>DIAMOND_SWORD</td></tr></tbody></table>`
%version% | Version of the plugin | 3.2.5
%planlite% | Replaced with an empty string. Old feature. | 
%dataweek% | Array containing users online numbers for last 7 days, used by the graph. | [0, 0, 1, 1, 1, 0, 0, 1, 0, 0]
%labelsweek% | Array containing formatted time labels corresponding the data array. | ["Feb 02 18:03:12", "Feb 02 18:06:32"]
%playersgraphcolor% | Color code for the online graph in the config. | ffffff
%playersgraphfill% | Color code for fill of the online graph in the config. | 000000
%gm0col% | Color of the SURVIVAL box in the config | ffffff
%gm1col% | Color of the CREATIVE box in the config | 000000
%gm2col% | Color of the ADVENTURE box in the config | ffffff
%gm3col% | Color of the SPECTATOR box in the config | 000000
%inaccuratedatawarning% | Replaced with a warning if the player has registered 3 minutes ago | `<div class="warn">Data might be inaccurate, player has just registered.</div>`

## Additional Inspect placeholders

Additionally some placeholders will be replaced with plugin data, these can be found here:
[HookHandler # getAdditionalInspectReplaceRules](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/data/additional/HookHandler.java#L145)

These might change in **3.1.0** so I will not put them here yet.

## Analysis placeholders

Placeholder | Description | Example
---------- | ------------------------------------- | -----
%gm0% | Total percentage all players have spent in SURVIVAL | 66%
%gm1% | Total percentage all players have spent in CREATIVE | 19%
%gm2% | Total percentage all players have spent in ADVENTURE | 10%
%gm3% | Total percentage all players have spent in SPECTATOR | 5%
%active% | Number of [Active](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/utilities/AnalysisUtils.java#L27) players | 4
%inactive% | Number of Inactive players. | 43
%banned% | Number of Banned players. | 5
%joinleaver% | Number of players who have only joined once | 100
%activitytotal% | Total number of players in the database. | 152
%npday% | Number of new players who have joined in the last 24h | 5
%npweek% | Number of new players who have joined in the last 7d | 13
%npmonth% | Number of new players who have joined in the last 30d | 53
%commanduse% | Table format lines for commands & times used, sorted to descending order. | `<tr><td><b>/spawn</b></td><td>42</td></tr><tr><td><b>/plan</b></td><td>4</td></tr>`
%totalcommands% | Number of unique commands used | 2
%avgage% | 'Not Known' or an average of known user ages. | 14.5
%avgplaytime% | Formatted average playtime of all players. | 1h 30m 4s
%totalplaytime% | Formatted total playtime of all players. | 4d 2h 43m 5s
%op% | Amount of Operators on the server | 1
%refresh% | Formatted time since Epoch second of the last refresh. | 4m 5s
%totallogins% | Total number of logins of all players | 342
%top20mostactive% | Not in use, old feature. | Error: Replace rule was not set
%recentlogins% | Buttons with links to the inspect pages of most recent players | `<p><a class="button" href="http://localhost:8804/bAkEd/player/Rsl1122">Rsl1122</a> </p>`

**Unfinished**

## Additional Analysis placeholders

Additionally some placeholders will be replaced with plugin data, these can be found here:
[HookHandler # getAdditionalAnalysisReplaceRules](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/data/additional/HookHandler.java#L127)

These might change in **3.1.0** so I will not put them here yet.
