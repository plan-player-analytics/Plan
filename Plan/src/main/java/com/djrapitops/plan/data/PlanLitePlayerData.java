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

    public PlanLitePlayerData() {
    }

    public void setTowny(boolean towny) {
        this.towny = towny;
    }

    public void setFactions(boolean factions) {
        this.factions = factions;
    }

    public void setSuperbVote(boolean superbVote) {
        this.superbVote = superbVote;
    }

    public void setVault(boolean vault) {
        this.vault = vault;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public void setPlotPerms(String plotPerms) {
        this.plotPerms = plotPerms;
    }

    public void setPlotOptions(String plotOptions) {
        this.plotOptions = plotOptions;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public boolean hasTowny() {
        return towny;
    }

    public boolean hasFactions() {
        return factions;
    }

    public boolean hasSuperbVote() {
        return superbVote;
    }

    public boolean hasVault() {
        return vault;
    }

    public String getTown() {
        if (towny) {
            return town;
        }
        return "";
    }

    public String getFriends() {
        if (towny) {
            return friends;
        }
        return "";
    }

    public String getPlotPerms() {
        if (towny) {
            return plotPerms;
        }
        return "";
    }

    public String getPlotOptions() {
        if (towny) {
            return plotOptions;
        }
        return "";
    }

    public String getFaction() {
        if (factions) {
            return faction;
        }
        return "";
    }

    public int getVotes() {
        if (superbVote) {
            return votes;
        }
        return -1;
    }

    public double getMoney() {
        if (vault) {
            return money;
        }
        return -1;
    }

}
