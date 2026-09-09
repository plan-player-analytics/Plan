import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchOptimizedPerformance, fetchPingGraph, fetchPluginHistory} from "../../service/serverService";
import {useEffect, useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {useAuth} from "../../hooks/authenticationHook";
import {mapPerformanceDataToSeries} from "../../util/graphs";
import {PlanDataResponse} from "../../service/PlanResponse";
import {PluginHistory} from "../model/graph/performance/PluginHistory";
import {PingGraph} from "../model/graph/performance/PingGraph";
import {ParsedPerformanceGraph, PerformanceGraph} from "../model/graph/performance/PerformanceGraph";
import {ParsedPerformanceZones} from "../model/graph/performance/PerformanceZones";

type Props = {
    identifier: string | null;
}

export const usePerformanceGraphData = ({identifier}: Props) => {
    const {t} = useTranslation();
    const {authRequired, hasPermission} = useAuth();
    const {
        data,
        loadingError
    } = useDataRequest(fetchOptimizedPerformance, [identifier]) as any as PlanDataResponse<PerformanceGraph>;
    const [parsedData, setParsedData] = useState<ParsedPerformanceGraph | undefined>(undefined);
    const {
        data: pluginHistory,
        loadingError: pluginHistoryLoadingError
    } = useDataRequest(fetchPluginHistory, [identifier], authRequired && hasPermission('page.server.plugin.history')) as any as PlanDataResponse<PluginHistory>;
    const {
        data: pingSeries,
        loadingError: pingLoadingError
    } = useDataRequest(fetchPingGraph, [identifier], hasPermission('page.server.performance.graphs.ping')) as any as PlanDataResponse<PingGraph>;

    useEffect(() => {
        if (data) {
            mapPerformanceDataToSeries(data.values).then((parsed: ParsedPerformanceGraph) => setParsedData(parsed))
        }
    }, [data, setParsedData]);
    const pluginHistorySeries = useMemo(() => {
        if (pluginHistory) {
            const grouped = Object.groupBy(pluginHistory.history.toReversed(), ({modified}) => modified);
            return {
                type: 'flags',
                accessibility: {
                    exposeAsGroupOnly: true,
                    description: t('html.label.pluginVersionHistory')
                },
                name: t('html.label.pluginHistory'),
                tooltip: {headerFormat: ''},
                data: Object.entries(grouped).map(entry => {
                    const plugins = entry[1] || []
                    const installedLines = plugins.filter(p => p.version).map(plugin => plugin.name + ': ' + plugin.version).join(', <br>');
                    const uninstalledLines = plugins.filter(p => !p.version).map(plugin => plugin.name).join(', <br>');
                    return {
                        x: entry[0],
                        title: plugins.length,
                        text: (installedLines.length ? '<b>' + t('html.label.installed') + '</b><br>' + installedLines : '') +
                            (uninstalledLines.length ? '<br><b>' + t('html.label.uninstalled') + '</b><br>' + uninstalledLines : '')
                    }
                })
            };
        }
    }, [pluginHistory, t]);

    const zones: ParsedPerformanceZones = useMemo(() => ({
        tps: data?.zones?.tpsThresholdMed ? [{
            value: data.zones.tpsThresholdMed,
            color: "var(--color-graphs-tps-low)"
        }, {
            value: data.zones.tpsThresholdHigh,
            color: "var(--color-graphs-tps-medium)"
        }, {
            value: 30,
            color: "var(--color-graphs-tps-high)"
        }] : undefined,
        disk: data?.zones?.diskThresholdMed ? [{
            value: data.zones.diskThresholdMed,
            color: "var(--color-graphs-disk-low)"
        }, {
            value: data.zones.diskThresholdHigh,
            color: "var(--color-graphs-disk-medium)"
        }, {
            value: Number.MAX_VALUE,
            color: "var(--color-graphs-disk-high)"
        }] : undefined
    }), [data]);

    return useMemo(() => ({
        zones,
        performanceSeries: parsedData,
        pluginHistorySeries,
        pingSeries,
        loadingError: loadingError || pluginHistoryLoadingError || pingLoadingError
    }), [parsedData, pluginHistorySeries, pingSeries])
}