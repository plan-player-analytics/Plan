import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import Datapoint from "../../../Datapoint";
import {faDragon, faMap, faTachometerAlt, faUsers} from "@fortawesome/free-solid-svg-icons";

const PerformanceInsightsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <></>

    return (
        <InsightsFor30DaysCard>
            <p>{t('html.label.duringLowTps')}</p>
            <Datapoint name={t('html.label.average') + ' ' + t('html.label.players')} icon={faUsers} color="red"
                       value={data.low_tps_players} bold/>
            <Datapoint name={t('html.label.averageEntities')} icon={faDragon} color="red"
                       value={data.low_tps_entities}/>
            <Datapoint name={t('html.label.averageChunks')} icon={faMap} color="red"
                       value={data.low_tps_chunks}/>
            <Datapoint name={t('html.label.averageCpuUsage')} icon={faTachometerAlt} color="red"
                       value={data.low_tps_entities}/>
            <Datapoint name={t('html.label.averageTps')} icon={faTachometerAlt} color="red"
                       value={data.low_tps_tps}/>
        </InsightsFor30DaysCard>
    )
}

export default PerformanceInsightsCard;