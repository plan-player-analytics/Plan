import React from 'react';
import {Card} from "react-bootstrap";
import CardHeader from "../CardHeader";
import {
    faBookOpen,
    faChartLine,
    faExclamationCircle,
    faPowerOff,
    faTachometerAlt,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import Datapoint from "../../Datapoint";
import CurrentUptime from "../../datapoint/CurrentUptime";

const QuickViewDataCard = ({server}) => {
    const {t} = useTranslation()

    return (
        <Card>
            <CardHeader icon={faBookOpen} color={'servers'} label={server.name + ' ' + t('html.label.asNumbers')}/>
            <Card.Body>
                <CurrentUptime uptime={server.current_uptime}/>
                <Datapoint name={t('html.label.lastPeak') + ' (' + server.last_peak_date + ')'}
                           color={'player-peak-last'} icon={faChartLine}
                           value={server.last_peak_players} valueLabel={t('html.unit.players')} bold/>
                <Datapoint name={t('html.label.bestPeak') + ' (' + server.best_peak_date + ')'}
                           color={'player-peak-all-time'} icon={faChartLine}
                           value={server.best_peak_players} valueLabel={t('html.unit.players')} bold/>
                <hr/>
                <p><b>{t('html.label.last7days')}</b></p>
                <Datapoint icon={faUsers} color={'players-unique'} name={t('html.label.uniquePlayers')}
                           value={server.unique_players}/>
                <Datapoint icon={faUsers} color={'players-new'} name={t('html.label.newPlayers')}
                           value={server.new_players}/>
                <Datapoint icon={faTachometerAlt} color={'tps-average'} name={t('html.label.averageTps')}
                           value={server.avg_tps}/>
                <Datapoint icon={faExclamationCircle} color={'tps-low-spikes'} name={t('html.label.lowTpsSpikes')}
                           value={server.low_tps_spikes}/>
                <Datapoint icon={faPowerOff} color={'downtime'} name={t('html.label.downtime')}
                           value={server.downtime}/>
            </Card.Body>
        </Card>
    )
};

export default QuickViewDataCard