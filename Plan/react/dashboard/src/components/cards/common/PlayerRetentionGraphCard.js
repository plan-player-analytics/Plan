import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {Card, Col, Row} from "react-bootstrap";
import CardHeader from "../CardHeader";
import {faUsersViewfinder} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import ExtendableCardBody from "../../layout/extension/ExtendableCardBody";
import {BasicDropdown} from "../../input/BasicDropdown";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchRetentionData} from "../../../service/serverService";
import {ErrorViewCard} from "../../../views/ErrorView";
import {CardLoader} from "../../navigation/Loader";
import {tooltip} from "../../../util/graphs";
import {hsvToRgb, randomHSVColor, rgbToHexString, withReducedSaturation} from "../../../util/colors";
import LineGraph from "../../graphs/LineGraph";
import FunctionPlotGraph from "../../graphs/FunctionPlotGraph";
import {useTheme} from "../../../hooks/themeHook";

const dayMs = 24 * 3600000;
const getWeek = (date) => {
    const onejan = new Date(date.getFullYear(), 0, 1);
    const today = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const dayOfYear = ((today - onejan + 86400000) / 86400000);
    return Math.ceil(dayOfYear / 7)
};

const PlayerRetentionGraphCard = ({identifier}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const time = useMemo(() => new Date().getTime(), []);

    const {data, loadingError} = useDataRequest(fetchRetentionData, [identifier]);

    const [selectedWindow, setSelectedWindow] = useState('days');
    const windowOptions = useMemo(() => [
        {name: 'hours', displayName: t('html.label.time.hours'), increment: 3600000},
        {name: 'days', displayName: t('html.label.time.days'), increment: dayMs},
        {name: 'weeks', displayName: t('html.label.time.weeks'), increment: 7 * dayMs},
        {name: 'months', displayName: t('html.label.time.months'), increment: 30 * dayMs},
    ], [t]);
    const [selectedGroup, setSelectedGroup] = useState('registered-7d');
    const groupOptions = useMemo(() => [
        {name: 'registered-7d', displayName: t('html.label.retention.inLast7d'), start: time - 7 * dayMs},
        {name: 'registered-30d', displayName: t('html.label.retention.inLast30d'), start: time - 30 * dayMs},
        {name: 'registered-3m', displayName: t('html.label.retention.inLast90d'), start: time - 3 * 30 * dayMs},
        {name: 'registered-6m', displayName: t('html.label.retention.inLast180d'), start: time - 6 * 30 * dayMs},
        {name: 'registered-1y', displayName: t('html.label.retention.inLast365d'), start: time - 365 * dayMs},
        {name: 'registered-2y', displayName: t('html.label.retention.inLast730d'), start: time - 2 * 365 * dayMs},
        {name: 'registered-ever', displayName: t('html.label.retention.inAnytime'), start: 0},
    ], [t, time]);
    const [selectedGroupBy, setSelectedGroupBy] = useState('none');
    const groupByOptions = useMemo(() => [
        {name: 'none', displayName: t('html.label.retention.groupByNone')},
        {name: 'days', displayName: t('html.label.time.day')},
        {name: 'weeks', displayName: t('html.label.time.week')},
        {name: 'months', displayName: t('html.label.time.month')},
        {name: 'years', displayName: t('html.label.time.year')},
    ], [t]);
    const [selectedYAxis, setSelectedYAxis] = useState('percentage');
    const yAxisOptions = useMemo(() => [
        {name: 'percentage', displayName: t('html.label.unit.percentage')},
        {name: 'count', displayName: t('html.label.unit.playerCount')},
    ], [t]);
    const [selectedAxis, setSelectedAxis] = useState('time');
    const axisOptions = useMemo(() => [
        {name: 'date', displayName: t('html.label.time.date')},
        {name: 'time', displayName: t('html.label.retention.timeSinceRegistered')},
        {name: 'playtime', displayName: t('html.label.playtime')}
    ], [t]);

    const [series, setSeries] = useState([]);
    const [graphOptions, setGraphOptions] = useState({title: {text: ''},});

    const mapToData = useCallback(async (dataToMap, start) => {
        const total = dataToMap.length;
        let seriesData = [];
        const increment = windowOptions.find(option => option.name === selectedWindow).increment;
        const xAxis = axisOptions.find(option => option.name === selectedAxis).name;
        switch (xAxis) {
            case 'date':
                const retainedBasedOnDate = [];
                const firstRegisterDateStart = dataToMap[0].registerDate - dataToMap[0].registerDate % dayMs;
                for (let date = firstRegisterDateStart; date < time; date += increment) {
                    const retainedSince = dataToMap.filter(player => player.lastSeenDate >= date).length;
                    retainedBasedOnDate.push([date, selectedYAxis === 'percentage' ? retainedSince * 100.0 / total : retainedSince]);
                    if (retainedSince < 0.5) break;
                }
                seriesData = retainedBasedOnDate;
                break;
            case 'time':
                const retainedBasedOnTime = [];
                for (let i = 0; i < time; i += increment) {
                    const retainedSince = dataToMap.filter(point => point.timeDifference > i).length;
                    retainedBasedOnTime.push([(i) / increment, selectedYAxis === 'percentage' ? retainedSince * 100.0 / total : retainedSince]);
                    if (retainedSince < 0.5) break;
                }
                seriesData = retainedBasedOnTime;
                break;
            case 'playtime':
            default:
                const retainedBasedOnPlaytime = [];
                for (let i = start; i < time; i += increment) {
                    const retainedSince = dataToMap.filter(point => point.playtime > i - start).length;
                    retainedBasedOnPlaytime.push([(i - start) / increment, selectedYAxis === 'percentage' ? retainedSince * 100.0 / total : retainedSince]);
                    if (retainedSince < 0.5) break;
                }
                seriesData = retainedBasedOnPlaytime;
                break;
        }
        return seriesData;
    }, [selectedWindow, windowOptions, selectedAxis, axisOptions, selectedYAxis, time])

    const group = useCallback(async filtered => {
        const grouped = {};
        const groupBy = groupByOptions.find(option => option.name === selectedGroupBy).name;
        for (const point of filtered) {
            const date = new Date();
            date.setTime(point.registerDate);
            switch (groupBy) {
                case 'days':
                    const day = date.toISOString().substring(0, 10);
                    if (!grouped[day]) grouped[day] = [];
                    grouped[day].push(point);
                    break;
                case 'weeks':
                    const week = date.getUTCFullYear() + '-week-' + getWeek(date);
                    if (!grouped[week]) grouped[week] = [];
                    grouped[week].push(point);
                    break;
                case 'months':
                    const month = date.toISOString().substring(0, 7);
                    if (!grouped[month]) grouped[month] = [];
                    grouped[month].push(point);
                    break;
                case 'years':
                    const year = date.getUTCFullYear();
                    if (!grouped[year]) grouped[year] = [];
                    grouped[year].push(point);
                    break;
                case 'none':
                default:
                    grouped['all'] = filtered;
                    break;
            }
        }
        return grouped;
    }, [groupByOptions, selectedGroupBy]);

    const createSeries = useCallback(async (retentionData) => {

        const start = groupOptions.find(option => option.name === selectedGroup).start;
        const filtered = retentionData.filter(point => point.registerDate > start)
            .sort((a, b) => a.registerDate - b.registerDate);

        const grouped = await group(filtered);

        let i = 0;
        return Promise.all(Object.entries(grouped).map(async group => {
            const name = group[0];
            const groupData = group[1];
            const color = rgbToHexString(hsvToRgb(randomHSVColor(i)));
            i++;
            return {
                name: name,
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                data: await mapToData(groupData, start),
                color: nightModeEnabled ? withReducedSaturation(color) : color
            };
        }));
    }, [nightModeEnabled, mapToData, groupOptions, selectedGroup, group]);

    useEffect(() => {
        if (!data) return;

        createSeries(data.player_retention).then(setSeries);
    }, [data, createSeries, setSeries]);

    useEffect(() => {
        const windowName = windowOptions.find(option => option.name === selectedWindow).displayName;
        const unitLabel = selectedYAxis === 'percentage' ? 'Retained Players %' : t('html.label.players');
        const axisName = axisOptions.find(option => option.name === selectedAxis).displayName;
        setGraphOptions({
            title: {text: ''},
            rangeSelector: selectedAxis === 'date' ? {
                selected: 2,
                buttons: [{
                    type: 'day',
                    count: 7,
                    text: '7d'
                }, {
                    type: 'month',
                    count: 1,
                    text: '30d'
                }, {
                    type: 'all',
                    text: 'All'
                }]
            } : undefined,
            chart: {
                zooming: {
                    type: 'xy'
                }
            },
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.4
                }
            },
            legend: {
                enabled: selectedGroupBy !== 'none',
            },
            xAxis: {
                zoomEnabled: true,
                title: {
                    text: selectedAxis === 'date' ? t('html.label.time.date') : axisName + ' (' + windowName + ')'
                }
            },
            yAxis: {
                zoomEnabled: true,
                title: {text: unitLabel},
                max: selectedYAxis === 'percentage' ? 100 : undefined
            },
            tooltip: selectedAxis === 'date' ? {enabled: true} : {
                enabled: true,
                headerFormat: '{point.x} ' + windowName + '<br>',
                pointFormat: (selectedGroupBy !== 'none' ? '{series.name} - ' : '') + '<b>{point.y} ' + (selectedYAxis === 'percentage' ? '%' : t('html.label.players')) + '</b>'
            },
            series: series
        })
    }, [t, nightModeEnabled, series, selectedGroupBy, axisOptions, selectedAxis, windowOptions, selectedWindow, selectedYAxis]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;
    return (
        <Card>
            <CardHeader icon={faUsersViewfinder} color={'light-blue'} label={t('html.label.playerRetention')}/>
            <ExtendableCardBody id={'card-body-' + (identifier ? 'server-' : 'network-') + 'player-retention'}>
                <Row>
                    <Col>
                        <label>{t('html.label.retention.timeStep')}</label>
                        <BasicDropdown selected={selectedWindow} options={windowOptions} onChange={setSelectedWindow}/>
                    </Col>
                    <Col>
                        <label>{t('html.label.retention.playersRegisteredInTime')}</label>
                        <BasicDropdown selected={selectedGroup} options={groupOptions} onChange={setSelectedGroup}/>
                    </Col>
                    <Col>
                        <label>{t('html.label.retention.groupByTime')}</label>
                        <BasicDropdown selected={selectedGroupBy} options={groupByOptions}
                                       onChange={setSelectedGroupBy}/>
                    </Col>
                    <Col>
                        <label>{t('html.label.xAxis')}</label>
                        <BasicDropdown selected={selectedAxis} options={axisOptions} onChange={setSelectedAxis}/>
                    </Col>
                    <Col>
                        <label>{t('html.label.yAxis')}</label>
                        <BasicDropdown selected={selectedYAxis} options={yAxisOptions} onChange={setSelectedYAxis}/>
                    </Col>
                </Row>
                <hr/>
                {selectedAxis !== 'date' &&
                    <FunctionPlotGraph id={'retention-graph'} options={graphOptions} tall/>}
                {selectedAxis === 'date' &&
                    <LineGraph id={'retention-graph'} options={graphOptions} tall/>}
            </ExtendableCardBody>
        </Card>
    )
};

export default PlayerRetentionGraphCard