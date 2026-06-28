import React from 'react';
import {Card} from "react-bootstrap";
import CardHeader from "../CardHeader.tsx";
import {
    faBookOpen,
    faChartLine,
    faExclamationCircle,
    faPowerOff,
    faTachometerAlt,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import CurrentUptime from "../../datapoint/CurrentUptime.tsx";
import {QueryDatapoint} from "../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../dataHooks/model/datapoint/Datapoint.ts";
import {MS_24H, MS_WEEK} from "../../../util/format/useDateFormatter.js";
import {GenericFilterContextProvider} from "../../../dataHooks/genericFilterContextHook.tsx";

const QuickViewDataCard = ({server}) => {
    const {t} = useTranslation()

    const filter = {server: server.serverUUID}

    return (
        <Card>
            <CardHeader icon={faBookOpen} color={'servers'}
                        label={server.serverName + ' ' + t('html.label.asNumbers')}/>
            <Card.Body>
                <CurrentUptime filter={filter}/>
                <QueryDatapoint
                    name={t('html.label.lastPeak')}
                    color={'player-peak-last'} icon={faChartLine}
                    valueLabel={t('html.unit.players')} bold
                    dataType={DatapointType.PLAYERS_ONLINE_PEAK}
                    filter={{...filter, afterMillisAgo: MS_24H * 2}}/>
                <QueryDatapoint
                    name={t('html.label.bestPeak')}
                    color={'player-peak-all-time'} icon={faChartLine}
                    valueLabel={t('html.unit.players')} bold
                    dataType={DatapointType.PLAYERS_ONLINE_PEAK}
                    filter={filter}/>
                <hr/>
                <p><b>{t('html.label.last7days')}</b></p>
                <GenericFilterContextProvider initialValue={{...filter, afterMillisAgo: MS_WEEK}}>
                    {filter => (
                        <>
                            <QueryDatapoint name={t('html.label.uniquePlayers')}
                                            color={'players-unique'} icon={faUsers}
                                            dataType={DatapointType.UNIQUE_PLAYERS_COUNT}
                                            filter={filter} bold/>
                            <QueryDatapoint name={t('html.label.newPlayers')}
                                            color={'players-new'} icon={faUsers}
                                            dataType={DatapointType.NEW_PLAYERS}
                                            filter={filter} bold/>
                            <QueryDatapoint name={t('html.label.averageTps')}
                                            color={'tps-average'} icon={faTachometerAlt}
                                            dataType={DatapointType.TPS_AVERAGE}
                                            filter={filter} bold/>
                            <QueryDatapoint name={t('html.label.lowTpsSpikes')}
                                            color={'tps-low-spikes'} icon={faExclamationCircle}
                                            dataType={DatapointType.TPS_LOW_SPIKES}
                                            filter={filter} bold/>
                            <QueryDatapoint name={t('html.label.downtime')}
                                            color={'downtime'} icon={faPowerOff}
                                            dataType={DatapointType.DOWNTIME}
                                            filter={filter}/>
                        </>
                    )}
                </GenericFilterContextProvider>
            </Card.Body>
        </Card>
    )
};

export default QuickViewDataCard