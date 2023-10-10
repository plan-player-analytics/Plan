import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {Card} from "react-bootstrap";
import CardHeader from "../CardHeader";
import {faChevronLeft, faChevronRight, faHandHoldingHeart} from "@fortawesome/free-solid-svg-icons";
import {fetchFirstMoments} from "../../../service/serverService";
import {CardLoader} from "../../navigation/Loader";
import XRangeGraph from "../../graphs/XRangeGraph";
import {Link} from "react-router-dom";
import {tooltip} from "../../../util/graphs";
import {useTranslation} from "react-i18next";
import Graph from "../../graphs/Graph";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const FirstMomentsCard = ({identifier}) => {
    const {t} = useTranslation();

    const [data, setData] = useState(undefined);
    const [sessionPlots, setSessionPlots] = useState([]);

    const loadData = useCallback(async () => {
        const loaded = await fetchFirstMoments(0, 0, identifier);
        setData(loaded);
        const sessionsByPlayer = {};
        for (const session of loaded.sessions) {
            const player = session.player_name;
            if (!sessionsByPlayer[player]) sessionsByPlayer[player] = [];
            sessionsByPlayer[player].push(session);
        }
        const sessionPlots = [];
        let i = 1;
        for (const entry of Object.entries(sessionsByPlayer)) {
            sessionPlots.push({
                name: "Player " + i,
                uuid: entry[1][0].player_uuid,
                points: entry[1].map(session => {
                    const dayMs = 24 * 60 * 60 * 1000;
                    const addStart = Math.floor(Math.random() * dayMs);
                    const start = Date.now() - (Date.now() % dayMs) + addStart;
                    const end = start + Math.floor(Math.random() * (dayMs - addStart));
                    return {x: start, x2: end, color: session.first_session ? '#4caf50' : '#4ab4de'};
                }).sort((a, b) => a.x - b.x > 0 ? 1 : -1)
            })
            i++;
        }
        setSessionPlots(sessionPlots.sort((a, b) => a.points[0].x - b.points[0].x > 0 ? 1 : -1));
    }, [setData, setSessionPlots, identifier]);
    useEffect(() => {
        loadData()
    }, [loadData]);


    const playersOnlineOptions = useMemo(() => {
        if (!data || !sessionPlots) return {};

        const startOfDay = sessionPlots ? (sessionPlots[0].points[0].x - sessionPlots[0].points[0].x % (24 * 60 * 60 * 1000)) : 0;
        const endOfDay = startOfDay + (24 * 60 * 60 * 1000);
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
                data: data ? data.graphs[0].points : [],
                color: "#90b7f3",
                yAxis: 0
            }]
        }
    }, [data, t, sessionPlots]);

    if (!data) return <CardLoader/>

    return (
        <Card>
            <CardHeader icon={faHandHoldingHeart} color="light-green" label={"First moments"}>
                <div className={"float-end"}>
                    <span style={{marginRight: '0.5rem'}}>on 2023-04-10</span>
                    <button style={{marginRight: '0.5rem'}}><FontAwesomeIcon icon={faChevronLeft}/></button>
                    <button><FontAwesomeIcon icon={faChevronRight}/></button>
                </div>
            </CardHeader>
            {/*<ExtendableCardBody id={"card-body-first-moments"} style={{marginTop: "-0.5rem"}}>*/}
            {/*    <Filter index={0} filter={filter} setFilterOptions={setFilterOptions}/>*/}
            {/*</ExtendableCardBody>*/}
            <div style={{overflowY: "scroll", maxHeight: "700px"}}>
                <table className={"table table-striped"}>
                    <thead>
                    <tr style={{position: 'sticky', top: 0, backgroundColor: "white", zIndex: 1}}>
                        <th>Player</th>
                        <th>Sessions</th>
                        <th>Playtime</th>
                    </tr>
                    <tr style={{position: 'sticky', top: "3rem", backgroundColor: "white", zIndex: 1}}>
                        <td>Players Online</td>
                        <td>
                            <Graph id={"players-online-graph"} options={playersOnlineOptions} className={''}
                                   style={{height: "100px"}}/>
                        </td>
                        <td>-</td>
                    </tr>
                    </thead>
                    <tbody>
                    {sessionPlots.map((plot, i) => <tr key={plot.name}>
                        <td><Link to={`/player/${plot.uuid}`}>{plot.name}</Link></td>
                        <td style={{padding: 0}}><XRangeGraph id={'xrange-' + i} pointsByAxis={[plot]} height={"60px"}/>
                        </td>
                        <td>0s</td>
                    </tr>)}
                    </tbody>
                </table>
            </div>
        </Card>
    )
};

export default FirstMomentsCard