![Player Analytics](https://puu.sh/t8vin.png)
# Manage-command Guide

![Manage Help in 3.1.0](http://puu.sh/vQAaV/41ea052a5d.jpg)  
*/plan manage-command*

# Commands

## /plan manage move

This command can be used to move all data from one database to another. This is useful when you have had Plan installed for a while, but have not had MySQL available.  
The command moves all data from one database to another & overwrites all values in the destination database.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<fromDB>` | Yes | sqlite / mysql | The database the data will be moved from. Database will stay unaffected.
`<toDB>` | Yes | sqlite / mysql | The database the data will be moved to. Old data in the database will be removed.
[-a] | No | -a | Confirmation argument, Required for change to occur.

## /plan manage backup

This command can be used to backup all data from a database into a separate .db file.
The name of the new file depends on the database & date. For example mysql-backup-Feb-11.db or sqlite-backup-Mar-12.db
The database file will use SQLite save format.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<DB>` | Yes | sqlite / mysql | The database the data will be backed up from. Database will stay unaffected.

## /plan manage restore

This command can be used to restore data from a backup file.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<Filename.db>` | Yes | Anything | The filename of the backup database file.
`<toDB>` | Yes | sqlite / mysql | The database the data will be restored to. Old data in the database will be removed.
[-a] | No | -a | Confirmation argument, Required for change to occur.

## /plan manage hotswap

This command restarts the plugin and changes the config value of database.type to the given argument.
If connection to the given database fails the command is cancelled & plugin is not restarted.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<DB>` | Yes | sqlite / mysql | The database the config value should be changed to.

## /plan manage status

Used to check what database is in use (Active Database)

## /plan import

This command can be used to import data from other plugins into the Plan database.
A revamp of the system is coming in 3.2.0.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<plugin>` | Yes | A supported plugin's name | Plugin where data should be imported from
[-a] | No | -a | Confirmation argument, Required for change to occur.

### Supported plugins

Plugin | What is done
-- | ----
OnTime | Playtime is imported & Gamemode times table is reset for affected players (Gamemode time calculation is dependent on playtime.)

## /plan manage remove

Removes all data of a player from the database.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<player>` | Yes | Playername | Player whose data should be deleted.
[-a] | No | -a | Confirmation argument, Required for change to occur.


## /plan manage clear

This command is used to completely empty a database. It uses DROP TABLE queries so everything goes.

Argument | Required | Accepted values | Description
-- | -- | -- | ----
`<DB>` | Yes | sqlite / mysql | Database all data will be cleared from.
[-a] | No | -a | Confirmation argument, Required for change to occur.
