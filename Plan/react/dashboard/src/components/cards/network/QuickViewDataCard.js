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

const QuickViewDataCard = ({server}) => {
    const {t} = useTranslation()

    return (
        <Card>
            <CardHeader icon={faBookOpen} color={'light-green'} label={server.name + ' ' + t('html.label.asNumbers')}/>
            <Card.Body>
                <Datapoint icon={faPowerOff} color={'light-green'} name={t('html.label.currentUptime')}
                           value={server.current_uptime}/>
                <Datapoint name={t('html.label.lastPeak') + ' (' + server.last_peak_date + ')'}
                           color={'blue'} icon={faChartLine}
                           value={server.last_peak_players} valueLabel={t('html.unit.players')} bold/>
                <Datapoint name={t('html.label.bestPeak') + ' (' + server.best_peak_date + ')'}
                           color={'light-green'} icon={faChartLine}
                           value={server.best_peak_players} valueLabel={t('html.unit.players')} bold/>
                <hr/>
                <p><b>{t('html.label.last7days')}</b></p>
                <Datapoint icon={faUsers} color={'light-blue'} name={t('html.label.uniquePlayers')}
                           value={server.unique_players}/>
                <Datapoint icon={faUsers} color={'light-green'} name={t('html.label.newPlayers')}
                           value={server.new_players}/>
                <Datapoint icon={faTachometerAlt} color={'orange'} name={t('html.label.averageTps')}
                           value={server.avg_tps}/>
                <Datapoint icon={faExclamationCircle} color={'red'} name={t('html.label.lowTpsSpikes')}
                           value={server.low_tps_spikes}/>
                <Datapoint icon={faPowerOff} color={'red'} name={t('html.label.downtime')}
                           value={server.downtime}/>
            </Card.Body>
        </Card>
    )
};

export default QuickViewDataCard