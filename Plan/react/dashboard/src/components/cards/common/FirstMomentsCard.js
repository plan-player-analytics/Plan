import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {Card} from "react-bootstrap";
import CardHeader from "../CardHeader";
import {faChevronLeft, faChevronRight, faHandHoldingHeart} from "@fortawesome/free-solid-svg-icons";
import {fetchFirstMoments, fetchPlayersOnlineGraph} from "../../../service/serverService";
import {CardLoader} from "../../navigation/Loader";
import XRangeGraph from "../../graphs/XRangeGraph";
import {Link} from "react-router-dom";
import {tooltip} from "../../../util/graphs";
import {useTranslation} from "react-i18next";
import Graph from "../../graphs/Graph";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {useMetadata} from "../../../hooks/metadataHook";
import {ErrorViewBody} from "../../../views/ErrorView";
import FormattedTime from "../../text/FormattedTime";

const dayMs = 24 * 60 * 60 * 1000;

const FirstMomentsCard = ({identifier}) => {
    const {t} = useTranslation();
    const {timeZoneOffsetMinutes, networkMetadata} = useMetadata();

    const [selectedDay, setSelectedDay] = useState(1648760400000); //Date.now())

    const {data: playersOnline, loadingError} = useDataRequest(fetchPlayersOnlineGraph, [identifier]);
    const [sessionPlots, setSessionPlots] = useState([]);

    const loadData = useCallback(async () => {
        const startOfDay = selectedDay - (selectedDay + timeZoneOffsetMinutes * 60 * 1000) % dayMs;
        const endOfDay = startOfDay + dayMs;
        const {
            data: loaded,
            error
        } = await fetchFirstMoments(startOfDay, endOfDay, networkMetadata?.servers.find(s => s.serverUUID === identifier));
        console.log(loaded);
        const sessionsByPlayer = {};
        if (loaded?.data) {
            for (const session of loaded.data.sessionList) {
                const player = session.player_name;
                if (!sessionsByPlayer[player]) sessionsByPlayer[player] = [];
                sessionsByPlayer[player].push(session);
            }
        }
        const sessionPlots = [];
        let i = 1;
        for (const entry of Object.entries(sessionsByPlayer)) {
            sessionPlots.push({
                name: entry[1][0].player_name,
                uuid: entry[1][0].player_uuid,
                points: entry[1].map(session => {
                    const start = session.startMillis;
                    const end = session.endMillis;
                    return {x: start, x2: end, color: session.first_session ? '#4caf50' : '#4ab4de'};
                }).sort((a, b) => a.x - b.x > 0 ? 1 : -1),
                playtime: entry[1].reduce((partialSum, session) => partialSum + session.endMillis - session.startMillis, 0)
            })
            i++;
        }
        setSessionPlots(sessionPlots.sort((a, b) => a.points[0].x - b.points[0].x > 0 ? 1 : -1));
    }, [selectedDay, setSessionPlots, identifier]);
    useEffect(() => {
        loadData()
    }, [loadData]);


    const playersOnlineOptions = useMemo(() => {
        if (!playersOnline || !sessionPlots?.length) return {};

        const startOfDay = selectedDay + timeZoneOffsetMinutes;
        const endOfDay = startOfDay + dayMs;
        return {
            yAxis: {
                title: {
                    text: ''
                },
                opposite: true,
                softMax: 1,
                softMin: 0
            },
            xAxis: {
                visible: false,
                type: 'datetime',
                min: startOfDay,
                max: endOfDay
            },
            title: {text: ''},
            legend: {
                enabled: false
            },
            time: {
                timezoneOffset: 0
            },
            series: [{
                name: t('html.label.playersOnline'),
                type: 'spline',
                tooltip: tooltip.zeroDecimals,
                data: playersOnline ? playersOnline.playersOnline : [],
                color: "#90b7f3",
                yAxis: 0
            }]
        }
    }, [playersOnline, t, sessionPlots]);

    if (!sessionPlots) return <CardLoader/>;

    return (
        <Card>
            <CardHeader icon={faHandHoldingHeart} color="light-green" label={"First moments"}>
                <div className={"float-end"}>
                    <span style={{marginRight: '0.5rem'}}>on {new Date(selectedDay).toISOString().split("T")[0]}</span>
                    <button style={{marginRight: '0.5rem'}} onClick={() => setSelectedDay(selectedDay - dayMs)}>
                        <FontAwesomeIcon icon={faChevronLeft}/></button>
                    <button onClick={() => setSelectedDay(selectedDay + dayMs)}>
                        <FontAwesomeIcon icon={faChevronRight}/>
                    </button>
                </div>
            </CardHeader>
            {/*<ExtendableCardBody id={"card-body-first-moments"} style={{marginTop: "-0.5rem"}}>*/}
            {/*    <Filter index={0} filter={filter} setFilterOptions={setFilterOptions}/>*/}
            {/*</ExtendableCardBody>*/}
            <div style={{overflowY: "scroll", maxHeight: "700px"}}>
                <table className={"table table-striped"}>
                    <thead>
                    <tr style={{position: 'sticky', top: 0, backgroundColor: "white", zIndex: 1}}>
                        <td>Players Online</td>
                        <td>
                            {loadingError && <ErrorViewBody error={loadingError}/>}
                            {!loadingError &&
                                <Graph id={"players-online-graph"} options={playersOnlineOptions} className={''}
                                       style={{height: "100px"}}/>}
                        </td>
                        <td>-</td>
                    </tr>
                    <tr style={{position: 'sticky', top: "7.8rem", backgroundColor: "white", zIndex: 1}}>
                        <th>Player</th>
                        <th>Sessions</th>
                        <th>Playtime</th>
                    </tr>
                    </thead>
                    <tbody>
                    {sessionPlots.map((plot, i) => <tr key={plot.name}>
                        <td><Link to={`/player/${plot.uuid}`}>{plot.name}</Link></td>
                        <td style={{padding: 0}}><XRangeGraph id={`xrange-${plot.uuid}`} pointsByAxis={[plot]}
                                                              height={"60px"}/>
                        </td>
                        <td><FormattedTime timeMs={plot.playtime}/></td>
                    </tr>)}
                    </tbody>
                </table>
            </div>
        </Card>
    )
};

export default FirstMomentsCard