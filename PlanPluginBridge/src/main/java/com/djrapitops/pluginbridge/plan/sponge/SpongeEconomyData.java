/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan.sponge;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Sponge.
 *
 * @author BrainStone
 */
class SpongeEconomyData extends PluginData {
    private static final Color color = Color.AMBER;
    private static final String nameMoneyIcon = "money-bill-wave";
    private static final Icon moneyIcon = Icon.called(nameMoneyIcon).build();
    private static final Icon moneyIconColored = Icon.called(nameMoneyIcon).of(color).build();

    private final EconomyService economyService;

    SpongeEconomyData(EconomyService economyService) {
        super(ContainerSize.THIRD, "Sponge Economy");

        this.economyService = economyService;

        setPluginIcon(moneyIconColored);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(uuid);

        if (!uOpt.isPresent()) {
            return inspectContainer;
        }

        UniqueAccount acc = uOpt.get();

        for(Currency currency : economyService.getCurrencies()) {
            BigDecimal balance = acc.getBalance(currency);
            inspectContainer.addValue(getWithIcon(currency.getName(), moneyIconColored), currency.format(balance).toPlain());
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        List<UniqueAccount> players = uuids.stream().map(economyService::getOrCreateAccount)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        for(Currency currency : economyService.getCurrencies()) {
            addCurrencyToContainer(currency, players, analysisContainer);
        }

        return analysisContainer;
    }

    private void addCurrencyToContainer(Currency currency, List<UniqueAccount> players, AnalysisContainer analysisContainer) {
        BigDecimal totalBalance = BigDecimal.ZERO;
        Map<UUID, String> playerBalances = new HashMap<>();

        for (UniqueAccount player : players) {
            BigDecimal balance = player.getBalance(currency);

            totalBalance = totalBalance.add(balance);
            playerBalances.put(player.getUniqueId(), currency.format(balance).toPlain());
        }

        analysisContainer.addValue(getWithIcon("Total Server Balance " + currency.getName(), moneyIconColored), currency.format(totalBalance).toPlain());
        analysisContainer.addPlayerTableValues(getWithIcon("Balance " + currency.getName(), moneyIcon), playerBalances);
    }
}
