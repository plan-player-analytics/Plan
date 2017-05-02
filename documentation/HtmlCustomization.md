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
%nicknames% | Formatted Array of Users nicknames, with <span class="color_#"></span> wrapped to represent §#-color tags.
%name% | User's username | Steve
%registered% | A formatted version of the Epoch second the user registered. | Feb 02 18:03:12
%timeskicked% | Number how many times the user was kicked. | 5
%playtime% | A formatted version of milliseconds spent on the server. | 1h 30m 4s
%banned% | '<span class="color_4">Banned</span>' or nothing. | 
%op% | ', Operator (Op)' or nothing | 

**Unfinished**
