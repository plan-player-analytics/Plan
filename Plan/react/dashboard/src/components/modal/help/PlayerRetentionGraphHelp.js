import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {useTranslation} from "react-i18next";
import {faChartArea, faGears} from "@fortawesome/free-solid-svg-icons";
import CardTabs from "../../CardTabs"
import {BasicDropdown} from "../../input/BasicDropdown";
import RangeSlider from "react-bootstrap-range-slider";
import {tooltip} from "../../../util/graphs";
import {hsvToRgb, randomHSVColor, rgbToHexString} from "../../../util/colors";
import FunctionPlotGraph from "../../graphs/FunctionPlotGraph";
import LineGraph from "../../graphs/LineGraph";

const PlayerRetentionGraphHelp = () => {
    const {t} = useTranslation();

    const [selectedAxis, setSelectedAxis] = useState('time');
    const axisOptions = useMemo(() => [
        {name: 'time', displayName: t('html.label.retention.timeSinceRegistered')},
        {name: 'playtime', displayName: t('html.label.playtime')},
        {name: 'date', displayName: t('html.label.time.date')},
        {name: 'deltas', displayName: t('html.label.time.date') + ' > ' + t('html.label.registered')},
    ], [t]);

    const [x, setX] = useState(0);
    const updateX = useCallback(event => setX(event.target.value), [setX]);
    useEffect(() => {
        setX(0);
    }, [selectedAxis, setX]);

    const data = useMemo(() => [
        {x: 1},
        {x: 2},
        {x: 9},
        {x: 24},
    ], []);
    const [series, setSeries] = useState([]);
    const [graphOptions, setGraphOptions] = useState({title: {text: ''},});
    useEffect(() => {
        const d = []
        for (let i = 0; i < x; i++) {
            d.push([i, data.filter(item => item.x > i).length * 100.0 / data.length]);
        }
        for (let i = x; i < 30; i++) {
            d.push([i, null]);
        }
        const color = rgbToHexString(hsvToRgb(randomHSVColor(1)));
        setSeries([{
            name: 'name',
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: d,
            color
        }]);
    }, [x, data, setSeries]);
    useEffect(() => {
        const unitLabel = 'Retained Players %';
        const windowName = t('html.label.time.hours');
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
            legend: {
                enabled: false,
            },
            plotOptions: {
                series: {animation: false}
            },
            xAxis: {
                zoomEnabled: true,
                title: {
                    text: selectedAxis === 'date' || selectedAxis === 'deltas' ? t('html.label.time.date') : axisName + ' (' + windowName + ')'
                }
            },
            yAxis: {
                zoomEnabled: true,
                title: {text: unitLabel},
                max: 100,
                min: 0
            },
            tooltip: selectedAxis === 'date' || selectedAxis === 'deltas' ? {
                enabled: true,
                valueDecimals: 2,
                pointFormat: '<b>{point.y} %</b>'
            } : {
                enabled: true,
                valueDecimals: 2,
                headerFormat: '{point.x} ' + windowName + '<br>',
                pointFormat: '<b>{point.y} %</b>'
            },
            series: series
        })
    }, [t, series, axisOptions, selectedAxis]);


    const disabledColor = 'rgba(0, 0, 0, 0.05)';
    return (
        <>
            <CardTabs tabs={[
                {
                    name: "Using the graph", icon: faGears, color: 'indigo', href: 'data-explanation',
                    element: <div className={'mt-2'}>
                        <p>Select the options to analyze different aspects of Player Retention.</p>
                        <h4>Tips</h4>
                        <ul>
                            <li>You can compare different months by changing the
                                '{t('html.label.retention.groupByTime')}' option to '{t('html.label.time.month')}'.
                            </li>
                            <li>Grouping by join address allows measuring advertising campaigns on different sites.</li>
                            <li>You can Zoom in by click + dragging on the graph.</li>
                            <li>You can hide/show a group by clicking on the label at the bottom.</li>
                        </ul>
                        <hr/>
                        <h3>How it is calculated</h3>
                        <p>The graph is generated from player data:</p>
                        <pre>
                            {'{\n    playerUUID,\n    registerDate,\n    lastSeenDate,\n    timeDifference = lastSeenDate - registerDate,\n    playtime\n    joinAddress\n}'}
                        </pre>
                        <ol>
                            <li>First the data is filtered using '{t('html.label.retention.timeSinceRegistered')}'
                                option. Any players with 'registerDate' outside the time range are ignored.
                            </li>
                            <li>Then it is grouped into groups of players using '{t('html.label.retention.groupByTime')}'
                                option, eg. With '{t('html.label.time.month')}': "All players who registered in January
                                2023, February 2023, etc"
                            </li>
                            <li>Then the '{t('html.label.xAxis')}' and '{t('html.label.yAxis')}' options select which
                                visualization to render.
                            </li>
                            <li>'{t('html.label.retention.timeStep')}' controls how many points the graph has, eg.
                                'Days' has one point per day.
                            </li>
                            <li>
                                <p className={'m-0'}>On each calculated point all players are checked for the condition.
                                    Select X Axis below to see conditions.</p>
                                <label>{t('html.label.xAxis')}</label>
                                <BasicDropdown selected={selectedAxis} options={axisOptions}
                                               onChange={setSelectedAxis}/>
                                <div className={'mt-2'}>
                                    <p>{selectedAxis === 'time' && <>
                                        This visualization tells how long people keep coming back to play on the server
                                        after they join the first time.
                                        The visualization uses timeDifference. If x &lt; timeDifference, the player is
                                        visible on the graph.
                                    </>}
                                        {selectedAxis === 'playtime' && <>
                                            This visualization tells how long the gameplay loop keeps players engaged on
                                            your server. The visualization uses playtime. If x &lt; playtime, the player
                                            is visible on the graph.
                                        </>}
                                        {selectedAxis === 'date' && <>
                                            This visualization shows the different groups of players that are still
                                            playing on your server. The visualization uses lastSeen date. If
                                            x &lt; lastSeenDate, the player is visible on the graph.
                                        </>}
                                        {selectedAxis === 'deltas' && <>
                                            This visualization is most effective using Player Count as the Y Axis. The
                                            visualization shows net gain of players (How many players joined minus
                                            players who stopped playing). The visualization uses both registered and
                                            lastSeen dates. If registerDate &lt; x &lt; lastSeenDate, the player is
                                            visible on the graph.
                                        </>}</p>
                                    {selectedAxis !== 'date' && selectedAxis !== 'deltas' && <>
                                        <h4>{t('html.label.help.testPrompt')}</h4>
                                        <table className={"table"}>
                                            <thead>
                                            <tr>
                                                <th>{t('html.label.player')}</th>
                                                <th>{axisOptions.find(option => option.name === selectedAxis).displayName}</th>
                                                <th>{t('html.label.playerRetention')}</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr style={x <= 24 ? {} : {backgroundColor: disabledColor}}>
                                                <td>Pooh</td>
                                                <td>1d 53s</td>
                                                <th>{x <= 24 ? t('plugin.generic.yes') : t('plugin.generic.no')}</th>
                                            </tr>
                                            <tr style={x <= 9 ? {} : {backgroundColor: disabledColor}}>
                                                <td>Piglet</td>
                                                <td>9h 12min</td>
                                                <th>{x <= 9 ? t('plugin.generic.yes') : t('plugin.generic.no')}</th>
                                            </tr>
                                            <tr style={x <= 2 ? {} : {backgroundColor: disabledColor}}>
                                                <td>Rabbit</td>
                                                <td>2h</td>
                                                <th>{x <= 2 ? t('plugin.generic.yes') : t('plugin.generic.no')}</th>
                                            </tr>
                                            <tr style={x <= 1 ? {} : {backgroundColor: disabledColor}}>
                                                <td>Tiger</td>
                                                <td>1h 59min 59s</td>
                                                <th>{x <= 1 ? t('plugin.generic.yes') : t('plugin.generic.no')}</th>
                                            </tr>
                                            </tbody>
                                            <tfoot>
                                            <tr>
                                                <th>Retention</th>
                                                <th></th>
                                                {x <= 1 && <td>100%</td>}
                                                {1 < x && x <= 2 && <td>75%</td>}
                                                {2 < x && x <= 9 && <td>50%</td>}
                                                {9 < x && x <= 24 && <td>25%</td>}
                                                {x > 24 && <td>0%</td>}
                                            </tr>
                                            </tfoot>
                                        </table>
                                        <label>x = {x} {t('html.label.time.hours')}</label>
                                        <RangeSlider
                                            value={x}
                                            onChange={updateX}
                                            min={0}
                                            max={25}
                                            tooltip={'off'}/>
                                        {(selectedAxis !== 'date' && selectedAxis !== 'deltas') &&
                                            <FunctionPlotGraph id={'retention-help-graph'} options={graphOptions}/>}
                                        {(selectedAxis === 'date' || selectedAxis === 'deltas') &&
                                            <LineGraph id={'retention-help-graph'} options={graphOptions}/>}
                                    </>}
                                </div>
                            </li>
                        </ol>

                    </div>
                }, {
                    name: "Examples",
                    icon: faChartArea,
                    color: 'indigo',
                    href: 'interesting-combinations',
                    element: <div className={'mt-2'}>
                        <label>{t('html.label.retention.timeSinceRegistered')}</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086629-69e70c66-69d5-4a08-afbc-c63b218ec9bc.png'}/>
                        <hr/>
                        <label>Playtime tells how long the gameplay loop keeps players engaged on your server.</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086773-ae5646e5-0d9e-4016-9f1d-c392d3d25c07.png'}/>
                        <hr/>
                        <label>{t('html.label.time.date')}</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086880-c6e88e9a-125d-4513-b86a-ca61b4d752b2.png'}/>
                        <hr/>
                        <label>{t('html.label.time.date')} &gt; {t('html.label.registered')} shows net gain of
                            players.</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087066-0cacc7e4-aacc-48ff-97d7-ba2cf6a368ff.png'}/>
                        <hr/>
                        <label>A general pattern emerges when all players start leaving the server at the same
                            time</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087273-04331324-6bc3-4efb-8864-166b5b3d4a89.png'}/>
                        <hr/>
                        <label>Comparing player gain of different months. Plateaus suggest there were players Plan
                            doesn't know about. In this example Plan was installed in January 2022.</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087828-8db2da1a-578d-43fc-abc5-2aa09e97935e.png'}/>
                        <hr/>
                        <label>Comparing player gain of different ad campaigns using different Join Addresses
                            (anonymized)</label>
                        <img className={'w-100'} alt={'Graph'} loading={'lazy'}
                             src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225088901-2e30caf6-f141-4998-91de-2034fda5b7e9.png'}/>
                    </div>
                }
            ]}/>
        </>
    )
};

export default PlayerRetentionGraphHelp