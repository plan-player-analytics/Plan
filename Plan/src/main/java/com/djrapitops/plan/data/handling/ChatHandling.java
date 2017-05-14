package main.java.com.djrapitops.plan.data.handling;

import java.util.Arrays;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.UserData;

/**
 * Class containing static methods for processing information contained in a
 * ChatEvent.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class ChatHandling {

    /**
     * Processes the information of the Event and changes UserData object
     * accordingly.
     *
     * @param data UserData of the player.
     * @param nickname Nickname of the player during the event.
     * @param msg Message sent by the player.
     */
    public static void processChatInfo(UserData data, String nickname, String msg) {
        data.addNickname(nickname);
        updateDemographicInformation(msg, data);
    }

    /**
     * Updates Demographics information according to various rules.
     *
     * @param msg Message sent by the player.
     * @param data UserData of the player.
     */
    public static void updateDemographicInformation(String msg, UserData data) {
        List<String> triggers = Arrays.asList(Settings.DEM_TRIGGERS.toString().split(", "));
        List<String> female = Arrays.asList(Settings.DEM_FEMALE.toString().split(", "));
        List<String> male = Arrays.asList(Settings.DEM_MALE.toString().split(", "));
        List<String> ignore = Arrays.asList(Settings.DEM_IGNORE.toString().split(", "));
        String[] messageA = msg.toLowerCase().split("\\s+");
        boolean trigger = false;
        boolean gender = false;
        for (String string : messageA) {
            if (ignore.contains(string)) {
                trigger = false;
                break;
            }
            if (triggers.contains(string)) {
                trigger = true;
            }
            if (female.contains(string) || male.contains(string)) {
                gender = true;
            }
        }
        if (!trigger) {
            return;
        }
        int ageNum = -1;
        for (String string : messageA) {
            try {
                ageNum = Integer.parseInt(string);
                if (ageNum != -1) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        if (ageNum != -1 && ageNum < 100) {
            data.getDemData().setAge(ageNum);
        }
        if (gender) {
            for (String string : messageA) {
                if (female.contains(string)) {
                    data.getDemData().setGender(Gender.FEMALE);
                } else if (male.contains(string)) {
                    data.getDemData().setGender(Gender.MALE);
                }
            }
        }
    }
}
