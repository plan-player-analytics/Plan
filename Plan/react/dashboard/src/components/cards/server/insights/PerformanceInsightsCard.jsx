import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useTranslation} from "react-i18next";
import Datapoint from "../../../Datapoint";
import {faDragon, faMap, faStopwatch, faTachometerAlt, faUsers} from "@fortawesome/free-solid-svg-icons";
import {CardLoader} from "../../../navigation/Loader";

const PerformanceInsightsCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <CardLoader/>;

    return (
        <InsightsFor30DaysCard id={"performance-insights"}>
            <p>{t('html.label.duringLowTps')}</p>
            <Datapoint name={t('html.label.averagePlayers')} icon={faUsers}
                       color="tps-low-spikes"
                       value={data.low_tps_players} bold/>
            <Datapoint name={t('html.label.averageEntities')} icon={faDragon} color="tps-low-spikes"
                       value={data.low_tps_entities}/>
            <Datapoint name={t('html.label.averageChunks')} icon={faMap} color="tps-low-spikes"
                       value={data.low_tps_chunks}/>
            <Datapoint name={t('html.label.averageCpuUsage')} icon={faTachometerAlt} color="tps-low-spikes"
                       value={data.low_tps_entities}/>
            <Datapoint name={t('html.label.averageTps')} icon={faTachometerAlt} color="tps-low-spikes"
                       value={data.low_tps_tps}/>
            <Datapoint name={t('html.label.averageMspt')} icon={faStopwatch} color="tps-low-spikes"
                       value={data.low_tps_mspt}/>
        </InsightsFor30DaysCard>
    )
}

export default PerformanceInsightsCard;