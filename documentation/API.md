![Player Analytics](https://puu.sh/t8vin.png)
# API

- [API class](/Plan/src/main/java/com/djrapitops/plan/api/API.java)
- API Javadocs (Coming soon)

Accessing the API Methods:
```
API planAPI = Plan.getPlanAPI(); // Throws IllegalStateException if onEnable() method for Plan has not yet been called.
```

## Adding plugin's data to the 'plugins'-tab on Analysis and/or Inspect pages

Plan has a flexible data addition system since 3.1.0. With it you can add Averages, Totals, Percentages, Tables & Other Html elements to the Plugins tab on the Analysis and/or Inspect pages.

## Basics

- [PluginData](/Plan/src/main/java/com/djrapitops/plan/data/additional/PluginData.java)
- [AnalysisType Enum](/Plan/src/main/java/com/djrapitops/plan/data/additional/AnalysisType.java)
- [Example classes](/Plan/src/main/java/com/djrapitops/plan/data/additional)

To add data a class that extends PluginData class is needed. One PluginData object should only contain data for one user, but tables & other elements are an exception.  

Examples:
- Basic Example
- Inspect Example
- Analysis Example
- Table Example

## Basic Example

```
public class StepCounterSteps extends PluginData {
	
	private StepCounterPlugin stepCounter; // This is the example plugin where the data is taken from.
	
	// Constructor with the plugin as parameter.
	public StepCounterSteps(StepCounterPlugin stepCounter) {
		
		// A call to super constructor: PluginName, PlaceholderName
		super("StepCounter", "stepsTaken");
		
		this.stepCounter = stepCounter; // Setting the plugin
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

**At the moment registering this data source will have Only show up on Inspect page.** It is disregarded on analysis page. Two variables inside PluginData determine what should be done with the datapoint:

- List<AnalysisType> analysisTypes
- boolean analysisOnly

- analysisOnly is 'true' by default. - Inspect page will ignore the data.  
- analysisOnly is set to 'false' automatically when using the super constructor without AnalysisType parameters.
- analysisTypes is empty. - Thus Analysis page will ignore the data.  

## Registering the data point

This might be the easiest step.
get the API and call a single method - all done!

```
Plan.getPlanAPI().addPluginDataSource(new StepCounterSteps(stepCounter));
```

**If you register multiple data sources, they will appear in the order they were registered.**

## Inspect Example

We can add the data to the Inspect page with `super.setAnalysisOnly(false);`-call inside the constructor:
```
public StepCounterSteps(StepCounterPlugin stepCounter) {
	super("StepCounter", "stepsTaken");
	
	super.setAnalysisOnly(false); //
	
	this.stepCounter = stepCounter;
}
```

Now on the inspect page, there will be a "StepCounter" plugin box. Inside the box, however **is only a single number**
This is because no prefix has been set.

Let's do that:
```
public StepCounterSteps(StepCounterPlugin stepCounter) {
	super("StepCounter", "stepsTaken");
	
	super.setPrefix("Steps taken: ")
	//super.setSuffix(" steps"); // You can also define the suffix
	
	super.setAnalysisOnly(false);
	this.stepCounter = stepCounter;
}
```

Why should parseContainer(String modifier, String value)-method be used with getHtmlReplaceValue-method?

- Automatically add `<div class="plugin-data"></div>` wrap around the text.
- Automatically add the prefix, suffix & icon to the value.

Wait, icons?
[Font Awesome Icons](http://fontawesome.io/icons/) can be used. They are added before the prefix when parseContainer method is called.
To set the icon call `super.setIcon(iconName)` in the constructor. Icon-names are visible on the linked page.

```
super.setIcon("wheelchair");
```

## Analysis Example

Adding data to the Analysis page is straightforward as well. Please note that one class can add a data to both Analysis & Inspect pages.

To add data to the Analysis page, analysisTypes need to be set.
This is done in the super constructor. There are three ways to set them:
```
List:
List<AnalysisType> types = new ArrayList<>();
types.add(AnalysisType.LONG_AVG);
types.add(AnalysisType.LONG_TOTAL);
super("StepCounter", "stepsTaken", types);
```
```
Array:
AnalysisType[] types = new AnalysisType{AnalysisType.LONG_AVG, AnalysisType.LONG_TOTAL};
super("StepCounter", "stepsTaken", types);
```
```
Values (1):
super("StepCounter", "stepsTaken", AnalysisType.LONG_AVG);

Values (many):
super("StepCounter", "stepsTaken", AnalysisType.LONG_AVG, AnalysisType.LONG_TOTAL);
```

Refer to AnalysisType for what types you should add. The type depends on the return of the getValue(UUID uuid)-method you have written.

- [AnalysisType](/Plan/src/main/java/com/djrapitops/plan/data/additional/AnalysisType.java)
- AnalysisType Javadoc (Coming soon)

AnalysisType.HTML is for all other elements you might want to add.

## Table Example

A good example is the [AdvancedAchievementsTable](/Plan/src/main/java/com/djrapitops/plan/data/additional/advancedachievements/AdvanceAchievementsTable.java).
You can use the [Html Enum] to quickly create table html.
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
