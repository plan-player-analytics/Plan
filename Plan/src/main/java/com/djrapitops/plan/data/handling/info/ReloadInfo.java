/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling.info;

import java.net.InetAddress;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.GameMode;

/**
 *
 * @author Risto
 */
public class ReloadInfo extends HandlingInfo {
    private LoginInfo info;

    public ReloadInfo(UUID uuid, long time, InetAddress ip, boolean banned, String nickname, GameMode gm) {
        super(uuid, InfoType.RELOAD, time);
        info = new LoginInfo(uuid, time, ip, banned, nickname, gm);
    }

    @Override
    public boolean process(UserData uData) {
        return info.process(uData);
    }
    
}
