/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.GamemodeHandling;
import org.bukkit.GameMode;

/**
 *
 * @author Risto
 */
public class GamemodeInfo extends HandlingInfo{
    private GameMode currentGamemode;

    public GamemodeInfo(UUID uuid, long time, GameMode gm) {
        super(uuid, InfoType.GM, time);
        currentGamemode = gm;
    }

    @Override
    public boolean process(UserData uData) {
        if (currentGamemode == null) {
            return false;
        }
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        GamemodeHandling.processGamemodeInfo(uData, time, currentGamemode);
        return true;
    }
}
