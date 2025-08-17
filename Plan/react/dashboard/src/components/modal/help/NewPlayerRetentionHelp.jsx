import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from "react-i18next";
import Graph from "../../graphs/Graph";
import RangeSlider from "react-bootstrap-range-slider";

const NewPlayerRetentionHelp = () => {
    const {t} = useTranslation();
    const clockNow = new Date().getTime() % (24 * 60 * 60 * 1000);
    const start = clockNow;
    const retentionStart = clockNow + 12 * 60 * 60 * 1000
    const end = clockNow + 24 * 60 * 60 * 1000;

    const [session1Time, setSession1Time] = useState(clockNow + 1243000);
    const [session2Time, setSession2Time] = useState(clockNow + (24 * 60 * 60 * 1000) * (7 / 8));
    const [result, setResult] = useState(true);

    useEffect(() => {
        setResult(
            start <= session1Time && end >= session1Time &&
            retentionStart <= session2Time && end >= session2Time
        );
    }, [session1Time, session2Time, setResult, end, retentionStart, start])

    const graphOptions = useMemo(() => {
        return {
            chart: {
                type: 'dumbbell',
                inverted: true
            },
            title: {text: ''},
            legend: {
                enabled: true
            },
            tooltip: {
                enabled: false
            },
            xAxis: {
                labels: {
                    enabled: false
                }
            },
            yAxis: {
                type: 'datetime',
                labels: {
                    enabled: false
                },
                plotLines: [{
                    label: {text: t('html.label.registered')},
                    color: 'var(--color-data-play-first-seen)',
                    value: session1Time,
                    width: 2
                }, {
                    label: {text: t('html.label.session')},
                    color: 'var(--color-data-play-sessions)',
                    value: session2Time,
                    width: 2
                }],
                title: {text: t('html.label.last7days')}
            },
            series: [{
                name: t('html.query.filter.registeredBetween.text'),
                animation: false,
                color: 'var(--color-data-play-first-seen)',
                data: [{
                    name: t('html.query.filter.registeredBetween.text'),
                    low: start,
                    high: end
                }],
            }, {
                name: t('html.query.filter.playedBetween.text'),
                animation: false,
                color: 'var(--color-data-play-sessions)',
                data: [{
                    name: t('html.query.filter.playedBetween.text'),
                    low: retentionStart,
                    high: end
                }],
            }]
        }
    }, [start, end, retentionStart, session1Time, session2Time, t]);

    const updateSession1Time = useCallback(event => setSession1Time(event.target.value), [setSession1Time]);
    const updateSession2Time = useCallback(event => setSession2Time(event.target.value), [setSession2Time]);

    return (
        <>
            <p>{t('html.label.help.retentionBasis')}</p>
            <Graph id={"new-player-retention-help"} options={graphOptions}/>
            <hr/>
            <p>{t('html.label.help.testPrompt')}</p>
            <label>{t('html.label.firstSession')}</label>
            <RangeSlider
                value={session1Time}
                onChange={updateSession1Time}
                min={start}
                max={end}
                tooltip={'off'}/>
            <label>{t('html.label.session') + ' 2'}</label>
            <RangeSlider
                value={session2Time}
                onChange={updateSession2Time}
                min={session1Time}
                max={end}
                tooltip={'off'}/>
            <p>{t('html.label.help.testResult')}: <b>{result ? t('plugin.generic.yes') : t('plugin.generic.no')}</b></p>
        </>
    )
};

export default NewPlayerRetentionHelp