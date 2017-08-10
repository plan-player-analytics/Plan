![Player Analytics](https://puu.sh/t8vin.png)
# Localization

This article will tell you how you can change the messages seen in the plugin.
(Introduced in version 2.5.0 & Revamped in 3.6.2)

By Default Plan uses internal Locale found inside Locale.java (Config setting: default)

- [Locale](https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/java/com/djrapitops/plan/locale/Locale.java)

## Config setting
In the config, there is a setting for Locale.
Setting this to, for example FI, (Finnish) will attempt to use locale from inside the plugin jar.
Inside the plugin there are multiple locale files.

You can generate a new locale (default values) by setting the config setting **WriteNewLocaleFileOnStart** to true.
This will generate a locale.txt to the plugin's folder & that will be used when present.

To change the messages, only change the parts on the right, left ones are identifiers.

Some messages in the html pages are not loaded from the text file, and need to be changed seperately.
This can be done by copying the html files from the .jar to the plugin folder and editing them.

- [Available Locales](https://github.com/Rsl1122/Plan-PlayerAnalytics/tree/master/Plan/localization)

## End
If you want to help me out with localizing the plugin to your language, you can translate the locale file to your language and send the contents to me in one way:
- Do a pull request 
- [Open an issue](https://github.com/Rsl1122/Plan-PlayerAnalytics/issues) 
- [Send me private message on spigot](https://www.spigotmc.org/members/rsl1122.122894/)
