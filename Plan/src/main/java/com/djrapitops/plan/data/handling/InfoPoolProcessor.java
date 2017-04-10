/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DBCallableProcessor;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.handling.info.*;
import main.java.com.djrapitops.plan.utilities.comparators.HandlingInfoTimeComparator;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Risto
 */
public class InfoPoolProcessor {

    private Plan plugin;
    private DataCacheHandler handler;
    private List<HandlingInfo> pool;

    public InfoPoolProcessor(Plan plugin) {
        this.plugin = plugin;
        handler = plugin.getHandler();
        pool = new ArrayList<>();
    }
    
    public void startPoolTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                processPool();
            }
        }.runTaskTimerAsynchronously(plugin, 20*60, 20*60);
    }
    
    public void processPool() {
        List<HandlingInfo> toProcess = new ArrayList<>(pool);
        try {
            pool.removeAll(toProcess);
            List<UUID> uuids = toProcess.parallelStream().map(i -> i.getUuid()).distinct().collect(Collectors.toList());
            Map<UUID, UserData> userData = getAffectedUserData(uuids);

            Collections.sort(toProcess, new HandlingInfoTimeComparator());
            for (HandlingInfo r : toProcess) {
                UserData data = userData.get(r.getUuid());
                if (data == null) {
                    pool.add(r);
                    continue;
                }
                r.process(data);
            }
        } catch (Exception e) {
            plugin.toLog(this.getClass().getName(), e);
            pool.addAll(toProcess);
        }
    }

    public Map<UUID, UserData> getAffectedUserData(List<UUID> uuids) {
        Map<UUID, UserData> userData = new HashMap<>();
        for (UUID uuid : uuids) {
            DBCallableProcessor processor = new DBCallableProcessor() {
                @Override
                public void process(UserData data) {
                    userData.put(data.getUuid(), data);
                }
            };
            handler.getUserDataForProcessing(processor, uuid);
        }
        int waitAttempts = 0;
        while (uuids.size() < userData.size()) {
            if (waitAttempts >= 15) {
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            waitAttempts++;
        }
        return userData;
    }
    
    public void addToPool(HandlingInfo info) {
        pool.add(info);
    }
}
