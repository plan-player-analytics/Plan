![Player Analytics](https://puu.sh/t8vin.png)
# Localization

This article will tell you how you can change the messages seen in the plugin.
(Introduced in version 2.5.0)

By Default Plan uses internal Locale found inside Phrase.java & Html.java (Config setting: default)

- [Phrase](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/Phrase.java)
- [Html](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/ui/Html.java)


## Config setting
In the config, there is a setting for Locale.
Setting this to, for example EN, will attempt to fetch locale from Github to be used with the plugin. 
Unfortunately when this setting is used, the Color customization setting for commands is not used. This can be circumvented by copying the existing locale from Github to `/plugins/Plan/locale.txt` and changing the colors in the file.

If you want to use your own locale, just copy contents of this file on github to `/plugins/Plan/locale.txt`
Now you can customize all in game messages!

Some messages in the html pages are not loaded from the text file, and need to be changed seperately.
This can be done by copying the html files from the .jar to the plugin folder and editing them.

- [Available Locales](https://github.com/Rsl1122/Plan-PlayerAnalytics/tree/master/Plan/localization)

## Known caveats:
- The 'äåö'-letters etc do not work on the webpage, this will hopefully be fixed in a future update.

## End
If you want to help me out with localizing the plugin to your language, you can translate the locale file to your language and send the contents to me in one way:
- Do a pull request 
- [Open an issue](https://github.com/Rsl1122/Plan-PlayerAnalytics/issues) 
- [Send me private message on spigot](https://www.spigotmc.org/members/rsl1122.122894/)
