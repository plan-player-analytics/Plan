import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchSessionOverview} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import Datapoint from "../../../Datapoint";
import {useTranslation} from "react-i18next";
import {faGamepad, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import {fetchNetworkSessionsOverview} from "../../../../service/networkService";
import FormattedTime from "../../../text/FormattedTime.jsx";

const SessionInsightsCard = ({identifier}) => {
    const {t} = useTranslation();

    const {
        data,
        loadingError
    } = useDataRequest(identifier ? fetchSessionOverview : fetchNetworkSessionsOverview, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    const insights = data?.insights;
    if (!insights) return <></>

    return (
        <InsightsFor30DaysCard id={'session-insights'}>
            <Datapoint name={t('html.label.mostActiveGamemode')} icon={faGamepad} color="gamemode" bold
                       value={insights.most_active_gamemode}
                       valueLabel={insights.most_active_gamemode_perc}
            />
            <Datapoint name={t('html.label.serverOccupied')} icon={faUsers} color="sessions"
                       value={insights.server_occupied ? <>{'~'}<FormattedTime
                           timeMs={insights.server_occupied}/></> : undefined}
                       valueLabel={insights.server_occupied_perc}
            />
            <Datapoint name={t('html.label.playtime')} icon={faClock} color="playtime"
                       value={<FormattedTime timeMs={insights.total_playtime}/>}
            />
            <Datapoint name={t('html.label.afkTime')} icon={faClock} color="playtime-afk"
                       value={<FormattedTime timeMs={insights.afk_time}/>} valueLabel={insights.afk_time_perc}
            />
        </InsightsFor30DaysCard>
    )
}

export default SessionInsightsCard;