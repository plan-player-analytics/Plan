import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchSessionOverview} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import Datapoint from "../../../Datapoint";
import {useTranslation} from "react-i18next";
import {faGamepad, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faClock} from "@fortawesome/free-regular-svg-icons";

const SessionInsightsCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchSessionOverview, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <InsightsFor30DaysCard>
            <Datapoint name={t('html.label.mostActiveGamemode')} icon={faGamepad} color="teal" bold
                       value={data?.insights.most_active_gamemode}
                       valueLabel={data?.insights.most_active_gamemode_perc}
            />
            <Datapoint name={t('html.label.serverOccupied')} icon={faUsers} color="teal"
                       value={'~' + data?.insights.server_occupied} valueLabel={data?.insights.server_occupied_perc}
            />
            <Datapoint name={t('html.label.playtime')} icon={faClock} color="green"
                       value={data?.insights.total_playtime}
            />
            <Datapoint name={t('html.label.afkTime')} icon={faClock} color="grey"
                       value={data?.insights.afk_time} valueLabel={data?.insights.afk_time_perc}
            />
        </InsightsFor30DaysCard>
    )
}

export default SessionInsightsCard;