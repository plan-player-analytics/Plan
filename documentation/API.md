![Player Analytics](https://puu.sh/t8vin.png)
# API

- [API class](/Plan/src/main/java/com/djrapitops/plan/api/API.java)
- [API Javadocs](https://rsl1122.github.io/Plan-PlayerAnalytics/main/java/com/djrapitops/plan/api/API.html)

## Accessing the API Methods:
Install the Plan.jar as a local dependency if you're using Maven.
Add soft-depend or depend in the plugin.yml

```
import main.java.com.djrapitops.plan.api.API;

if (getPluginManager().isPluginEnabled("Plan")) {
    try {
        // Throws IllegalStateException if onEnable() method for Plan has not yet been called.    
        // Throws NoClassDefError if Plan is not installed
        API planAPI = Plan.getPlanAPI(); 
    } catch (Throwable e) {
        // Do something (Plan is not installed)
    }
}
```

# Plugins Tab

- [PluginData](/Plan/src/main/java/com/djrapitops/plan/data/additional/PluginData.java)
- [PluginData Javadoc](https://rsl1122.github.io/Plan-PlayerAnalytics/main/java/com/djrapitops/plan/data/additional/PluginData.html)
- [AnalysisType Enum](/Plan/src/main/java/com/djrapitops/plan/data/additional/AnalysisType.java)
- [AnalysisType Javadoc](https://rsl1122.github.io/Plan-PlayerAnalytics/main/java/com/djrapitops/plan/data/additional/AnalysisType.html)
- [Example classes](/Plan/src/main/java/com/djrapitops/plan/data/additional)

## Adding plugin's data to the 'plugins'-tab on Analysis and/or Inspect pages

Plan has a flexible data addition system since 3.1.0. With it you can add Averages, Totals, Percentages, Tables & Other Html elements to the Plugins tab on the Analysis and/or Inspect pages.

To add data **a class that extends PluginData class is needed.**  
One PluginData object should only contain data for one user, but tables & other elements are an exception.  

### Registering the data point

```
PluginData yourPluginDataObject = new YourClassThatExtendsPluginData();
Plan.getPlanAPI().addPluginDataSource(yourPluginDataObject);
```

**If you register multiple data sources, they will appear in the order they were registered.**

## Building the PluginData object

### Super Constructors
Constructor determines if the datapoint can be analyzed or not & how it should be analyzed.  
The Inspect page visibility can be changed with another method call.

Constructor | Parameters | Description
-- | -- | --
super(pluginName, placeholder) | Name of the plugin & the placeholder, eg: "stepsTaken" | This datapoint will be only shown on the Inspect page
super(pluginName, placeholder, AnalysisType.INT_TOTAL) | Type is determined by return value of getValue(UUID)-method | This datapoint will be shown on the Analysis page with total for all Players
super(pluginName, plaheholder, AnalysisType.INT_AVG, AnalysisType.INT_TOTAL) | The constructor takes as many AnalysisTypes as needed. | This datapoint will be shown on the Analysis page with the total and average for all Players

### Method calls in the contructor
There are multiple methods that change the appearance of the PluginData object on the webpage:

Method | Description
-- | --
super.analysisOnly(boolean) | Determine whether or not the datapoint should be only shown on the Analysis page, Set to false to show it on the Inspect page as well.
super.setPrefix(String) | For example: super.setPrefix("Steps taken: "), determines the prefix
super.setSuffix(String) | For example: super.setSuffix(" steps"), determines the suffix.
super.setIcon(String) | Set the font awesome icon name: [Font Awesome Icons](http://fontawesome.io/icons/)

To not show the datapoint on Analysis page, do not give the constructor any AnalysisType variables.

### getHtmlReplaceValue(String modifier, UUID uuid)-method

This method is used by Inspect page & by Analysis Page when AnalysisType.HTML is specified.  
**It should use the parseContainer(String modifier, String value) method when returning a value.**  

The Prefix, Suffix & Icon are added to the value automatically when using the parseContainer(String modifier, String value)-method.  
Example:
```
@Override
public String getHtmlReplaceValue(String modifier, UUID uuid) {
	return parseContainer(modifier, stepCounter.getSteps(uuid)+"");
}
```

UUID is the UUID of the player whose Inspect page is displayed OR Random UUID on Analysispage when AnalysisType.HTML is specified

### getValue(UUID uuid)-method

This method is used by Analysis when calculating Totals & Averages for each PluginData object.
The return type is Serializable, so Integer, Double, Long, Boolean & String can be returned.
What type you return determines what AnalýsisType you should use in the super constructor.

**-1** should be returned if the player has no value / Your plugin has no data on the player  
If -1 is returned, the value is ignored when calculating Averages & Totals.

**Wrong AnalysisType will cause an exception during calculation** - It is caught per AnalysisType, so test your plugin with /plan analyze.

# Examples:
- Basic Example
- Analysis Example
- Table Example

## Basic Example

This class will show "Steps Taken" on the Inspect page.

```
public class StepCounterSteps extends PluginData {
	
	private StepCounterPlugin stepCounter; // This is the example plugin where the data is taken from.
	
	// Constructor with the plugin as parameter.
	public StepCounterSteps(StepCounterPlugin stepCounter) {
		
		// A call to super constructor: PluginName, PlaceholderName
		super("StepCounter", "stepsTaken");
		
		super.setPrefix("Steps taken: ")
		super.setSuffix(" steps");
		super.setIcon("wheelchair");
		
		this.stepCounter = stepCounter; // Setting the plugin
		
		// Registering to the API, this can be done outside the class as well.
		API api = Plan.getPlanAPI();
		if (api.isEnabled()) {
			api.addPluginDataSource(this);
		}
	}
	
	// Required method.
	// This method is used with AnalysisType.HTML and for Inspect page's values.
	// All return values should use parseContainer(String modifier, String value)-method, more on that down below.
	@Override
   	public String getHtmlReplaceValue(String modifier, UUID uuid) {
		return parseContainer(modifier, stepCounter.getSteps(uuid)+"");
	}
	
	// Required method.
	// This method is used to calculate values for the Analysis page.
	@Override
    	public Serializable getValue(UUID uuid) {
		return stepCounter.getSteps(uuid);
	}
}
```

## Analysis Example

Adding data to the Analysis page is straightforward as well. Please note that one class can add a data to both Analysis & Inspect pages.

To add data to the Analysis page, analysisTypes need to be set.
This is done in the super constructor.

Refer to AnalysisType for what types you should add. The type depends on the return of the getValue(UUID uuid)-method you have written.

- [AnalysisType](/Plan/src/main/java/com/djrapitops/plan/data/additional/AnalysisType.java)
- [AnalysisType Javadoc](https://rsl1122.github.io/Plan-PlayerAnalytics/main/java/com/djrapitops/plan/data/additional/AnalysisType.html)

AnalysisType.HTML is for all other elements you might want to add - parseContainer method will be used instead of the getValue method.

**If you want this same datapoint to show data on the inspect page call super.analysisOnly(false);**

Good example is the [FactionsPower](/Plan/src/main/java/com/djrapitops/plan/data/additional/factions/FactionsPower.java)

## Table Example

A good example is the [AdvancedAchievementsTable](/Plan/src/main/java/com/djrapitops/plan/data/additional/advancedachievements/AdvanceAchievementsTable.java).
You can use the [Html Enum](/Plan/src/main/java/com/djrapitops/plan/ui/Html.java) to quickly create table html.
The Html Enum has easy lines for 2, 3 & 4 line columns - More than that will most likely not fit in the box.
the parse(String... p)-method takes as many strings as parameters as needed to replace all the REPLACE# placeholders on the line.

Let's deconstruct the constructor.
```
public AdvancedAchievementsTable(AdvancedAchievementsAPI aaAPI) {
	super("AdvancedAchievements", "achievementstable", AnalysisType.HTML);
	this.aaAPI = aaAPI;
	String player = Html.FONT_AWESOME_ICON.parse("user") + " Player";
	String achievements = Html.FONT_AWESOME_ICON.parse("check-circle-o") + " Achievements";
	// analysisOnly true by default.
	super.setPrefix(Html.TABLE_START_2.parse(player, achievements));
	super.setSuffix(Html.TABLE_END.parse());
}
```

- analysisTypes is set as AnalysisType.HTML.
- prefix is set as a sortable table's start with "<icon> Player" and "<icon> Achievements" columns. 
- suffix is set as the end tags for the whole table.

Now let's see how the table gets the values:
```
@Override
public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
	StringBuilder html = new StringBuilder();
	List<OfflinePlayer> offlinePlayers = Arrays.stream(getOfflinePlayers()).filter(p -> p.hasPlayedBefore()).collect(Collectors.toList());
	if (offlinePlayers.isEmpty()) {
		html.append(Html.TABLELINE_2.parse("No Players.",""));
	} else {
		for (OfflinePlayer p : offlinePlayers) {
			String inspectUrl = HtmlUtils.getInspectUrl(p.getName());
			String achievements = aaAPI.getPlayerTotalAchievements(p.getUniqueId()) + "";
			html.append(Html.TABLELINE_2.parse(Html.LINK.parse(inspectUrl, p.getName()), achievements));
		}
	}
	return parseContainer("", html.toString());
}
```

It simply gets the Players that have played on the server. If the list is empty, a row with "No Players" is added to the value (html, StringBuilder). 
Otherwise it get's the Url for the Inspect page of each player, and the amount of achievements a player has.
The link is parsed inside a html a-tag (with the text of the players name).
Then that is parsed into a table line with the a-tag & achievements.

The parseContainer uses an empty string as modifierPrefix, because we don't want any extra text added in-front of the table. 

Inspect doesn't use getValue(UUID uuid) &
AnalysisType.HTML disregards it's return values, so that simply returns an empty string.
```
@Override
public Serializable getValue(UUID uuid) {
	return "";
}
```
