/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import java.util.Comparator;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Risto
 */
public class UserDataLastPlayedComparator implements Comparator<UserData> {

    @Override
    public int compare(UserData u1, UserData u2) {
        return Long.compare(u2.getLastPlayed(), u1.getLastPlayed());
    }
}
