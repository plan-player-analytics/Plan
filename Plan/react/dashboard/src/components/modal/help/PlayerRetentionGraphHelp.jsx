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
        const unitLabel = t('html.label.retention.retainedPlayersPercentage');
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
        <CardTabs tabs={[
            {
                name: t('html.label.help.usingTheGraph'), icon: faGears, color: 'retention', href: 'data-explanation',
                element: <div className={'mt-2'}>
                    <p>{t('html.label.help.retention.options')}</p>
                    <h4>{t('html.label.help.tips')}</h4>
                    <ul>
                        <li>{t('html.label.help.retention.compareMonths')
                            .replace('<0>', t('html.label.retention.groupByTime'))
                            .replace('<1>', t('html.label.time.month'))}
                        </li>
                        <li>{t('html.label.help.retention.compareJoinAddress')}</li>
                        <li>{t('html.label.help.graph.zoom')}</li>
                        <li>{t('html.label.help.graph.labels')}</li>
                    </ul>
                    <hr/>
                    <h3>{t('html.label.help.retention.howIsItCalculated')}</h3>
                    <p>{t('html.label.help.retention.howIsItCalculatedData')}</p>
                    <pre>
                            {'{\n    playerUUID,\n    registerDate,\n    lastSeenDate,\n    timeDifference = lastSeenDate - registerDate,\n    playtime\n    joinAddress\n}'}
                        </pre>
                    <ol>
                        <li>{t('html.label.help.retention.calculationStep1')
                            .replace('<>', t('html.label.retention.timeSinceRegistered'))}
                        </li>
                        <li>{t('html.label.help.retention.calculationStep2')
                            .replace('<0>', t('html.label.retention.groupByTime'))
                            .replace('<1>', t('html.label.time.month'))}
                        </li>
                        <li>{t('html.label.help.retention.calculationStep3')
                            .replace('<0>', t('html.label.xAxis'))
                            .replace('<1>', t('html.label.yAxis'))}
                        </li>
                        <li>{t('html.label.help.retention.calculationStep4')
                            .replace('<>', t('html.label.retention.timeStep'))}
                        </li>
                        <li>
                            <p className={'m-0'}>{t('html.label.help.retention.calculationStep5')}
                                {t('html.label.help.retention.calculationStep6')}</p>
                            <label>{t('html.label.xAxis')}</label>
                            <BasicDropdown selected={selectedAxis} options={axisOptions}
                                           onChange={setSelectedAxis}/>
                            <div className={'mt-2'}>
                                <p>
                                    {selectedAxis === 'time' && <>
                                        {t('html.label.help.retention.calculationStepTime')}
                                    </>}
                                    {selectedAxis === 'playtime' && <>
                                        {t('html.label.help.retention.calculationStepPlaytime')}
                                    </>}
                                    {selectedAxis === 'date' && <>
                                        {t('html.label.help.retention.calculationStepDate')}
                                    </>}
                                    {selectedAxis === 'deltas' && <>
                                        {t('html.label.help.retention.calculationStepDeltas')}
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
                                            <td>Tigger</td>
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
                name: t('html.label.help.examples'),
                icon: faChartArea,
                color: 'retention',
                href: 'interesting-combinations',
                element: <div className={'mt-2'}>
                    <label>{t('html.label.retention.timeSinceRegistered')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086629-69e70c66-69d5-4a08-afbc-c63b218ec9bc.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.playtime')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086773-ae5646e5-0d9e-4016-9f1d-c392d3d25c07.png'}/>
                    <hr/>
                    <label>{t('html.label.time.date')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225086880-c6e88e9a-125d-4513-b86a-ca61b4d752b2.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.deltas')
                        .replace('<>', t('html.label.time.date') + ' > ' + t('html.label.registered'))}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087066-0cacc7e4-aacc-48ff-97d7-ba2cf6a368ff.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.pattern')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087273-04331324-6bc3-4efb-8864-166b5b3d4a89.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.plateau')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225087828-8db2da1a-578d-43fc-abc5-2aa09e97935e.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.adCampaign')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225088901-2e30caf6-f141-4998-91de-2034fda5b7e9.png'}/>
                    <hr/>
                    <label>{t('html.label.help.retention.examples.stack')}</label>
                    <img className={'w-100'} alt={t('html.label.help.graph.title')} loading={'lazy'}
                         src={'https://raw.githubusercontent.com/plan-player-analytics/drawio-diagrams-storage/master/image/screenshot/225722723-cde69a1a-09fd-4e19-a8fe-993d60435652.png'}/>
                </div>
            }
        ]}/>
    )
};

export default PlayerRetentionGraphHelp