/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 AuroraLS3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plugin.task.nukkit;

import cn.nukkit.scheduler.ServerScheduler;
import com.djrapitops.plugin.NukkitPlugin;
import com.djrapitops.plugin.task.PluginRunnable;
import com.djrapitops.plugin.task.PluginTask;

/**
 * {@link PluginRunnable} implementation for Nukkit.
 *
 * @author AuroraLS3
 */
public abstract class AbsNukkitRunnable implements PluginRunnable, Runnable {

    private final String name;
    private final ServerScheduler scheduler;
    private final long time;
    private int id = -1;

    private NukkitPlugin plugin;

    AbsNukkitRunnable(String name, NukkitPlugin plugin, ServerScheduler scheduler, long time) {
        this.name = name;
        this.scheduler = scheduler;
        this.time = time;
        this.plugin = plugin;
    }

    @Override
    public PluginTask runTask() {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleTask(plugin, this));
        id = task.getTaskId();
        return task;
    }

    @Override
    public PluginTask runTaskAsynchronously() {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleTask(plugin, this, true));
        id = task.getTaskId();
        return task;
    }

    @Override
    public PluginTask runTaskLater(long delay) {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleDelayedTask(plugin, this, (int) delay));
        id = task.getTaskId();
        return task;
    }

    @Override
    public PluginTask runTaskLaterAsynchronously(long delay) {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleDelayedTask(plugin, this, (int) delay, true));
        id = task.getTaskId();
        return task;
    }

    @Override
    public PluginTask runTaskTimer(long delay, long period) {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleDelayedRepeatingTask(plugin, this, (int) delay, (int) period));
        id = task.getTaskId();
        return task;
    }

    @Override
    public PluginTask runTaskTimerAsynchronously(long delay, long period) {
        AbsNukkitTask task = new AbsNukkitTask(scheduler.scheduleDelayedRepeatingTask(plugin, this, (int) delay, (int) period, true));
        id = task.getTaskId();
        return task;
    }

    @Override
    public synchronized void cancel() {
        if (plugin == null || id == -1) {
            return;
        }
        try {
            plugin.getServer().getScheduler().cancelTask(id);
        } finally {
            plugin = null;
        }
    }

    @Override
    public int getTaskId() {
        return id;
    }

    @Override
    public String getTaskName() {
        return name;
    }

    @Override
    public long getTime() {
        return time;
    }
}
