import React from "react";
import {IconDefinition} from "@fortawesome/free-regular-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useTranslation} from "react-i18next";
import {
    faDragon,
    faExclamationCircle,
    faGauge,
    faMap,
    faMicrochip,
    faPowerOff,
    faServer,
    faSignal,
    faStopwatch,
    faUser,
    faUsers,
    faWaveSquare
} from "@fortawesome/free-solid-svg-icons";

type Metric = {
    name: string;
    icon: IconDefinition;
    color: string;
    explanation: string;
}

export const PerformanceHelp = () => {
    const {t} = useTranslation();
    const metrics: Metric[] = [
        {
            name: 'html.label.serverDowntime',
            icon: faPowerOff,
            color: 'downtime',
            explanation: 'html.label.help.performance.serverDowntime'
        }, {
            name: 'html.label.serverUptime',
            icon: faPowerOff,
            color: 'uptime',
            explanation: 'html.label.help.performance.serverUptime'
        }, {
            name: 'html.label.playersOnline',
            icon: faUser,
            color: 'players-online',
            explanation: 'html.label.help.performance.playersOnline'
        }, {
            name: 'html.label.tps',
            icon: faGauge,
            color: 'tps',
            explanation: 'html.label.help.performance.tps'
        }, {
            name: 'html.label.lowTpsSpikes',
            icon: faExclamationCircle,
            color: 'tps',
            explanation: 'html.label.help.performance.lowTpsSpikes'
        }, {
            name: 'html.label.mspt',
            icon: faStopwatch,
            color: 'mspt-average',
            explanation: 'html.label.help.performance.mspt'
        }, {
            name: t('html.label.msptPercentile', {percentile: 95}),
            icon: faStopwatch,
            color: 'mspt-percentile',
            explanation: 'html.label.help.performance.msptPercentile',
        }, {
            name: 'html.label.msptJitterAverage',
            icon: faWaveSquare,
            color: 'mspt-average',
            explanation: 'html.label.help.performance.msptJitterAverage',
        }, {
            name: 'html.label.msptJitterMax',
            icon: faWaveSquare,
            color: 'mspt-percentile',
            explanation: 'html.label.help.performance.msptJitterMax',
        }, {
            name: 'html.label.msptImpactPlayer',
            icon: faUsers,
            color: 'mspt-average',
            explanation: 'html.label.help.performance.msptImpactPlayer',
        }, {
            name: 'html.label.msptImpactChunk',
            icon: faMap,
            color: 'mspt-average',
            explanation: 'html.label.help.performance.msptImpactChunk',
        }, {
            name: 'html.label.entities',
            icon: faDragon,
            color: 'entities',
            explanation: 'html.label.help.performance.entities',
        }, {
            name: 'html.label.loadedChunks',
            icon: faMap,
            color: 'chunks',
            explanation: 'html.label.help.performance.loadedChunks',
        }, {
            name: 'html.label.cpu',
            icon: faGauge,
            color: 'cpu',
            explanation: 'html.label.help.performance.cpu',
        }, {
            name: 'html.label.cpuImpactPerPlayer',
            icon: faUsers,
            color: 'cpu',
            explanation: 'html.label.help.performance.cpuImpactPerPlayer',
        }, {
            name: 'html.label.ram',
            icon: faMicrochip,
            color: 'ram',
            explanation: 'html.label.help.performance.ram',
        }, {
            name: 'html.label.diskSpace',
            icon: faServer,
            color: 'disk',
            explanation: 'html.label.help.performance.diskSpace',
        }, {
            name: 'html.label.minFreeDisk',
            icon: faServer,
            color: 'disk',
            explanation: 'html.label.help.performance.minFreeDisk',
        }, {
            name: 'html.label.maxFreeDisk',
            icon: faServer,
            color: 'disk',
            explanation: 'html.label.help.performance.maxFreeDisk',
        }, {
            name: 'html.label.ping',
            icon: faSignal,
            color: 'ping',
            explanation: 'html.label.help.performance.ping',
        },
    ]

    return (
        <div className={"performance-help"}>
            <p>{t('html.label.help.performance.desc-1')} {t('html.label.help.performance.desc-2')}</p>
            <h4>{t('html.label.help.performance.title')}</h4>
            <table className={"table"}>
                <thead>
                <tr>
                    <td>{t('html.label.help.performance.metric')}</td>
                    <td>{t('html.label.help.performance.explanation')}</td>
                </tr>
                </thead>
                <tbody>
                {metrics.map(metric => (
                    <tr key={metric.name}>
                        <td><FontAwesomeIcon icon={metric.icon} className={"col-" + metric.color}/> {t(metric.name)}
                        </td>
                        <td>{t(metric.explanation)}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    )
}