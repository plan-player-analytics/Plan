package net.playeranalytics.plugin.scheduling;

public class FabricTask implements Task {

    @Override
    public boolean isGameThread() {
        return false;
    }

    @Override
    public void cancel() {

    }
}
