/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import java.util.Arrays;
import java.util.List;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.Gender;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Risto
 */
public class ChatHandling {
    public static void processChatInfo(UserData data, String nickname, String msg) {
        data.addNickname(nickname);
        updateDemographicInformation(msg, data);
    }

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
