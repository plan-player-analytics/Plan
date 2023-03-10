import React, {useEffect, useState} from 'react';
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

const PlayerRetentionGraphCard = ({identifier}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();

    const dayMs = 24 * 3600000;
    const time = new Date().getTime();

    const {data, loadingError} = useDataRequest(fetchRetentionData, [identifier]);

    const [selectedWindow, setSelectedWindow] = useState('days');
    const windowOptions = [
        {name: 'hours', displayName: 'Hours', increment: 3600000},
        {name: 'days', displayName: 'Days', increment: dayMs},
        {name: 'weeks', displayName: 'Weeks', increment: 7 * dayMs},
        {name: 'months', displayName: 'Month', increment: 30 * dayMs},
    ];
    const [selectedGroup, setSelectedGroup] = useState('registered-7d');
    const groupOptions = [
        {name: 'registered-7d', displayName: 'in the last 7 days', start: time - 7 * dayMs},
        {name: 'registered-30d', displayName: 'in the last 30 days', start: time - 30 * dayMs},
        {name: 'registered-3m', displayName: 'in the last 3 months', start: time - 3 * 30 * dayMs},
        {name: 'registered-6m', displayName: 'in the last 6 months', start: time - 6 * 30 * dayMs},
        {name: 'registered-1y', displayName: 'in the last 12 months', start: time - 365 * dayMs},
        {name: 'registered-2y', displayName: 'in the last 24 months', start: time - 2 * 365 * dayMs},
        {name: 'registered-ever', displayName: 'any time', start: 0},
    ];
    const [selectedGroupBy, setSelectedGroupBy] = useState('none');
    const groupByOptions = [
        {name: 'none', displayName: 'No grouping'},
        {name: 'days', displayName: 'Day'},
        {name: 'weeks', displayName: 'Week'},
        {name: 'months', displayName: 'Month'},
        {name: 'years', displayName: 'Year'},
    ];
    const [selectedYAxis, setSelectedYAxis] = useState('percentage');
    const yAxisOptions = [
        {name: 'percentage', displayName: 'Percentage'},
        {name: 'count', displayName: 'Player Count'}
    ];
    const [selectedAxis, setSelectedAxis] = useState('time');
    const axisOptions = [
        {name: 'date', displayName: 'Date'},
        {name: 'time', displayName: 'Time since register date'},
        {name: 'playtime', displayName: 'Playtime'}
    ];

    const [series, setSeries] = useState([]);

    useEffect(() => {
        if (!data) return;

        Date.prototype.getWeek = function () {
            const onejan = new Date(this.getFullYear(), 0, 1);
            const today = new Date(this.getFullYear(), this.getMonth(), this.getDate());
            const dayOfYear = ((today - onejan + 86400000) / 86400000);
            return Math.ceil(dayOfYear / 7)
        };

        const start = groupOptions.find(option => option.name === selectedGroup).start;
        const increment = windowOptions.find(option => option.name === selectedWindow).increment;
        const filtered = data.player_retention.filter(point => point.registerDate > start)
            .sort((a, b) => a.registerDate - b.registerDate);

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
                    const week = date.getUTCFullYear() + '-week-' + date.getWeek();
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

        const mapToData = (dataToMap) => {
            const total = dataToMap.length;
            let seriesData = [];
            const xAxis = axisOptions.find(option => option.name === selectedAxis).name;
            switch (xAxis) {
                case 'date':
                    const retainedBasedOnDate = [];
                    const firstRegisterDate = dataToMap[0].registerDate - dataToMap[0].registerDate % dayMs;
                    for (let i = dataToMap[0].registerDate; i < time; i += increment) {
                        const retainedSince = dataToMap.filter(point => point.timeDifference > i - firstRegisterDate).length;
                        retainedBasedOnDate.push([i, selectedYAxis === 'percentage' ? retainedSince * 100.0 / total : retainedSince]);
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
        }

        let i = 0;
        const s = Object.entries(grouped).map(group => {
            const name = group[0];
            const groupData = group[1];
            const color = rgbToHexString(hsvToRgb(randomHSVColor(i)));
            i++;
            return {
                name: name,
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                data: mapToData(groupData),
                color: nightModeEnabled ? withReducedSaturation(color) : color
            };
        });
        console.log(s);
        setSeries(s)
    }, [data, selectedWindow, selectedGroup, selectedAxis, selectedGroupBy, selectedYAxis, setSeries])

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;
    return (
        <Card>
            <CardHeader icon={faUsersViewfinder} color={'light-blue'} label={t('html.label.playerRetention')}/>
            <ExtendableCardBody id={'card-body-' + (identifier ? 'server-' : 'network-') + 'player-retention'}>
                <Row>
                    <Col>
                        <label>Time step</label>
                        <BasicDropdown selected={selectedWindow} options={windowOptions} onChange={setSelectedWindow}/>
                    </Col>
                    <Col>
                        <label>Players who registered</label>
                        <BasicDropdown selected={selectedGroup} options={groupOptions} onChange={setSelectedGroup}/>
                    </Col>
                    <Col>
                        <label>Group registered by</label>
                        <BasicDropdown selected={selectedGroupBy} options={groupByOptions}
                                       onChange={setSelectedGroupBy}/>
                    </Col>
                    <Col>
                        <label>X Axis</label>
                        <BasicDropdown selected={selectedAxis} options={axisOptions} onChange={setSelectedAxis}/>
                    </Col>
                    <Col>
                        <label>Y Axis</label>
                        <BasicDropdown selected={selectedYAxis} options={yAxisOptions} onChange={setSelectedYAxis}/>
                    </Col>
                </Row>
                <hr/>
                {selectedAxis !== 'date' &&
                    <FunctionPlotGraph id={'retention-graph'} series={series} alreadyOffsetTimezone={true}
                                       legendEnabled={true} tall/>}
                {selectedAxis === 'date' &&
                    <LineGraph id={'retention-graph'} series={series} alreadyOffsetTimezone={true} selectedRange={5}
                               legendEnabled={true} tall/>}
            </ExtendableCardBody>
        </Card>
    )
};

export default PlayerRetentionGraphCard