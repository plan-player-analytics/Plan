package main.java.com.djrapitops.plan.locale;

import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.DefaultMessages;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.comparators.LocaleEntryComparator;
import main.java.com.djrapitops.plan.utilities.comparators.StringLengthComparator;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.bukkit.ChatColor;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class responsible for message localization.
 * <p>
 * Successor to Phrase Enum system.
 *
 * @author Rsl1122
 * @since 3.6.2
 */
public class Locale implements Closeable {

    private final Plan plugin;
    private final Map<Msg, Message> messages;

    public Locale(Plan plugin) {
        LocaleHolder.setLocale(this);
        this.plugin = plugin;
        messages = new HashMap<>();
    }

    public void loadLocale() {
        String locale = Settings.LOCALE.toString().toUpperCase();
        Benchmark.start("Initializing locale");
        loadDefault();
        try {
            if (Settings.WRITE_NEW_LOCALE.isTrue()) {
                writeNewDefaultLocale();
            }
            File localeFile = new File(plugin.getDataFolder(), "locale.txt");
            if (localeFile.exists()) {
                loadFromFile(localeFile);
            } else if (locale.equals("DEFAULT")) {
                Log.info("Using Locale: Default (EN)");
            } else {
                loadFromResource("locale_" + locale + ".txt");
            }
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            Benchmark.stop("Enable", "Initializing locale");
        }
    }

    private void writeNewDefaultLocale() throws IOException {
        final int length = messages.keySet().stream()
                .map(Msg::getIdentifier)
                .sorted(new StringLengthComparator())
                .findFirst()
                .get().length() + 2;
        List<String> lines = messages.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(entry -> getSpacedIdentifier(entry.getKey().getIdentifier(), length) + "|| " + entry.getValue().toString())
                .collect(Collectors.toList());
        Files.write(new File(plugin.getDataFolder(), "locale.txt").toPath(), lines, StandardCharsets.UTF_8);
        plugin.getConfig().set(Settings.WRITE_NEW_LOCALE.getPath(), false);
    }

    private String getSpacedIdentifier(String identifier, int length) {
        StringBuilder b = new StringBuilder(identifier);
        while (b.length() < length) {
            b.append(" ");
        }
        return b.toString();
    }

    private void loadDefault() {
        String analysis = "Analysis | ";
        String prefix = "[Plan] ";
        String green = ChatColor.GREEN.toString();
        String yellow = ChatColor.YELLOW.toString();
        String red = ChatColor.RED.toString();
        String arrowsRight = DefaultMessages.ARROWS_RIGHT.parse();
        ColorScheme cs = plugin.getColorScheme();
        String mCol = cs.getMainColor();
        String sCol = cs.getSecondaryColor();
        String tCol = cs.getTertiaryColor();
        String cmdFooter = tCol + arrowsRight;

        add(Msg.ENABLED, "Player Analytics Enabled.");
        add(Msg.ENABLE_DB_INIT, "Database init..");
        add(Msg.ENABLE_DB_INFO, "REPLACE0-database connection established.");
        add(Msg.ENABLE_WEBSERVER, "Initializing Webserver..");
        add(Msg.ENABLE_WEBSERVER_INFO, "Webserver running on PORT REPLACE0");
        add(Msg.ENABLE_BOOT_ANALYSIS_INFO, analysis + "Boot analysis in 30 seconds..");
        add(Msg.ENABLE_BOOT_ANALYSIS_RUN_INFO, analysis + "Starting Boot Analysis..");

        add(Msg.ENABLE_NOTIFY_EMPTY_IP, yellow + "IP in server.properties is empty & AlternativeServerIP is not used, incorrect links will be given!");
        add(Msg.ENABLE_NOTIFY_NO_DATA_VIEW, yellow + "Webserver disabled but Alternative IP/TextUI not used, no way to view data!");
        add(Msg.ENABLE_NOTIFY_DISABLED_CHATLISTENER, yellow + "Chat listener disabled, nickname info inaccurate.");
        add(Msg.ENABLE_NOTIFY_DISABLED_COMMANDLISTENER, yellow + "Command usage listener disabled.");
        add(Msg.ENABLE_NOTIFY_DISABLED_DEATHLISTENER, yellow + "Death listener disabled, player & mob kills not recorded.");

        add(Msg.ENABLE_FAIL_WRONG_DB, "That database type doesn't exist.");
        add(Msg.ENABLE_FAIL_DB, "REPLACE0-Database Connection failed: REPLACE1");
        add(Msg.ENABLE_DB_FAIL_DISABLE_INFO, "Database initialization has failed, disabling Plan.");

        add(Msg.RUN_WARN_QUEUE_SIZE, "Queue size is too small! (REPLACE0), change the setting to a higher number! (Currently REPLACE1)");

        add(Msg.DISABLED, "Player Analytics Disabled.");
        add(Msg.DISABLE_CACHE_SAVE, "Saving cached data..");
        add(Msg.DISABLE_WEBSERVER, "Shutting down Webserver..");

        add(Msg.ANALYSIS_START, analysis + "Beginning analysis of user data..");
        add(Msg.ANALYSIS_FETCH_UUID, analysis + "Checking for available players..");
        add(Msg.ANALYSIS_FETCH, analysis + "Fetching Data..");
        add(Msg.ANALYSIS_PHASE_START, analysis + "Data Fetched (REPLACE0 users, took REPLACE1ms), beginning Analysis of data..");
        add(Msg.ANALYSIS_3RD_PARTY, analysis + "Analyzing additional data sources (3rd party)");
        add(Msg.ANALYSIS_FINISHED, analysis + "Analysis Complete. (took REPLACE0ms) REPLACE1");
        add(Msg.ANALYSIS_FAIL_NO_PLAYERS, analysis + "Analysis failed, no known players.");
        add(Msg.ANALYSIS_FAIL_NO_DATA, analysis + "Analysis failed, no data in the database.");
        add(Msg.ANALYSIS_FAIL_FETCH_EXCEPTION, analysis + "Failed to fetch data for Analysis, Exception occurred.");

        add(Msg.MANAGE_INFO_CONFIG_REMINDER, yellow + prefix + "Remember to swap to the new database and reload plugin");
        add(Msg.MANAGE_INFO_START, arrowsRight + sCol + " Processing data..");
        add(Msg.MANAGE_INFO_IMPORT, cmdFooter + " " + mCol + " Importing Data..");
        add(Msg.MANAGE_INFO_FAIL, red + prefix + "Something went wrong while processing the data!");
        add(Msg.MANAGE_INFO_SUCCESS, cmdFooter + " " + mCol + " Success!");
        add(Msg.MANAGE_INFO_COPY_SUCCESS, green + prefix + "All data copied successfully!");
        add(Msg.MANAGE_INFO_MOVE_SUCCESS, green + prefix + "All data moved successfully!");
        add(Msg.MANAGE_INFO_CLEAR_SUCCESS, green + prefix + "All data cleared successfully!");
        add(Msg.MANAGE_INFO_REMOVE_SUCCESS, cmdFooter + " " + mCol + "Data of " + tCol + "REPLACE0" + mCol + " was removed from Database " + tCol + "REPLACE1" + mCol + ".");

        add(Msg.MANAGE_FAIL_INCORRECT_PLUGIN, red + prefix + "Plugin not supported: ");
        add(Msg.MANAGE_FAIL_PLUGIN_NOT_ENABLED, red + prefix + "Plugin is not enabled: ");
        add(Msg.MANAGE_FAIL_SAME_DB, red + prefix + "Can't move to the same database!");
        add(Msg.MANAGE_FAIL_INCORRECT_DB, red + prefix + "Incorrect database! (sqlite/mysql accepted): ");
        add(Msg.MANAGE_FAIL_FAULTY_DB, red + prefix + "One of the databases was not initialized properly.");
        add(Msg.MANAGE_FAIL_NO_PLAYERS, red + prefix + "Database has no player data!");
        add(Msg.MANAGE_FAIL_FILE_NOT_FOUND, red + prefix + "Backup file doesn't exist!");

        add(Msg.MANAGE_FAIL_CONFIRM, red + prefix + "Add -a to confirm execution! REPLACE0");
        add(Msg.MANAGE_NOTIFY_REWRITE, "Data in REPLACE0-database will be rewritten!");
        add(Msg.MANAGE_NOTIFY_OVERWRITE, "Data in REPLACE0-database will be overwritten!");
        add(Msg.MANAGE_NOTIFY_PARTIAL_OVERWRITE, "Some data in REPLACE0-database will be overwritten!");
        add(Msg.MANAGE_NOTIFY_REMOVE, "Data in REPLACE0-database will be removed!");

        add(Msg.CMD_FAIL_REQ_ARGS, red + prefix + "Command requires arguments. REPLACE0");
        add(Msg.CMD_FAIL_REQ_ONE_ARG, red + prefix + "Command requires one argument.");
        add(Msg.CMD_FAIL_NO_PERMISSION, red + prefix + "You do not have the required permission.");
        add(Msg.CMD_FAIL_USERNAME_NOT_VALID, red + prefix + "This Player doesn't exist.");
        add(Msg.CMD_FAIL_USERNAME_NOT_SEEN, red + prefix + "This Player has not played on this server.");
        add(Msg.CMD_FAIL_USERNAME_NOT_KNOWN, red + prefix + "Player not found from the database.");
        add(Msg.CMD_FAIL_TIMEOUT, red + prefix + "REPLACE0 Command timed out! Check '/plan status' & console.");
        add(Msg.CMD_FAIL_NO_DATA_VIEW, yellow + "" + prefix + "No Way to view Data Available.");

        add(Msg.CMD_INFO_ANALYSIS_TEMP_DISABLE, yellow + "Analysis has been temporarily disabled due to expensive task, use /plan status for info.");
        add(Msg.CMD_INFO_RELOAD_COMPLETE, green + prefix + "Reload complete.");
        add(Msg.CMD_INFO_FETCH_DATA, tCol + arrowsRight + mCol + " Fetching data to cache..");
        add(Msg.CMD_INFO_CLICK_ME, "Click Me");
        add(Msg.CMD_INFO_LINK, sCol + " • " + mCol + "Link: " + tCol);
        add(Msg.CMD_INFO_RESULTS, sCol + " Matching players: " + tCol);
        add(Msg.CMD_INFO_NO_RESULTS, sCol + " • " + mCol + "No results for " + sCol + "REPLACE0" + mCol + ".");
        add(Msg.CMD_INFO_SEARCHING, cmdFooter + mCol + "Searching..");

        add(Msg.CMD_USG_ANALYZE, "View the Server Analysis");
        add(Msg.CMD_USG_QANALYZE, "View the Server Analysis as Text");
        add(Msg.CMD_USG_HELP, "Show command list.");
        add(Msg.CMD_USG_INFO, "Check the version of Plan");
        add(Msg.CMD_USG_INSPECT, "Inspect Player's Data");
        add(Msg.CMD_USG_QINSPECT, "Inspect Player's Data as Text");
        add(Msg.CMD_USG_LIST, "List to all cached players");
        add(Msg.CMD_USG_MANAGE, "Database management command");
        add(Msg.CMD_USG_MANAGE_BACKUP, "Backup a database to .db file");
        add(Msg.CMD_USG_MANAGE_CLEAN, "Clear old data from the database");
        add(Msg.CMD_USG_MANAGE_CLEAR, "Clear ALL data from the database");
        add(Msg.CMD_USG_MANAGE_DUMP, "Create a Hastebin log for Dev for easier Issue reporting.");
        add(Msg.CMD_USG_MANAGE_HOTSWAP, "Hotswap to another database & restart the plugin");
        add(Msg.CMD_USG_MANAGE_IMPORT, "Import Data from supported plugins to Active Database.");
        add(Msg.CMD_USG_MANAGE_MOVE, "Copy data from one database to another & overwrite values");
        add(Msg.CMD_USG_MANAGE_REMOVE, "Remove players's data from the Active Database.");
        add(Msg.CMD_USG_MANAGE_RESTORE, "Restore a database from a backup file");
        add(Msg.CMD_USG_RELOAD, "Restart the Plugin (Reloads config)");
        add(Msg.CMD_USG_SEARCH, "Search for player");
        add(Msg.CMD_USG_WEB, "Manage Webusers");
        add(Msg.CMD_USG_WEB_CHECK, "Check a webuser and their permission level.");
        add(Msg.CMD_USG_WEB_DELETE, "Delete a webuser");
        add(Msg.CMD_USG_WEB_LEVEL, "Info about permission levels.");
        add(Msg.CMD_USG_WEB_REGISTER, "Register a user for the webserver");

        add(Msg.CMD_HELP_ANALYZE, mCol + "Analysis Command"
                + "\\" + tCol + "  Used to Refresh analysis cache & Access the result page"
                + "\\" + sCol + "  /plan status can be used to check status of analysis while it is running."
                + "\\" + sCol + "  Aliases: analyze, analyse, analysis, a");
        add(Msg.CMD_HELP_QANALYZE, mCol + "Quick Analysis command"
                + "\\" + tCol + "  Used to get in game info about analysis."
                + "\\" + sCol + "  Has less info than full Analysis web page."
                + "\\" + sCol + "  Aliases: qanalyze, ganalyse, qanalysis, qa");
        add(Msg.CMD_HELP_PLAN, mCol + "/plan - Main Command"
                + "\\" + tCol + "  Used to access all SubCommands & help"
                + "\\" + sCol + "  /plan - List subcommands"
                + "\\" + sCol + "  /plan <subcommand> ? - in depth help");
        add(Msg.CMD_HELP_INSPECT, mCol + "Inspect command"
                + "\\" + tCol + "  Used to get a link to User's inspect page."
                + "\\" + sCol + "  Own inspect page can be accessed with /plan inspect"
                + "\\" + sCol + "  Alias: /plan <name>");
        add(Msg.CMD_HELP_QINSPECT, mCol + "Quick Inspect command"
                + "\\" + tCol + "  Used to get some inspect info in game."
                + "\\" + sCol + "  Has less info than full Inspect web page."
                + "\\" + sCol + "  Alias: /plan qi");
        add(Msg.CMD_HELP_LIST, mCol + "List command"
                + "\\" + tCol + "  Used to get a link to players page."
                + "\\" + sCol + "  Players page contains links to all cached inspect pages."
                + "\\" + sCol + "  Alias: /plan pl");
        add(Msg.CMD_HELP_MANAGE, mCol + "Manage command\\"
                + tCol + "  Used to Manage Database of the plugin."
                + "\\" + sCol + "  Alias: /plan m"
                + "\\" + sCol + "  /plan m - List subcommands"
                + "\\" + sCol + "  /plan m <subcommand> ? - in depth help");
        add(Msg.CMD_HELP_MANAGE_CLEAR, mCol + "Manage Clear command"
                + "\\" + tCol + "  Used to delete ALL data in the active database."
                + "\\" + sCol + "  Plugin should be reloaded after successful clear."
                + "\\" + sCol + "  Alias: /plan pl");
        add(Msg.CMD_HELP_MANAGE_DUMP, mCol + "Manage Dump command"
                + "\\" + tCol + "  Used to dump important data for bug reporting to hastebin.");
        add(Msg.CMD_HELP_MANAGE_HOTSWAP, mCol + "Manage Hotswap command"
                + "\\" + tCol + "  Used to change database in use on the fly."
                + "\\" + sCol + "  Does not change database if connection fails");
        add(Msg.CMD_HELP_MANAGE_IMPORT, mCol + "Manage Import command"
                + "\\" + tCol + "  Used to import data from other sources"
                + "\\" + sCol + "  Analysis will be disabled during import.");
        add(Msg.CMD_HELP_MANAGE_REMOVE, mCol + "Manage Remove command"
                + "\\" + tCol + "  Used to Remove user's data from the active database.");
        add(Msg.CMD_HELP_SEARCH, mCol + "Search command"
                + "\\" + tCol + "  Used to get a list of Player names that match the given argument."
                + "\\" + sCol + "  Example: /plan search 123 - Finds all users with 123 in their name.");
        add(Msg.CMD_HELP_WEB, mCol + "Web User Manage command"
                + "\\" + tCol + "  Used to manage web users of the plugin"
                + "\\" + sCol + "  Users have a permission level:"
                + "\\" + tCol + "   0 - Access to all pages"
                + "\\" + tCol + "   1 - Access to /players & all inspect pages"
                + "\\" + tCol + "   2 - Access to own inspect page"
                + "\\" + sCol + "  Alias: /plan web");
        add(Msg.CMD_HELP_WEB_REGISTER, mCol + "Web Register command"
                + "\\" + tCol + "  Used to register a new user for the webserver."
                + "\\" + sCol + "  Registering a user for another player requires " + Permissions.MANAGE_WEB.getPerm() + " permission."
                + "\\" + sCol + "  Passwords are hashed with PBKDF2 (64,000 iterations of SHA1) using a cryptographically-random salt.");

        add(Msg.CMD_HEADER_ANALYZE, cmdFooter + mCol + " Player Analytics - Analysis results");
        add(Msg.CMD_HEADER_INSPECT, cmdFooter + mCol + " Player Analytics - Inspect results");
        add(Msg.CMD_HEADER_INFO, cmdFooter + mCol + " Player Analytics - Info");
        add(Msg.CMD_HEADER_SEARCH, cmdFooter + mCol + " Player Analytics - Search results: ");

        add(Msg.CMD_CONSTANT_FOOTER, tCol + arrowsRight);
        add(Msg.CMD_CONSTANT_LIST_BALL, sCol + " " + "•" + mCol);

        add(Msg.HTML_NO_PLUGINS, "<p>No extra plugins registered.</p>");
        add(Msg.HTML_BANNED, "| " + Html.SPAN.parse(Html.COLOR_4.parse() + "Banned"));
        add(Msg.HTML_OP, ", Operator (Op)");
        add(Msg.HTML_ONLINE, "| " + Html.SPAN.parse(Html.COLOR_2.parse() + "Online"));
        add(Msg.HTML_OFFLINE, "| " + Html.SPAN.parse(Html.COLOR_4.parse() + "Offline"));
        add(Msg.HTML_ACTIVE, "Player is Active");
        add(Msg.HTML_INACTIVE, "Player is inactive");
        add(Msg.HTML_TABLE_NO_KILLS, "No Kills");
    }

    private void add(Msg msg, String message) {
        Verify.nullCheck(msg, message);
        messages.put(msg, new Message(message));
    }

    private void loadFromFile(File localeFile) throws IOException {
        loadFromContents(Files.lines(localeFile.toPath(), StandardCharsets.UTF_8).collect(Collectors.toList()), "Custom File");
    }

    private void loadFromResource(String fileName) {
        try {
            loadFromContents(FileUtil.lines(plugin, fileName), fileName);
        } catch (FileNotFoundException e) {
            Log.error("Could not find file inside the jar: " + fileName);
            Log.info("Using Locale: Default (EN)");
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            Log.info("Using Locale: Default (EN)");
        }
    }

    private void loadFromContents(List<String> locale, String name) {
        Log.info("Using Locale: " + name);
        Map<String, Msg> identifiers = Msg.getIdentifiers();
        locale.forEach(line -> {
            String[] split = line.split(" \\|\\| ");
            if (split.length == 2) {
                String identifier = split[0].trim();
                Msg msg = identifiers.get(identifier);
                if (msg != null) {
                    add(msg, split[1]);
                }
            }
        });
    }

    public Message getMessage(Msg msg) {
        return messages.getOrDefault(msg, new Message(""));
    }

    @Override
    public void close() {
        messages.clear();
        LocaleHolder.locale = null;
    }

    public static void unload() {
        Locale locale = LocaleHolder.getLocale();
        if (locale != null) {
            locale.close();
        }
    }

    public static Message get(Msg msg) {
        Locale locale = LocaleHolder.getLocale();
        if (locale == null) {
            throw new IllegalStateException("Locale has not been initialized.");
        }
        return locale.getMessage(msg);
    }

    private static class LocaleHolder {

        private static Locale locale;

        public static void setLocale(Locale locale) {
            LocaleHolder.locale = locale;
        }

        public static Locale getLocale() {
            return locale;
        }
    }
}
