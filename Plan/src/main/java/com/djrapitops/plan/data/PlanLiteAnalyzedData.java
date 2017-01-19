package main.java.com.djrapitops.plan.data;

import java.util.HashMap;

/**
 *
 * @author Rsl1122
 */
public class PlanLiteAnalyzedData {

    private HashMap<String, Integer> townMap;
    private HashMap<String, Integer> factionMap;
    private int totalVotes;
    private int totalMoney;

    /**
     * Class Constructor.
     *
     * All data has to be set to avoid NPE.
     */
    public PlanLiteAnalyzedData() {
    }

    /**
     * @return Map of Town names with residents inside them.
     */
    public HashMap<String, Integer> getTownMap() {
        return townMap;
    }

    /**
     * @param townMap Map of Town names with residents inside them.
     */
    public void setTownMap(HashMap<String, Integer> townMap) {
        this.townMap = townMap;
    }

    /**
     * @return Map of Faction names with players inside them.
     */
    public HashMap<String, Integer> getFactionMap() {
        return factionMap;
    }

    /**
     * @param factionMap Map of Faction names with players inside them.
     */
    public void setFactionMap(HashMap<String, Integer> factionMap) {
        this.factionMap = factionMap;
    }

    /**
     * @return Amount of votes the server has recieved.
     */
    public int getTotalVotes() {
        return totalVotes;
    }

    /**
     * @param totalVotes Amount of votes the server has recieved.
     */
    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    /**
     * @return Amount of money that is in the economy.
     */
    public int getTotalMoney() {
        return totalMoney;
    }

    /**
     * @param totalMoney Amount of money that is in the economy.
     */
    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }

}
