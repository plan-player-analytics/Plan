package net.playeranalytics.plan.gathering.listeners;

public interface FabricListener {

    public void register();

    public boolean isEnabled();

    public void enable();

    public void disable();
}
