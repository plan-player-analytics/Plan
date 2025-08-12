import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faBookOpen, faChartLine, faCrosshairs, faSkull, faUser, faUsers} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../../../Datapoint";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import {CardLoader} from "../../../navigation/Loader";
import ExtendableCardBody from "../../../layout/extension/ExtendableCardBody";
import {useMetadata} from "../../../../hooks/metadataHook";
import CurrentUptime from "../../../datapoint/CurrentUptime";
import FormattedTime from "../../../text/FormattedTime.jsx";
import FormattedDate from "../../../text/FormattedDate.jsx";

const ServerAsNumbersCard = ({data}) => {
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();

    if (!data || !networkMetadata) return <CardLoader/>;

    const isGameServer = data.player_kills !== undefined;
    const showPeaks = isGameServer || networkMetadata.usingRedisBungee || networkMetadata.servers.filter(server => server.proxy).length === 1;

    return (
        <Card id={isGameServer ? "server-as-numbers" : "network-as-numbers"}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faBookOpen}/> {isGameServer ? t('html.label.serverAsNumberse') : t('html.label.networkAsNumbers')}
                </h6>
            </Card.Header>
            <ExtendableCardBody
                id={data.player_kills !== undefined ? 'card-body-server-as-numbers' : 'card-body-network-as-numbers'}>
                <CurrentUptime uptime={data.current_uptime}/>
                <hr/>
                <Datapoint name={t('html.label.totalPlayers')}
                           color={'players-count'} icon={faUsers}
                           value={data.total_players} bold/>
                <Datapoint name={t('html.label.regularPlayers')}
                           color={'players-regular'} icon={faUsers}
                           value={data.regular_players} bold/>
                <Datapoint name={t('html.label.playersOnline')}
                           color={'players-online'} icon={faUser}
                           value={data.online_players} bold/>
                {showPeaks && <>
                    <hr/>
                    <Datapoint name={<>{t('html.label.lastPeak')} (<FormattedDate date={data.last_peak_date}/>)</>}
                               color={'player-peak-last'} icon={faChartLine}
                               value={data.last_peak_players} valueLabel={t('html.unit.players')} bold/>
                    <Datapoint name={<>{t('html.label.bestPeak')} (<FormattedDate date={data.best_peak_date}/>)</>}
                               color={'player-peak-all-time'} icon={faChartLine}
                               value={data.best_peak_players} valueLabel={t('html.unit.players')} bold/>
                </>}
                <hr/>
                <Datapoint name={t('html.label.totalPlaytime')}
                           color={'playtime'} icon={faClock}
                           value={<FormattedTime timeMs={data.playtime}/>}/>
                <Datapoint name={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                           color={'playtime'} icon={faClock}
                           value={<FormattedTime timeMs={data.player_playtime}/>}/>
                {data.session_length_avg && <Datapoint name={t('html.label.averageSessionLength')}
                           color={'sessions'} icon={faClock}
                                                       value={<FormattedTime timeMs={data.session_length_avg}/>}/>}
                <Datapoint name={t('html.label.sessions')}
                           color={'sessions'} icon={faCalendarCheck}
                           value={data.sessions} bold/>
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
    )
}

export default ServerAsNumbersCard;