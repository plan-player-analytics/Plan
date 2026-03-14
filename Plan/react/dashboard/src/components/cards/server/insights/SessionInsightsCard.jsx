import React from "react";
import InsightsFor30DaysCard from "../../common/InsightsFor30DaysCard";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchSessionOverview} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView.tsx";
import Datapoint from "../../../datapoint/Datapoint.tsx";
import {useTranslation} from "react-i18next";
import {faGamepad, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faClock} from "@fortawesome/free-regular-svg-icons";
import {fetchNetworkSessionsOverview} from "../../../../service/networkService";
import FormattedTime from "../../../text/FormattedTime.jsx";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook.tsx";
import {QueryDatapoint, QueryDatapointValue} from "../../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint.ts";
import {TitleWithDates} from "../../../text/TitleWithDates.tsx";

const SessionInsightsCard = ({identifier}) => {
    const {t} = useTranslation();
    const {after, before, server} = useGenericFilter();

    const filter = {
        after: after || Date.now() - 30 * 24 * 60 * 60 * 1000,
        before,
        server
    };

    const title = <TitleWithDates label={'html.label.insights'} fallback={'html.label.insights30days'} after={after}
                                  before={before}/>;

    const {
        data,
        loadingError
    } = useDataRequest(identifier ? fetchSessionOverview : fetchNetworkSessionsOverview, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    const insights = data?.insights;
    if (!insights) return <></>

    return (
        <InsightsFor30DaysCard id={'session-insights'} title={title}>
            <Datapoint name={t('html.label.mostActiveGamemode')} icon={faGamepad} color="gamemode" bold
                       value={insights.most_active_gamemode}
                       valueLabel={insights.most_active_gamemode_perc}
            />
            <Datapoint name={t('html.label.serverOccupied')} icon={faUsers} color="sessions"
                       value={insights.server_occupied ? <>{'~'}<FormattedTime
                           timeMs={insights.server_occupied}/></> : undefined}
                       valueLabel={insights.server_occupied_perc}
            />
            <QueryDatapoint name={t('html.label.playtime')} icon={faClock} color="playtime"
                            dataType={DatapointType.PLAYTIME} filter={filter}/>
            <QueryDatapoint name={t('html.label.afkTime')} icon={faClock} color="playtime-afk"
                            dataType={DatapointType.AFK_TIME} filter={filter}
                            valueLabel={<>(<QueryDatapointValue dataType={DatapointType.AFK_TIME_PERCENTAGE}
                                                                filter={filter}/>)</>}
            />
        </InsightsFor30DaysCard>
    )
}

export default SessionInsightsCard;