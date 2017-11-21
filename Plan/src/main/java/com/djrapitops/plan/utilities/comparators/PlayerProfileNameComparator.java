/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.PlayerProfile;

import java.util.Comparator;

/**
 * Comparator for PlayerProfile for Alphabetical Name order.
 *
 * @author Rsl1122
 */
public class PlayerProfileNameComparator implements Comparator<PlayerProfile> {

    @Override
    public int compare(PlayerProfile u1, PlayerProfile u2) {
        return u1.getName().compareTo(u2.getName());
    }
}
