package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.DataContainer;

import java.util.List;

public class PvpInfoMutator {

    private final SessionsMutator sessionsMutator;

    private PvpInfoMutator(SessionsMutator sessionsMutator) {
        this.sessionsMutator = sessionsMutator;
    }

    public PvpInfoMutator(List<Session> sessions) {
        this(new SessionsMutator(sessions));
    }

    public static PvpInfoMutator forContainer(DataContainer container) {
        return new PvpInfoMutator(SessionsMutator.forContainer(container));
    }

    public static PvpInfoMutator forMutator(SessionsMutator sessionsMutator) {
        return new PvpInfoMutator(sessionsMutator);
    }

    public double killDeathRatio() {
        int deathCount = sessionsMutator.toPlayerDeathCount();
        return sessionsMutator.toPlayerKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public int mobCausedDeaths() {
        return sessionsMutator.toDeathCount() - sessionsMutator.toPlayerDeathCount();
    }

    public double mobKillDeathRatio() {
        int deathCount = mobCausedDeaths();
        return sessionsMutator.toMobKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public int mobKills() {
        return sessionsMutator.toMobKillCount();
    }

    public int playerKills() {
        return sessionsMutator.toPlayerKillCount();
    }

    public int deaths() {
        return sessionsMutator.toDeathCount();
    }

    public int playerCausedDeaths() {
        return sessionsMutator.toPlayerDeathCount();
    }
}
