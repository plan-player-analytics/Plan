![Player Analytics](https://puu.sh/t8vin.png)
# Commands & Permissions

- [Plugin.yml](/Plan/src/main/resources/plugin.yml)
- [Permissions Enum](/Plan/src/main/java/com/djrapitops/plan/Permissions.java)

This page is an in depth documentation of all permissions & commands.  
Page version: **3.5.2**

# Commands

Command | Permission | Default | Description
--- | ---- | ------ | ---------------------------------
/plan | plan.? | true | Base command, Shows help. Alias for /plan inspect [player] when used like /plan [player]
/plan inspect | plan.inspect | true | Gives the link to player page of the player who issued the command.
/plan inspect [player] | plan.inspect.other | OP | Gives link to player page of the given playername, CaSe-SenSiTiVe.
/plan qinspect | plan.qinspect | OP | Displays information about the issuing player in the chatbox.
/plan qinspect [player] | plan.qinspect.other | OP | Displays information about the player in the chatbox, CaSe-SenSiTiVe.
/plan analyze | plan.analyze | OP | Gives a link to the Analysis page of the server.
/plan qanalyze | plan.qanalyze | OP | Displays analysis information in the chatbox.
/plan search [argument] | plan.search | OP | Searches for matching playernames & gives the link to matching player's pages.
/plan reload | plan.reload | OP | Reloads the plugin. (All active play-sessions will be ended & started.)
/plan info | plan.info | OP | View version, update availability & active database
/plan manage | plan.manage | OP | Manage the database of the plugin.
/plan register | none / plan.webmanage | true | Register the sender / another user (requires the perm)  
/plan webuser | plan.webmanage | OP | Manage web users.
none | plan.ignore.commanduse | false | Commands issued by players with this permission will be ignored.
  
## Permission Groups:

### plan.basic:
- plan.?
- plan.inspect
- plan.qinspect

### plan.advanced
- plan.basic
- plan.info
- plan.qanalyze

### plan.staff
- plan.advanced
- plan.search
- plan analyze
- plan.inspect.other
- plan.qinspect.other
- plan.reload
- plan.webmanage

### plan.*
- plan.staff
- plan.manage
