package net.playeranalytics.plugin.scheduling;

public class UnscheduledFabricTask implements UnscheduledTask {

    @Override
    public Task runTaskAsynchronously() {
        return null;
    }

    @Override
    public Task runTaskLaterAsynchronously(long l) {
        return null;
    }

    @Override
    public Task runTaskTimerAsynchronously(long l, long l1) {
        return null;
    }

    @Override
    public Task runTask() {
        return null;
    }

    @Override
    public Task runTaskLater(long l) {
        return null;
    }

    @Override
    public Task runTaskTimer(long l, long l1) {
        return null;
    }
}
