
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

    public PlanLiteAnalyzedData() {
    }

    public HashMap<String, Integer> getTownMap() {
        return townMap;
    }

    public void setTownMap(HashMap<String, Integer> townMap) {
        this.townMap = townMap;
    }

    public HashMap<String, Integer> getFactionMap() {
        return factionMap;
    }

    public void setFactionMap(HashMap<String, Integer> factionMap) {
        this.factionMap = factionMap;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public int getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(int totalMoney) {
        this.totalMoney = totalMoney;
    }
    
    
    
}
