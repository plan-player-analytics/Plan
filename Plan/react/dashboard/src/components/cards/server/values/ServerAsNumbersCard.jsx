import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBookOpen, faChartLine, faCrosshairs, faSkull, faUser, faUsers} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../../../datapoint/Datapoint.tsx";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import {CardLoader} from "../../../navigation/Loader.tsx";
import ExtendableCardBody from "../../../layout/extension/ExtendableCardBody.tsx";
import {useMetadata} from "../../../../hooks/metadataHook.tsx";
import CurrentUptime from "../../../datapoint/CurrentUptime";
import FormattedTime from "../../../text/FormattedTime.jsx";
import {GenericFilterContextProvider} from "../../../../dataHooks/genericFilterContextHook.tsx";
import {useParams} from "react-router";
import {QueryDatapoint} from "../../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint.ts";
import {MS_24H} from "../../../../util/format/useDateFormatter.js";

const ServerAsNumbersCard = ({data}) => {
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();
    const {identifier} = useParams();

    if (!data || !networkMetadata) return <CardLoader/>;

    const isGameServer = data.player_kills !== undefined;

    return (
        <GenericFilterContextProvider initialValue={{server: identifier}}>
            {filter => (
                <Card id={isGameServer ? "server-as-numbers" : "network-as-numbers"}>
                    <Card.Header>
                        <h6 className="col-text">
                            <Fa icon={faBookOpen}/> {isGameServer ? t('html.label.serverAsNumbers') : t('html.label.networkAsNumbers')}
                        </h6>
                    </Card.Header>
                    <ExtendableCardBody
                        id={data.player_kills === undefined ? 'card-body-network-as-numbers' : 'card-body-server-as-numbers'}>
                        <CurrentUptime uptime={data.current_uptime}/>
                        <hr/>
                        <QueryDatapoint name={t('html.label.totalPlayers')}
                                        color={'players-count'} icon={faUsers}
                                        dataType={DatapointType.NEW_PLAYERS}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.regularPlayers')}
                                        color={'players-regular'} icon={faUsers}
                                        dataType={DatapointType.REGULAR_PLAYERS}
                                        filter={filter} bold/>
                        <Datapoint name={t('html.label.playersOnline')}
                                   color={'players-online'} icon={faUser}
                                   value={data.online_players} bold/>
                        <hr/>
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
                        <QueryDatapoint name={t('html.label.totalPlaytime')}
                                        color={'playtime'} icon={faClock}
                                        dataType={DatapointType.PLAYTIME}
                                        filter={filter}/>
                        <Datapoint name={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                                   color={'playtime'} icon={faClock}
                                   value={<FormattedTime timeMs={data.player_playtime}/>}/>
                        {data.session_length_avg && <Datapoint name={t('html.label.averageSessionLength')}
                                                               color={'sessions'} icon={faClock}
                                                               value={<FormattedTime
                                                                   timeMs={data.session_length_avg}/>}/>}
                        <QueryDatapoint name={t('html.label.sessions')}
                                        color={'sessions'} icon={faCalendarCheck} bold
                                        dataType={DatapointType.SESSION_COUNT}
                                        filter={filter}/>
                        {data.player_kills !== undefined && <hr/>}
                        <Datapoint name={t('html.label.playerKills')}
                                   color={'player-kills'} icon={faCrosshairs}
                                   value={data.player_kills} bold/>
                        <Datapoint name={t('html.label.mobKills')}
                                   color={'mob-kills'} icon={faCrosshairs}
                                   value={data.mob_kills} bold/>
                        <Datapoint name={t('html.label.deaths')}
                                   color={'deaths'} icon={faSkull}
                                   value={data.deaths} bold/>
                    </ExtendableCardBody>
                </Card>
            )}
        </GenericFilterContextProvider>
    )
}

export default ServerAsNumbersCard;