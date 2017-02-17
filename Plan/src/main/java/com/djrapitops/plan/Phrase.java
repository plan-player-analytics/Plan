package main.java.com.djrapitops.plan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.bukkit.ChatColor;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public enum Phrase {
    REPLACE0("REPLACE0"),
    PREFIX("[Plan] "),
    ENABLED("Player Analytics Enabled."),
    DISABLED("Player Analytics Disabled."),
    RELOAD_COMPLETE(ChatColor.GREEN + "" + PREFIX + "Reload complete."),
    SAVE_CACHE("Saving cached data.."),
    ADD_TO_CACHE("Added REPLACE0 to Cache."),
    CONFIG_HEADER("Plan Config | More info at https://www.spigotmc.org/wiki/plan-configuration/"),
    DB_INIT("Database init.."),
    WEBSERVER_INIT("Initializing Webserver.."),
    WEBSERVER_CLOSE("Shutting down Webserver.."),
    WEBSERVER_RUNNING("Webserver running on PORT " + REPLACE0),
    DB_CONNECTION_FAIL("REPLACE0-Database Connection failed: REPLACE1"),
    DB_ESTABLISHED(REPLACE0 + "-database connection established."),
    DATABASE_TYPE_DOES_NOT_EXIST("That database type doesn't exist."),
    DATABASE_FAILURE_DISABLE("Database initialization has failed, disabling Plan."),
    NOTIFY_EMPTY_IP(ChatColor.YELLOW + "" + PREFIX + "IP in server.properties is empty & AlternativeServerIP is not used, incorrect links will be given!"),
    //
    VERSION_NEW_AVAILABLE("New Version (" + REPLACE0 + ") is availible at https://www.spigotmc.org/resources/plan-player-analytics.32536/"),
    VERSION_LATEST("You're running the latest version"),
    VERSION_CHECK_ERROR("Failed to compare versions."),
    VERSION_FAIL("Failed to get newest version number."),
    //
    USERNAME_NOT_VALID(ChatColor.RED + "" + PREFIX + "This Player doesn't exist."),
    USERNAME_NOT_SEEN(ChatColor.RED + "" + PREFIX + "This Player has not played on this server."),
    USERNAME_NOT_KNOWN(ChatColor.RED + "" + PREFIX + "Player not found from the database."),
    //
    COLOR_MAIN(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Main").charAt(1))),
    COLOR_SEC(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Secondary").charAt(1))),
    COLOR_TER(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Highlight").charAt(1))),
    HCOLOR_ACT_ONL(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityGraph.OnlinePlayers")),
    HCOLOR_ACT_NEW(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityGraph.NewPlayers")),
    HCOLOR_GMP_0(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Survival")),
    HCOLOR_GMP_1(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Creative")),
    HCOLOR_GMP_2(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Adventure")),
    HCOLOR_GMP_3(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Spectator")),
    HCOLOR_ACTP_ACT(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Active")),
    HCOLOR_ACTP_BAN(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Banned")),
    HCOLOR_ACTP_INA(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Inactive")),
    HCOLOR_ACTP_JON(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.JoinedOnce")),
    //
    ARROWS_RIGHT("»"),
    BALL("•"),
    GRABBING_DATA_MESSAGE(COLOR_TER + "" + ARROWS_RIGHT + COLOR_MAIN + " Fetching data to cache.."),
    //
    DEM_UNKNOWN("Not Known"),
    NOT_IN_TOWN("Not in a town"),
    NOT_IN_FAC("Not in a faction"),
    //
    ANALYSIS("Analysis | "),
    COMMAND_TIMEOUT(ChatColor.RED + "" + PREFIX + "REPLACE0 Command timed out! Error most likely on console."),
    ANALYSIS_START(ANALYSIS + "Beginning analysis of user data.."),
    ANALYSIS_BOOT_NOTIFY(ANALYSIS + "Boot analysis in 30 seconds.."),
    ANALYSIS_BOOT(ANALYSIS + "Starting Boot Analysis.."),
    ANALYSIS_FETCH_PLAYERS(ANALYSIS + "Checking for available players.."),
    ANALYSIS_FETCH_DATA(ANALYSIS + "Fetching Data.."),
    ANALYSIS_FAIL_NO_PLAYERS(ANALYSIS + "Analysis failed, no known players."),
    ANALYSIS_FAIL_NO_DATA(ANALYSIS + "Analysis failed, no data in the database."),
    ANALYSIS_BEGIN_ANALYSIS(ANALYSIS + "Data Fetched, beginning Analysis of data.."),
    ANALYSIS_COMPLETE(ANALYSIS + "Analysis Complete."),
    DATA_CORRUPTION_WARN("Some data might be corrupted: " + REPLACE0),
    //
    ERROR_PLANLITE("PlanLite not found, if you're have plugins using PlanAPI v1.6.0 download PlanLite."),
    ERROR_CONSOLE_PLAYER("This point of code should not be accessable on console. Inform author: " + REPLACE0 + " Console: REPLACE1"),
    ERROR_NO_DATA_VIEW(ChatColor.YELLOW + "Webserver disabled but Alternative IP/PlanLite not used, no way to view data!"),
    ERROR_WEBSERVER_OFF_ANALYSIS(ChatColor.YELLOW + "" + PREFIX + "This command can be only used if the webserver is running on this server."),
    ERROR_WEBSERVER_OFF_INSPECT(ChatColor.YELLOW + "" + PREFIX + "This command can be only used if webserver/planlite is enabled on this server."),
    ERROR_LOGGED("Ran into an error. It has been logged to the Errors.txt"),
    //
    CMD_FOOTER(COLOR_TER.color() + "" + ARROWS_RIGHT),
    MANAGE_ERROR_INCORRECT_PLUGIN(ChatColor.RED + "" + PREFIX + "Plugin not supported: "),
    MANAGE_PROCESS_START(ARROWS_RIGHT + "" + COLOR_SEC.color() + " Processing data.."),
    MANAGE_ERROR_PLUGIN_NOT_ENABLED(ChatColor.RED + "" + PREFIX + "Plugin is not enabled: "),
    MANAGE_ERROR_INCORRECT_DB(ChatColor.RED + "" + PREFIX + "Incorrect database! (sqlite/mysql accepted): "),
    MANAGE_ERROR_SAME_DB(ChatColor.RED + "" + PREFIX + "Can't move to the same database!"),
    MANAGE_DATABASE_FAILURE(ChatColor.RED + "" + PREFIX + "One of the databases was not initialized properly."),
    MANAGE_DB_CONFIG_REMINDER(ChatColor.YELLOW + "" + PREFIX + "Remember to swap to the new database and reload plugin"),
    MANAGE_ERROR_NO_PLAYERS(ChatColor.RED + "" + PREFIX + "Database has no player data!"),
    MANAGE_ERROR_BACKUP_FILE_NOT_FOUND(ChatColor.RED + "" + PREFIX + "Backup file doesn't exist!"),
    MANAGE_MOVE_SUCCESS(ChatColor.GREEN + "" + PREFIX + "All data moved successfully!"),
    MANAGE_COPY_SUCCESS(ChatColor.GREEN + "" + PREFIX + "All data copied successfully!"),
    MANAGE_PROCESS_FAIL(ChatColor.RED + "" + PREFIX + "Something went wrong while processing the data!"),
    MANAGE_CLEAR_SUCCESS(ChatColor.GREEN + "" + PREFIX + "All data cleared successfully!"),
    MANAGE_REMOVE_SUCCESS(CMD_FOOTER + " " + COLOR_MAIN.color() + "Data of " + COLOR_TER.color() + "REPLACE0" + COLOR_MAIN.color() + " was removed from Database " + COLOR_TER.color() + "REPLACE1" + COLOR_MAIN.color() + "."),
    MANAGE_IMPORTING(CMD_FOOTER + " " + COLOR_MAIN.color() + " Importing Data.."),
    MANAGE_SUCCESS(CMD_FOOTER + " " + COLOR_MAIN.color() + " Success!"),
    //
    CMD_BALL(COLOR_SEC.color() + " " + Phrase.BALL.toString() + COLOR_MAIN.color()),
    CMD_ANALYZE_HEADER(CMD_FOOTER + "" + COLOR_MAIN.color() + " Player Analytics - Analysis results"),
    CMD_INSPECT_HEADER(CMD_FOOTER + "" + COLOR_MAIN.color() + " Player Analytics - Inspect results: "),
    CMD_SEARCH_HEADER(CMD_FOOTER + "" + COLOR_MAIN.color() + " Player Analytics - Search results for: "),
    CMD_HELP_HEADER(CMD_FOOTER + "" + COLOR_MAIN.color() + " Player Analytics - Help"),
    CMD_MANAGE_STATUS_HEADER(CMD_FOOTER + "" + COLOR_MAIN.color() + " Player Analytics - Database status"),
    CMD_MANAGE_STATUS_ACTIVE_DB(CMD_BALL + "" + COLOR_MAIN.color() + " Active Database: " + COLOR_SEC.color() + "REPLACE0"),
    CMD_CLICK_ME("Click Me"),
    CMD_LINK(COLOR_SEC.color() + " " + BALL + COLOR_MAIN.color() + " Link: " + COLOR_TER.color()),
    CMD_PASS_PLANLITE(ChatColor.YELLOW + "" + PREFIX + "Passing to PlanLite.."),
    CMD_RESULTS_AVAILABLE(COLOR_SEC.color() + "   Results will be available for " + COLOR_TER.color() + REPLACE0 + COLOR_SEC.color() + " minutes."),
    CMD_NO_RESULTS(CMD_BALL + " No results for " + COLOR_SEC.color() + REPLACE0 + COLOR_MAIN.color() + "."),
    CMD_MATCH(COLOR_SEC.color() + " Matching player: " + COLOR_TER.color()),
    //
    CMD_USG_ANALYZE("View the Server Analysis"),
    CMD_USG_HELP("Show command list."),
    CMD_USG_INFO("View Version of Plan"),
    CMD_USG_INSPECT("Inspect Player's Data"),
    CMD_USG_MANAGE("Database managment command"),
    CMD_USG_MANAGE_BACKUP("Backup a database to .db file"),
    CMD_USG_MANAGE_RESTORE("Restore a database from a backup file"),
    CMD_USG_MANAGE_MOVE("Copy data from one database to another & overwrite values"),
    CMD_USG_MANAGE_COMBINE("Copy data from one database to another & combine values"),
    CMD_USG_MANAGE_IMPORT("Import Data from supported plugins to Active Database."),
    CMD_USG_MANAGE_CLEAR("Clear data from one database"),
    CMD_USG_MANAGE_REMOVE("Remove players's data from the Active Database."),
    CMD_USG_MANAGE_STATUS("Check the status of the Active Database."),
    CMD_USG_RELOAD("Reload plugin config & save cached data"),
    CMD_USG_SEARCH("Search for player"),
    ARG_SEARCH("<part of playername>"),
    ARG_PLAYER("<player>"),
    ARG_RESTORE("<Filename.db> <dbTo> [-a]"),
    ARG_IMPORT("<plugin> [-a]"),
    ARG_MOVE("<fromDB> <toDB> [-a]"),
    //
    USE_BACKUP("Use /plan manage backup <DB>"),
    USE_RESTORE("Use /plan manage restore <Filename.db> <dbTo> [-a]"),
    USE_MANAGE("Use /plan manage for help"),
    USE_PLAN("Use /plan for help"),
    USE_MOVE("Use /plan manage move <fromDB> <toDB> [-a]"),
    USE_COMBINE("Use /plan manage combine <fromDB> <toDB> [-a]"),
    USE_IMPORT("Use /plan manage import <plugin> [-a]"),
    //
    WARN_REWRITE("Data in REPLACE0-database will be rewritten!"),
    WARN_OVERWRITE("Data in REPLACE0-database will be overwritten!"),
    WARN_OVERWRITE_SOME("Some data in REPLACE0-database will be overwritten!"),
    WARN_REMOVE("Data in REPLACE0-database will be removed!"),
    //
    COMMAND_SENDER_NOT_PLAYER(ChatColor.RED + "" + PREFIX + "This command can be only used as a player."),
    COMMAND_REQUIRES_ARGUMENTS(ChatColor.RED + "" + PREFIX + "Command requires arguments. REPLACE0"),
    COMMAND_ADD_CONFIRMATION_ARGUMENT(ChatColor.RED + "" + PREFIX + "Add -a to confirm execution! REPLACE0"),
    COMMAND_REQUIRES_ARGUMENTS_ONE(ChatColor.RED + "" + PREFIX + "Command requires one argument."),
    COMMAND_NO_PERMISSION(ChatColor.RED + "" + PREFIX + "You do not have the required permmission.");

    private String text;
    private ChatColor color;

    private Phrase(String text) {
        this.text = text;
        this.color = null;
    }

    private Phrase(ChatColor color) {
        this.color = color;
        this.text = "";
    }

    @Override
    public String toString() {
        return text;
    }

    public String parse() {
        return this.toString();
    }

    public String parse(String... p) {
        String returnValue = this.toString();
        for (int i = 0; i < p.length; i++) {
            returnValue = returnValue.replaceAll("REPLACE" + i, p[i]);
        }
        return returnValue;
    }

    /**
     * @return Color of the COLOR_ENUM
     */
    public ChatColor color() {
        return color;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setColor(String colorCode) {
        this.color = ChatColor.getByChar(colorCode);
    }

    static void loadLocale(File localeFile) {
        try {
            Scanner localeScanner = new Scanner(localeFile, "UTF-8");
            List<String> localeRows = new ArrayList<>();
            while (localeScanner.hasNextLine()) {
                String line = localeScanner.nextLine();
                if (!line.isEmpty()) {
                    if (line.equals("<<<<<<HTML>>>>>>")) {
                        break;
                    }
                    localeRows.add(line);
                }
            }
            for (String localeRow : localeRows) {
                try {
                    String[] split = localeRow.split(" <> ");
                    Phrase.valueOf(split[0]).setText(split[1]);
                } catch (IllegalArgumentException e) {
                    getPlugin(Plan.class).logError("There is a miswritten line in locale on line " + localeRows.indexOf(localeRow));
                }
            }
        } catch (IOException e) {

        }
    }
}
