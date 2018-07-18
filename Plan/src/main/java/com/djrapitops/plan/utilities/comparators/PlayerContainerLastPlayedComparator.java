/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;

import java.util.Comparator;

/**
 * Comparator for PlayerContainer so that most recently seen is first.
 *
 * @author Rsl1122
 */
public class PlayerContainerLastPlayedComparator implements Comparator<PlayerContainer> {

    @Override
    public int compare(PlayerContainer playerOne, PlayerContainer playerTwo) {
        return Long.compare(
                playerTwo.getValue(PlayerKeys.LAST_SEEN).orElse(0L),
                playerOne.getValue(PlayerKeys.LAST_SEEN).orElse(0L)
        );
    }
}
