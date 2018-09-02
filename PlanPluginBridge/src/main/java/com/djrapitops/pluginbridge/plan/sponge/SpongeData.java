package com.djrapitops.pluginbridge.plan.sponge;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.cache.DataCache;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for Sponge.
 *
 * @author BrainStone
 */
public class SpongeData extends PluginData {
    private static final Color color = Color.AMBER;
    private static final String nameMoneyIcon = "money-bill-wave";
    private static final Icon moneyIcon = Icon.called(nameMoneyIcon).build();
    private static final Icon moneyIconColored = Icon.called(nameMoneyIcon).of(color).build();
    
    private final EconomyService economyService;
    
    public SpongeData(EconomyService economyService) {
        super(ContainerSize.THIRD, "Sponge Economy");
        
        this.economyService = economyService;
        
        setPluginIcon(moneyIconColored);
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String name = DataCache.getInstance().getName(uuid);
        
        if (name == null)
            return inspectContainer;
        
        Optional<UniqueAccount> uOpt = economyService.getOrCreateAccount(uuid);
        if (uOpt.isPresent()) {
            UniqueAccount acc = uOpt.get();
            
            for(Currency currency : economyService.getCurrencies()) {
                BigDecimal balance = acc.getBalance(currency);
                inspectContainer.addValue(getWithIcon(currency.getName(), moneyIconColored), currency.format(balance).toPlain());
            }
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        return analysisContainer;
    }
}
