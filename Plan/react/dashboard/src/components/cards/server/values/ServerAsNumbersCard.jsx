import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBookOpen, faChartLine, faCrosshairs, faSkull, faUser, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import {CardLoader} from "../../../navigation/Loader.tsx";
import ExtendableCardBody from "../../../layout/extension/ExtendableCardBody.tsx";
import {useMetadata} from "../../../../hooks/metadataHook.tsx";
import CurrentUptime from "../../../datapoint/CurrentUptime.tsx";
import {GenericFilterContextProvider} from "../../../../dataHooks/genericFilterContextHook.tsx";
import {useParams} from "react-router";
import {QueryDatapoint} from "../../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint.ts";
import {MS_24H} from "../../../../util/format/useDateFormatter.js";

const ServerAsNumbersCard = () => {
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();
    const {identifier} = useParams();

    if (!networkMetadata) return <CardLoader/>;

    const isGameServer = !!identifier;

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
                        id={isGameServer ? 'card-body-server-as-numbers' : 'card-body-network-as-numbers'}>
                        <CurrentUptime filter={filter}/>
                        <hr/>
                        <QueryDatapoint name={t('html.label.totalPlayers')}
                                        color={'players-count'} icon={faUsers}
                                        dataType={DatapointType.NEW_PLAYERS}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.regularPlayers')}
                                        color={'players-regular'} icon={faUsers}
                                        dataType={DatapointType.REGULAR_PLAYERS}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.playersOnline')}
                                        color={'players-online'} icon={faUser}
                                        dataType={DatapointType.PLAYERS_ONLINE} permission="players.online.current"
                                        filter={filter} bold/>
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
                        <QueryDatapoint name={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                                        color={'playtime'} icon={faClock}
                                        dataType={DatapointType.PLAYTIME_PER_PLAYER_AVERAGE}
                                        filter={filter}/>
                        <QueryDatapoint name={t('html.label.averageSessionLength')}
                                        color={'sessions'} icon={faClock}
                                        dataType={DatapointType.SESSION_LENGTH_AVERAGE}
                                        filter={filter}/>
                        <QueryDatapoint name={t('html.label.sessions')}
                                        color={'sessions'} icon={faCalendarCheck} bold
                                        dataType={DatapointType.SESSION_COUNT}
                                        filter={filter}/>
                        <hr/>
                        <QueryDatapoint name={t('html.label.playerKills')}
                                        color={'player-kills'} icon={faCrosshairs}
                                        dataType={DatapointType.PLAYER_KILLS}
                                        filter={filter}/>
                        <QueryDatapoint name={t('html.label.mobKills')}
                                        color={'mob-kills'} icon={faCrosshairs}
                                        dataType={DatapointType.MOB_KILLS}
                                        filter={filter}/>
                        <QueryDatapoint name={t('html.label.deaths')}
                                        color={'deaths'} icon={faSkull}
                                        dataType={DatapointType.DEATHS}
                                        filter={filter}/>
                    </ExtendableCardBody>
                </Card>
            )}
        </GenericFilterContextProvider>
    )
}

export default ServerAsNumbersCard;