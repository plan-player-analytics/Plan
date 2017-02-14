package main.java.com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.data.DemographicsData;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class DataCombineUtils {

    /**
     * Combines two conflicting UserDatasets from databases.
     *
     * @param allFromUserData First Dataset
     * @param allToUserData Second Dataset
     * @param uuids UUIDs from both Datasets
     * @return Combined UserDataset
     */
    public static List<UserData> combineUserDatas(HashMap<UUID, UserData> allFromUserData, HashMap<UUID, UserData> allToUserData, Set<UUID> uuids) {
        List<UserData> combinedData = new ArrayList<>();
        uuids.forEach((UUID uuid) -> {
            UserData fData = allFromUserData.get(uuid);
            UserData tData = allToUserData.get(uuid);
            if (fData == null) {
                combinedData.add(tData);
            } else if (tData == null) {
                combinedData.add(fData);
            } else {
                combinedData.add(combineUserData(fData, tData));
            }
        });
        return combinedData;
    }

    private static UserData combineUserData(UserData fData, UserData tData) {
        if (fData.getLastGmSwapTime() < tData.getLastGmSwapTime()) {
            fData.setLastGmSwapTime(tData.getLastGmSwapTime());
            fData.setLastGamemode(tData.getLastGamemode());
        }
        HashMap<GameMode, Long> gmTimes = fData.getGmTimes();
        HashMap<GameMode, Long> tGmTimes = tData.getGmTimes();
        gmTimes.keySet().stream().forEach((GameMode gm) -> {
            long fTime = gmTimes.get(gm);
            if (tGmTimes.get(gm) != null) {
                long tTime = tGmTimes.get(gm);
                gmTimes.put(gm, fTime + tTime);
            }
        });
        if (fData.getLastPlayed() < tData.getLastPlayed()) {
            fData.setLastPlayed(tData.getLastPlayed());
        }
        fData.setPlayTime(fData.getPlayTime() + tData.getPlayTime());
        fData.setTimesKicked(fData.getTimesKicked() + tData.getTimesKicked());
        fData.setLoginTimes(fData.getLoginTimes() + tData.getLoginTimes());
        fData.addLocations(tData.getLocations());
        fData.addNicknames(tData.getNicknames());
        fData.addIpAddresses(tData.getIps());
        DemographicsData tDemData = tData.getDemData();
        DemographicsData fDemData = fData.getDemData();
        if (tDemData.getAge() > fDemData.getAge()) {
            fDemData.setAge(tDemData.getAge());
        }
        if (fDemData.getGeoLocation().equals(Phrase.DEM_UNKNOWN+"")) {
            fDemData.setGeoLocation(tDemData.getGeoLocation());
        }
        fData.setDemData(fDemData);
        return fData;
    }

    /**
     * Combines Two conflicting command usage datasets.
     *
     * @param fData First Dataset
     * @param tData Second Dataset
     * @return Combined Dataset
     */
    public static HashMap<String, Integer> combineCommandUses(HashMap<String, Integer> fData, HashMap<String, Integer> tData) {
        HashMap<String, Integer> combinedData = new HashMap<>();
        Set<String> allCommands = new HashSet<>();
        if (fData != null) {
            allCommands.addAll(fData.keySet());
        }
        if (tData != null) {
            allCommands.addAll(tData.keySet());
        }
        for (String command : allCommands) {
            boolean fDataHasCommand = false;
            if (fData != null) {
                fDataHasCommand = fData.keySet().contains(command);
            }
            boolean tDataHasCommand = false;
            if (tData != null) {
                tDataHasCommand = tData.keySet().contains(command);
            }
            int value = 0;
            if (fDataHasCommand) {
                value += fData.get(command);
            }
            if (tDataHasCommand) {
                value += tData.get(command);
            }
            combinedData.put(command, value);
        }
        return combinedData;
    }
}
