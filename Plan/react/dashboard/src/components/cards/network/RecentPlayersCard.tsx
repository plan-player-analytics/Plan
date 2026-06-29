import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import ExtendableCardBody from "../../layout/extension/ExtendableCardBody";
import {faUsers} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../../util/format/useDateFormatter";
import {GenericFilterContextProvider} from "../../../dataHooks/genericFilterContextHook";
import {QueryDatapoint} from "../../datapoint/QueryDatapoint";
import {DatapointType} from "../../../dataHooks/model/datapoint/Datapoint";


export const RecentPlayersCard = () => {
    const {t} = useTranslation();

    return (
        <Card id={"recent-players"}>
            <Card.Header>
                <h6 className="col-text">
                    {t('html.label.players')}
                </h6>
            </Card.Header>
            <ExtendableCardBody id={'card-body-network-overview-players'}>
                <GenericFilterContextProvider initialValue={{afterMillisAgo: MS_24H}}>
                    {filter => (
                        <>
                            <p>{t('html.label.last24hours')}</p>
                            <QueryDatapoint icon={faUsers} color="players-unique"
                                            name={t('html.label.uniquePlayers')}
                                            dataType={DatapointType.UNIQUE_PLAYERS_COUNT}
                                            filter={filter}/>
                            <QueryDatapoint icon={faUsers} color="players-new"
                                            name={t('html.label.newPlayers')}
                                            dataType={DatapointType.NEW_PLAYERS}
                                            filter={filter}/>
                        </>
                    )}
                </GenericFilterContextProvider>
                <GenericFilterContextProvider initialValue={{afterMillisAgo: MS_WEEK}}>
                    {filter => (
                        <>
                            <p>{t('html.label.last7days')}</p>
                            <QueryDatapoint icon={faUsers} color="players-unique"
                                            name={t('html.label.uniquePlayers')}
                                            dataType={DatapointType.UNIQUE_PLAYERS_COUNT}
                                            filter={filter}/>
                            <QueryDatapoint icon={faUsers} color="players-new"
                                            name={t('html.label.newPlayers')}
                                            dataType={DatapointType.NEW_PLAYERS}
                                            filter={filter}/>
                        </>
                    )}
                </GenericFilterContextProvider>
                <GenericFilterContextProvider initialValue={{afterMillisAgo: MS_MONTH}}>
                    {filter => (
                        <>
                            <p>{t('html.label.last30days')}</p>
                            <QueryDatapoint icon={faUsers} color="players-unique"
                                            name={t('html.label.uniquePlayers')}
                                            dataType={DatapointType.UNIQUE_PLAYERS_COUNT}
                                            filter={filter}/>
                            <QueryDatapoint icon={faUsers} color="players-new"
                                            name={t('html.label.newPlayers')}
                                            dataType={DatapointType.NEW_PLAYERS}
                                            filter={filter}/>
                        </>
                    )}
                </GenericFilterContextProvider>
            </ExtendableCardBody>
        </Card>
    )
}