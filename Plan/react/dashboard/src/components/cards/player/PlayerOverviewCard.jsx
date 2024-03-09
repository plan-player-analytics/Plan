import React, {useCallback} from 'react';
import {useTranslation} from "react-i18next";
import {useMetadata} from "../../../hooks/metadataHook";
import {useNavigation} from "../../../hooks/navigationHook";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {
    faAddressBook,
    faCalendar,
    faCalendarCheck,
    faClock,
    faQuestionCircle
} from "@fortawesome/free-regular-svg-icons";
import ExtendableCardBody from "../../layout/extension/ExtendableCardBody";
import ExtendableRow from "../../layout/extension/ExtendableRow";
import {
    faCircle,
    faCrosshairs,
    faGavel,
    faLocationArrow,
    faServer,
    faSignal,
    faSkull,
    faUser,
    faUserPlus
} from "@fortawesome/free-solid-svg-icons";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";
import Datapoint from "../../Datapoint";

const PlayerHeadSection = ({player}) => {
    const {t} = useTranslation();
    const {getPlayerHeadImageUrl} = useMetadata();
    const headImageUrl = getPlayerHeadImageUrl(player.info.name, player.info.uuid);

    return (
        <>
            <Col xs={4}>
                <p>
                    <Fa icon={faCircle} className={player.info.online ? "col-green" : "col-red"}/>
                    {' ' + (player.info.online ? t('html.value.online') : t('html.value.offline'))}
                </p>
                {player.info.operator ?
                    <p><Fa icon={faSuperpowers} className="col-blue"/> {t('html.label.operator')}</p> : ''}
                <p><Fa icon={faGavel}
                       className="col-brown"/> {t('html.label.timesKicked')}: {player.info.kick_count}</p>
            </Col>
            <Col xs={4}>
                <img className="rounded mx-auto d-block"
                     alt="player head"
                     src={headImageUrl}/>
            </Col>
            <Col xs={4}>
                <p><Fa icon={faCrosshairs}
                       className="col-red"/> {t('html.label.playerKills')}: {player.info.player_kill_count}
                </p>
                <p><Fa icon={faCrosshairs}
                       className="col-green"/> {t('html.label.mobKills')}: {player.info.mob_kill_count}</p>
                <p><Fa icon={faSkull}/> {t('html.label.deaths')}: {player.info.death_count}</p>
            </Col>
        </>
    )
}

const PlayerOverviewCard = ({player}) => {
    const {t} = useTranslation();
    const {setHelpModalTopic} = useNavigation();
    const openHelp = useCallback(() => setHelpModalTopic('activity-index'), [setHelpModalTopic]);

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faAddressBook}/> {player.info.name}
                </h6>
            </Card.Header>
            <ExtendableCardBody id={'card-body-player-overview-card'}>
                <ExtendableRow id={'row-player-overview-card-0'}>
                    <PlayerHeadSection player={player}/>
                </ExtendableRow>
                <hr/>
                <ExtendableRow id={'row-player-overview-card-1'}>
                    <Col lg={6}>
                        <Datapoint
                            icon={faClock} color="green"
                            name={t('html.label.totalPlaytime')} value={player.info.playtime}
                        />
                        <Datapoint
                            icon={faClock} color="green"
                            name={t('html.label.totalActive')} value={player.info.active_playtime}
                        />
                        <Datapoint
                            icon={faClock} color="grey"
                            name={t('html.label.totalAfk')} value={player.info.afk_time}
                        />
                        <hr/>
                        <Datapoint
                            icon={faCalendarCheck} color="teal"
                            name={t('html.label.sessions')} value={player.info.session_count} bold
                        />
                        <Datapoint
                            icon={faClock} color="teal"
                            name={t('html.label.longestSession')} value={player.info.longest_session_length}
                        />
                        <Datapoint
                            icon={faClock} color="teal"
                            name={t('html.label.sessionMedian')} value={player.info.session_median}
                        />
                        <hr/>
                        <Datapoint
                            icon={faUserPlus} color="light-green"
                            name={t('html.label.registered')} value={player.info.registered} boldTitle
                        />
                    </Col>
                    <Col lg={6}>
                        <Datapoint
                            icon={faUser} color="amber"
                            name={<>{t('html.label.activityIndex')} <span>
                                <button onClick={openHelp}><Fa className={"col-blue"}
                                                               icon={faQuestionCircle}/>
                                </button></span></>}
                            value={player.info.activity_index} bold
                            valueLabel={player.info.activity_index_group}
                            title={t('html.label.activityIndex')}
                        />
                        <Datapoint
                            icon={faServer} color="light-green"
                            name={t('html.label.favoriteServer')} value={player.info.favorite_server}
                        />
                        <Datapoint
                            icon={faLocationArrow} color="amber"
                            name={t('html.label.joinAddress')} value={player.info.latest_join_address}
                        />
                        <hr/>
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.averagePing')} value={player.info.average_ping}
                        />
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.bestPing')} value={player.info.best_ping}
                        />
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.worstPing')} value={player.info.worst_ping}
                        />
                        <hr/>
                        <Datapoint
                            icon={faCalendar} color="teal"
                            name={t('html.label.lastSeen')} value={player.info.last_seen} boldTitle
                        />
                    </Col>
                </ExtendableRow>
            </ExtendableCardBody>
        </Card>
    );
};

export default PlayerOverviewCard