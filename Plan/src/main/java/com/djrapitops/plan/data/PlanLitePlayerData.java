package main.java.com.djrapitops.plan.data;

/**
 *
 * @author Rsl1122
 */
public class PlanLitePlayerData {

    private boolean towny;
    private boolean factions;
    private boolean superbVote;
    private boolean vault;

    private String town;
    private String friends;
    private String plotPerms;
    private String plotOptions;

    private String faction;

    private int votes;

    private double money;

    /**
     * Class Constructor.
     *
     * All data has to be set to avoid NPE
     */
    public PlanLitePlayerData() {
    }

    /**
     * @param towny Is Towny installed?
     */
    public void setTowny(boolean towny) {
        this.towny = towny;
    }

    /**
     * @param factions Is Factions installed?
     */
    public void setFactions(boolean factions) {
        this.factions = factions;
    }

    /**
     * @param superbVote Is SuperbVote installed?
     */
    public void setSuperbVote(boolean superbVote) {
        this.superbVote = superbVote;
    }

    /**
     * @param vault Is Vault installed?
     */
    public void setVault(boolean vault) {
        this.vault = vault;
    }

    /**
     * @param town Name of town player is resident in (Towny)
     */
    public void setTown(String town) {
        this.town = town;
    }

    /**
     * @param friends Friends of player (Towny)
     */
    public void setFriends(String friends) {
        this.friends = friends;
    }

    /**
     * @param plotPerms Perms of player (Towny)
     */
    public void setPlotPerms(String plotPerms) {
        this.plotPerms = plotPerms;
    }

    /**
     * @param plotOptions Options of player (Towny)
     */
    public void setPlotOptions(String plotOptions) {
        this.plotOptions = plotOptions;
    }

    /**
     * @param faction Faction the player is in
     */
    public void setFaction(String faction) {
        this.faction = faction;
    }

    /**
     * @param votes How many votes the player has
     */
    public void setVotes(int votes) {
        this.votes = votes;
    }

    /**
     * @param money How much money the player has
     */
    public void setMoney(double money) {
        this.money = money;
    }

    /**
     * @return Is Towny installed?
     */
    public boolean hasTowny() {
        return towny;
    }

    /**
     * @return Is Factions installed?
     */
    public boolean hasFactions() {
        return factions;
    }

    /**
     * @return Is Superbvote installed?
     */
    public boolean hasSuperbVote() {
        return superbVote;
    }

    /**
     * @return Is Vault installed?
     */
    public boolean hasVault() {
        return vault;
    }

    /**
     * @return Town player is resident in
     */
    public String getTown() {
        if (towny) {
            return town;
        }
        return "";
    }

    /**
     * @return Friends of player (towny)
     */
    public String getFriends() {
        if (towny) {
            return friends;
        }
        return "";
    }

    /**
     * @return Perms of player (towny)
     */
    public String getPlotPerms() {
        if (towny) {
            return plotPerms;
        }
        return "";
    }

    /**
     * @return Options of player (towny)
     */
    public String getPlotOptions() {
        if (towny) {
            return plotOptions;
        }
        return "";
    }

    /**
     * @return Faction of player
     */
    public String getFaction() {
        if (factions) {
            return faction;
        }
        return "";
    }

    /**
     * @return How many times player has voted, -1 if superbvote not installed
     */
    public int getVotes() {
        if (superbVote) {
            return votes;
        }
        return -1;
    }

    /**
     * @return How much money player has. -1 if vault not installed
     */
    public double getMoney() {
        if (vault) {
            return money;
        }
        return -1;
    }

}
